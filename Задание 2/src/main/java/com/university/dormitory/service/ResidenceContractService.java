package com.university.dormitory.service;

import com.university.dormitory.dto.ResidenceContractRequestDto;
import com.university.dormitory.dto.ResidenceContractResponseDto;
import com.university.dormitory.entity.HousingApplication;
import com.university.dormitory.entity.ResidenceContract;
import com.university.dormitory.entity.ResidenceContract.ContractStatus;
import com.university.dormitory.entity.Room;
import com.university.dormitory.exception.ResourceNotFoundException;
import com.university.dormitory.repository.HousingApplicationRepository;
import com.university.dormitory.repository.ResidenceContractRepository;
import com.university.dormitory.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResidenceContractService {

    private final ResidenceContractRepository contractRepository;
    private final HousingApplicationRepository applicationRepository;
    private final RoomRepository roomRepository;

    public List<ResidenceContractResponseDto> findAll() {
        return contractRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ResidenceContractResponseDto findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public ResidenceContractResponseDto create(ResidenceContractRequestDto dto) {
        HousingApplication application = applicationRepository.findById(dto.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("HousingApplication", dto.getApplicationId()));
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", dto.getRoomId()));

        ResidenceContract contract = ResidenceContract.builder()
                .application(application)
                .room(room)
                .checkInDate(dto.getCheckInDate())
                .checkOutDate(dto.getCheckOutDate())
                .status(ContractStatus.ACTIVE)
                .build();
        return toResponse(contractRepository.save(contract));
    }

    @Transactional
    public ResidenceContractResponseDto update(Long id, ResidenceContractRequestDto dto) {
        ResidenceContract contract = getOrThrow(id);

        if (dto.getApplicationId() != null) {
            HousingApplication app = applicationRepository.findById(dto.getApplicationId())
                    .orElseThrow(() -> new ResourceNotFoundException("HousingApplication", dto.getApplicationId()));
            contract.setApplication(app);
        }
        if (dto.getRoomId() != null) {
            Room room = roomRepository.findById(dto.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room", dto.getRoomId()));
            contract.setRoom(room);
        }
        if (dto.getCheckInDate() != null) contract.setCheckInDate(dto.getCheckInDate());
        if (dto.getCheckOutDate() != null) contract.setCheckOutDate(dto.getCheckOutDate());
        if (dto.getStatus() != null) {
            contract.setStatus(ContractStatus.valueOf(dto.getStatus().toUpperCase()));
        }
        return toResponse(contractRepository.save(contract));
    }

    @Transactional
    public void delete(Long id) {
        ResidenceContract contract = getOrThrow(id);
        contractRepository.delete(contract);
    }

    // --- helpers ---

    ResidenceContract getOrThrow(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ResidenceContract", id));
    }

    private ResidenceContractResponseDto toResponse(ResidenceContract c) {
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
