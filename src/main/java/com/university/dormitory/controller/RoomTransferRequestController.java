package com.university.dormitory.controller;

import com.university.dormitory.dto.RoomTransferRequestDto;
import com.university.dormitory.dto.RoomTransferResponseDto;
import com.university.dormitory.service.RoomTransferRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room-transfer-requests")
@RequiredArgsConstructor
public class RoomTransferRequestController {

    private final RoomTransferRequestService transferService;

    @GetMapping
    public ResponseEntity<List<RoomTransferResponseDto>> getAll() {
        return ResponseEntity.ok(transferService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomTransferResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.findById(id));
    }

    @PostMapping
    public ResponseEntity<RoomTransferResponseDto> create(
            @RequestBody RoomTransferRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transferService.create(dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RoomTransferResponseDto> update(
            @PathVariable Long id,
            @RequestBody RoomTransferRequestDto dto) {
        return ResponseEntity.ok(transferService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        transferService.delete(id);
    }
}
