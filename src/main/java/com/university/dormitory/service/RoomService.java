package com.university.dormitory.service;

import com.university.dormitory.dto.RoomRequestDto;
import com.university.dormitory.dto.RoomResponseDto;
import com.university.dormitory.entity.Room;
import com.university.dormitory.exception.ResourceNotFoundException;
import com.university.dormitory.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;

    public List<RoomResponseDto> findAll() {
        return roomRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public RoomResponseDto findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public RoomResponseDto create(RoomRequestDto dto) {
        Room room = Room.builder()
                .roomNumber(dto.getRoomNumber())
                .capacity(dto.getCapacity())
                .isAvailable(dto.getIsAvailable() != null ? dto.getIsAvailable() : true)
                .build();
        return toResponse(roomRepository.save(room));
    }

    @Transactional
    public RoomResponseDto update(Long id, RoomRequestDto dto) {
        Room room = getOrThrow(id);
        if (dto.getRoomNumber() != null) room.setRoomNumber(dto.getRoomNumber());
        if (dto.getCapacity() != null) room.setCapacity(dto.getCapacity());
        if (dto.getIsAvailable() != null) room.setIsAvailable(dto.getIsAvailable());
        return toResponse(roomRepository.save(room));
    }

    @Transactional
    public void delete(Long id) {
        Room room = getOrThrow(id);
        roomRepository.delete(room);
    }

    // --- helpers ---

    Room getOrThrow(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));
    }

    private RoomResponseDto toResponse(Room r) {
        return RoomResponseDto.builder()
                .id(r.getId())
                .roomNumber(r.getRoomNumber())
                .capacity(r.getCapacity())
                .isAvailable(r.getIsAvailable())
                .build();
    }
}
