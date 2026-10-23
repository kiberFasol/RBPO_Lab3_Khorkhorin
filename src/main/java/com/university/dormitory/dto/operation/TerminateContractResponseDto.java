package com.university.dormitory.dto.operation;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TerminateContractResponseDto {
    private Long contractId;
    private Long studentId;
    private String studentName;
    private String roomNumber;
    private LocalDate checkOutDate;
    private String contractStatus;
    private int rejectedTransferRequestsCount;
    private String message;
}
