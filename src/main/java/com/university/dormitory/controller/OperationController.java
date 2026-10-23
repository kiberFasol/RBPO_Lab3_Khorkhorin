package com.university.dormitory.controller;

import com.university.dormitory.dto.ResidenceContractResponseDto;
import com.university.dormitory.dto.operation.*;
import com.university.dormitory.service.DormitoryOperationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST контроллер бизнес-операций общежития.
 */
@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
public class OperationController {

    private final DormitoryOperationService operationService;

    /**
     * Бизнес-операция 1: Заселение студента
     */
    @PostMapping("/check-in")
    public ResponseEntity<ResidenceContractResponseDto> checkIn(@Valid @RequestBody CheckInRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(operationService.checkIn(dto));
    }

    /**
     * Бизнес-операция 2: Одобрение заявки на переселение
     */
    @PostMapping("/transfers/{requestId}/approve")
    public ResponseEntity<TransferApprovalResponseDto> approveTransfer(@PathVariable Long requestId) {
        return ResponseEntity.ok(operationService.approveTransfer(requestId));
    }

    /**
     * Бизнес-операция 3: Досрочное выселение / расторжение договора
     */
    @PostMapping("/contracts/{contractId}/terminate")
    public ResponseEntity<TerminateContractResponseDto> terminateContract(
            @PathVariable Long contractId,
            @RequestBody(required = false) TerminateContractRequestDto dto) {
        return ResponseEntity.ok(operationService.terminateContract(contractId, dto));
    }

    /**
     * Бизнес-операция 4: Пакетное закрытие истекших договоров
     */
    @PostMapping("/contracts/process-expirations")
    public ResponseEntity<BatchExpirationResponseDto> processExpirations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        return ResponseEntity.ok(operationService.processExpirations(asOfDate));
    }

    /**
     * Бизнес-операция 5: Вывод комнаты на ремонт / техническое обслуживание
     */
    @PostMapping("/rooms/{roomId}/maintenance")
    public ResponseEntity<RoomMaintenanceResponseDto> setRoomMaintenance(
            @PathVariable Long roomId,
            @RequestBody(required = false) RoomMaintenanceRequestDto dto) {
        return ResponseEntity.ok(operationService.setRoomMaintenance(roomId, dto));
    }
}
