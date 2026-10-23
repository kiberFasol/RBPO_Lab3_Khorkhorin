package com.university.dormitory.service;

import com.university.dormitory.dto.RoomTransferRequestDto;
import com.university.dormitory.dto.RoomTransferResponseDto;
import com.university.dormitory.entity.ResidenceContract;
import com.university.dormitory.entity.Room;
import com.university.dormitory.entity.RoomTransferRequest;
import com.university.dormitory.entity.RoomTransferRequest.TransferStatus;
import com.university.dormitory.exception.ResourceNotFoundException;
import com.university.dormitory.repository.ResidenceContractRepository;
import com.university.dormitory.repository.RoomRepository;
import com.university.dormitory.repository.RoomTransferRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomTransferRequestService {

    private final RoomTransferRequestRepository transferRepository;
    private final ResidenceContractRepository contractRepository;
    private final RoomRepository roomRepository;

    public List<RoomTransferResponseDto> findAll() {
        return transferRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public RoomTransferResponseDto findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public RoomTransferResponseDto create(RoomTransferRequestDto dto) {
        ResidenceContract contract = contractRepository.findById(dto.getContractId())
                .orElseThrow(() -> new ResourceNotFoundException("ResidenceContract", dto.getContractId()));
        Room targetRoom = roomRepository.findById(dto.getTargetRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", dto.getTargetRoomId()));

        RoomTransferRequest request = RoomTransferRequest.builder()
                .contract(contract)
                .targetRoom(targetRoom)
                .submissionDate(LocalDate.now())   // server-side timestamp
                .status(TransferStatus.PENDING)
                .build();
        return toResponse(transferRepository.save(request));
    }

    @Transactional
    public RoomTransferResponseDto update(Long id, RoomTransferRequestDto dto) {
        RoomTransferRequest request = getOrThrow(id);

        if (dto.getContractId() != null) {
            ResidenceContract contract = contractRepository.findById(dto.getContractId())
                    .orElseThrow(() -> new ResourceNotFoundException("ResidenceContract", dto.getContractId()));
            request.setContract(contract);
        }
        if (dto.getTargetRoomId() != null) {
            Room room = roomRepository.findById(dto.getTargetRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room", dto.getTargetRoomId()));
            request.setTargetRoom(room);
        }
        if (dto.getStatus() != null) {
            request.setStatus(TransferStatus.valueOf(dto.getStatus().toUpperCase()));
        }
        // submissionDate is NEVER overridden by client
        return toResponse(transferRepository.save(request));
    }

    @Transactional
    public void delete(Long id) {
        RoomTransferRequest request = getOrThrow(id);
        transferRepository.delete(request);
    }

    // --- helpers ---

    private RoomTransferRequest getOrThrow(Long id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RoomTransferRequest", id));
    }

    private RoomTransferResponseDto toResponse(RoomTransferRequest r) {
        return RoomTransferResponseDto.builder()
                .id(r.getId())
                .contractId(r.getContract().getId())
                .targetRoomId(r.getTargetRoom().getId())
                .targetRoomNumber(r.getTargetRoom().getRoomNumber())
                .submissionDate(r.getSubmissionDate())
                .status(r.getStatus().name())
                .build();
    }
}
