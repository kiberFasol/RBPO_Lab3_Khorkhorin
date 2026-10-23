package com.university.dormitory.service;

import com.university.dormitory.dto.ResidenceContractResponseDto;
import com.university.dormitory.dto.operation.*;
import com.university.dormitory.entity.HousingApplication;
import com.university.dormitory.entity.HousingApplication.ApplicationStatus;
import com.university.dormitory.entity.ResidenceContract;
import com.university.dormitory.entity.ResidenceContract.ContractStatus;
import com.university.dormitory.entity.Room;
import com.university.dormitory.entity.RoomTransferRequest;
import com.university.dormitory.entity.RoomTransferRequest.TransferStatus;
import com.university.dormitory.exception.BusinessRuleException;
import com.university.dormitory.exception.ResourceNotFoundException;
import com.university.dormitory.repository.HousingApplicationRepository;
import com.university.dormitory.repository.ResidenceContractRepository;
import com.university.dormitory.repository.RoomRepository;
import com.university.dormitory.repository.RoomTransferRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DormitoryOperationService {

    private final HousingApplicationRepository applicationRepository;
    private final ResidenceContractRepository contractRepository;
    private final RoomRepository roomRepository;
    private final RoomTransferRequestRepository transferRepository;

    @Transactional
    public ResidenceContractResponseDto checkIn(CheckInRequestDto dto) {
        if (dto.getCheckInDate().isAfter(dto.getCheckOutDate())) {
            throw new BusinessRuleException("Дата заселения (" + dto.getCheckInDate() +
                    ") не может быть позже даты выселения (" + dto.getCheckOutDate() + ")", HttpStatus.BAD_REQUEST);
        }

        HousingApplication app = applicationRepository.findById(dto.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("HousingApplication", dto.getApplicationId()));

        if (app.getStatus() == ApplicationStatus.REJECTED || app.getStatus() == ApplicationStatus.CANCELLED) {
            throw new BusinessRuleException("Заявление ID " + app.getId() +
                    " отклонено/отменено и не может быть использовано для заселения", HttpStatus.BAD_REQUEST);
        }

        if (contractRepository.existsByApplicationUserIdAndStatus(app.getUser().getId(), ContractStatus.ACTIVE)) {
            throw new BusinessRuleException("Студент " + app.getUser().getFullName() +
                    " (ID " + app.getUser().getId() + ") уже имеет активный договор проживания", HttpStatus.CONFLICT);
        }

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", dto.getRoomId()));

        if (!Boolean.TRUE.equals(room.getIsAvailable())) {
            throw new BusinessRuleException("Комната " + room.getRoomNumber() +
                    " недоступна для заселения", HttpStatus.CONFLICT);
        }

        long activeResidents = contractRepository.countByRoomIdAndStatus(room.getId(), ContractStatus.ACTIVE);
        if (activeResidents >= room.getCapacity()) {
            throw new BusinessRuleException("В комнате " + room.getRoomNumber() +
                    " нет свободных мест (вместимость " + room.getCapacity() +
                    ", занято " + activeResidents + ")", HttpStatus.CONFLICT);
        }

        ResidenceContract contract = ResidenceContract.builder()
                .application(app)
                .room(room)
                .checkInDate(dto.getCheckInDate())
                .checkOutDate(dto.getCheckOutDate())
                .status(ContractStatus.ACTIVE)
                .build();
        contract = contractRepository.save(contract);

        app.setStatus(ApplicationStatus.APPROVED);
        applicationRepository.save(app);

        if (activeResidents + 1 >= room.getCapacity()) {
            room.setIsAvailable(false);
            roomRepository.save(room);
        }

        return toContractResponse(contract);
    }

    @Transactional
    public TransferApprovalResponseDto approveTransfer(Long requestId) {
        RoomTransferRequest request = transferRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("RoomTransferRequest", requestId));

        if (request.getStatus() != TransferStatus.PENDING) {
            throw new BusinessRuleException("Заявка на переселение ID " + requestId +
                    " уже обработана (статус " + request.getStatus() + ")", HttpStatus.BAD_REQUEST);
        }

        ResidenceContract contract = request.getContract();
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new BusinessRuleException("Договор проживания ID " + contract.getId() +
                    " не активен (статус " + contract.getStatus() + "). Переселение невозможно", HttpStatus.BAD_REQUEST);
        }

        Room oldRoom = contract.getRoom();
        Room targetRoom = request.getTargetRoom();

        if (Objects.equals(oldRoom.getId(), targetRoom.getId())) {
            throw new BusinessRuleException("Целевая комната (" + targetRoom.getRoomNumber() +
                    ") совпадает с текущей комнатой проживания", HttpStatus.BAD_REQUEST);
        }

        if (!Boolean.TRUE.equals(targetRoom.getIsAvailable())) {
            throw new BusinessRuleException("Целевая комната " + targetRoom.getRoomNumber() +
                    " недоступна для заселения", HttpStatus.CONFLICT);
        }

        long targetActiveCount = contractRepository.countByRoomIdAndStatus(targetRoom.getId(), ContractStatus.ACTIVE);
        if (targetActiveCount >= targetRoom.getCapacity()) {
            throw new BusinessRuleException("В целевой комнате " + targetRoom.getRoomNumber() +
                    " нет свободных мест (вместимость " + targetRoom.getCapacity() +
                    ", занято " + targetActiveCount + ")", HttpStatus.CONFLICT);
        }

        request.setStatus(TransferStatus.APPROVED);
        transferRepository.save(request);

        contract.setRoom(targetRoom);
        contractRepository.save(contract);

        oldRoom.setIsAvailable(true);
        roomRepository.save(oldRoom);

        if (targetActiveCount + 1 >= targetRoom.getCapacity()) {
            targetRoom.setIsAvailable(false);
            roomRepository.save(targetRoom);
        }

        return TransferApprovalResponseDto.builder()
                .requestId(request.getId())
                .contractId(contract.getId())
                .studentId(contract.getApplication().getUser().getId())
                .studentName(contract.getApplication().getUser().getFullName())
                .previousRoomNumber(oldRoom.getRoomNumber())
                .targetRoomNumber(targetRoom.getRoomNumber())
                .status(request.getStatus().name())
                .message("Студент успешно переселен из комнаты " + oldRoom.getRoomNumber() +
                        " в комнату " + targetRoom.getRoomNumber())
                .build();
    }

    @Transactional
    public TerminateContractResponseDto terminateContract(Long contractId, TerminateContractRequestDto dto) {
        ResidenceContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("ResidenceContract", contractId));

        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new BusinessRuleException("Договор ID " + contractId +
                    " уже закрыт (статус " + contract.getStatus() + ")", HttpStatus.BAD_REQUEST);
        }

        LocalDate effectiveDate = (dto != null && dto.getTerminationDate() != null)
                ? dto.getTerminationDate()
                : LocalDate.now();

        if (effectiveDate.isBefore(contract.getCheckInDate())) {
            throw new BusinessRuleException("Дата выселения (" + effectiveDate +
                    ") не может быть раньше даты заселения (" + contract.getCheckInDate() + ")", HttpStatus.BAD_REQUEST);
        }

        contract.setStatus(ContractStatus.TERMINATED);
        contract.setCheckOutDate(effectiveDate);
        contractRepository.save(contract);

        Room room = contract.getRoom();
        room.setIsAvailable(true);
        roomRepository.save(room);

        List<RoomTransferRequest> pendingTransfers =
                transferRepository.findAllByContractIdAndStatus(contract.getId(), TransferStatus.PENDING);
        for (RoomTransferRequest tr : pendingTransfers) {
            tr.setStatus(TransferStatus.REJECTED);
            transferRepository.save(tr);
        }

        return TerminateContractResponseDto.builder()
                .contractId(contract.getId())
                .studentId(contract.getApplication().getUser().getId())
                .studentName(contract.getApplication().getUser().getFullName())
                .roomNumber(room.getRoomNumber())
                .checkOutDate(effectiveDate)
                .contractStatus(contract.getStatus().name())
                .rejectedTransferRequestsCount(pendingTransfers.size())
                .message("Договор расторгнут. Комната " + room.getRoomNumber() + " освобождена")
                .build();
    }

    @Transactional
    public BatchExpirationResponseDto processExpirations(LocalDate asOfDate) {
        LocalDate effectiveDate = asOfDate != null ? asOfDate : LocalDate.now();

        List<ResidenceContract> expiredContracts =
                contractRepository.findAllByStatusAndCheckOutDateLessThanEqual(ContractStatus.ACTIVE, effectiveDate);

        Set<Long> affectedRoomIds = new HashSet<>();
        List<Long> expiredContractIds = new ArrayList<>();

        for (ResidenceContract c : expiredContracts) {
            c.setStatus(ContractStatus.EXPIRED);
            contractRepository.save(c);
            expiredContractIds.add(c.getId());
            affectedRoomIds.add(c.getRoom().getId());

            List<RoomTransferRequest> pendingTransfers =
                    transferRepository.findAllByContractIdAndStatus(c.getId(), TransferStatus.PENDING);
            for (RoomTransferRequest tr : pendingTransfers) {
                tr.setStatus(TransferStatus.REJECTED);
                transferRepository.save(tr);
            }
        }

        int freedRoomsCount = 0;
        for (Long roomId : affectedRoomIds) {
            Room room = roomRepository.findById(roomId).orElse(null);
            if (room != null) {
                long activeInRoom = contractRepository.countByRoomIdAndStatus(room.getId(), ContractStatus.ACTIVE);
                if (activeInRoom < room.getCapacity()) {
                    room.setIsAvailable(true);
                    roomRepository.save(room);
                    freedRoomsCount++;
                }
            }
        }

        return BatchExpirationResponseDto.builder()
                .asOfDate(effectiveDate)
                .expiredContractsCount(expiredContracts.size())
                .freedRoomsCount(freedRoomsCount)
                .expiredContractIds(expiredContractIds)
                .message("Обработано истекших договоров: " + expiredContracts.size() +
                        ", освобождено комнат: " + freedRoomsCount)
                .build();
    }

    @Transactional
    public RoomMaintenanceResponseDto setRoomMaintenance(Long roomId, RoomMaintenanceRequestDto dto) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        RoomMaintenanceRequestDto.MaintenancePolicy policy = (dto != null && dto.getPolicy() != null)
                ? dto.getPolicy()
                : RoomMaintenanceRequestDto.MaintenancePolicy.REJECT_IF_OCCUPIED;

        List<ResidenceContract> activeContracts =
                contractRepository.findAllByRoomIdAndStatus(room.getId(), ContractStatus.ACTIVE);

        int terminatedCount = 0;
        if (!activeContracts.isEmpty()) {
            if (policy == RoomMaintenanceRequestDto.MaintenancePolicy.REJECT_IF_OCCUPIED) {
                throw new BusinessRuleException("В комнате " + room.getRoomNumber() +
                        " проживают " + activeContracts.size() + " чел. Комната не может быть закрыта на ремонт без расселения", HttpStatus.CONFLICT);
            } else {
                for (ResidenceContract c : activeContracts) {
                    c.setStatus(ContractStatus.TERMINATED);
                    c.setCheckOutDate(LocalDate.now());
                    contractRepository.save(c);
                    terminatedCount++;

                    List<RoomTransferRequest> pendingTransfers =
                            transferRepository.findAllByContractIdAndStatus(c.getId(), TransferStatus.PENDING);
                    for (RoomTransferRequest tr : pendingTransfers) {
                        tr.setStatus(TransferStatus.REJECTED);
                        transferRepository.save(tr);
                    }
                }
            }
        }

        room.setIsAvailable(false);
        roomRepository.save(room);

        String reason = (dto != null && dto.getReason() != null) ? dto.getReason() : "Техническое обслуживание";
        return RoomMaintenanceResponseDto.builder()
                .roomId(room.getId())
                .roomNumber(room.getRoomNumber())
                .isAvailable(room.getIsAvailable())
                .terminatedContractsCount(terminatedCount)
                .message("Комната " + room.getRoomNumber() + " выведена на обслуживание (" + reason + ")")
                .build();
    }

    private ResidenceContractResponseDto toContractResponse(ResidenceContract c) {
        return ResidenceContractResponseDto.builder()
                .id(c.getId())
                .applicationId(c.getApplication().getId())
                .roomId(c.getRoom().getId())
                .roomNumber(c.getRoom().getRoomNumber())
                .checkInDate(c.getCheckInDate())
                .checkOutDate(c.getCheckOutDate())
                .status(c.getStatus().name())
                .build();
    }
}
