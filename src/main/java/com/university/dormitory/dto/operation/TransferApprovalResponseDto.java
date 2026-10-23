package com.university.dormitory.dto.operation;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferApprovalResponseDto {
    private Long requestId;
    private Long contractId;
    private Long studentId;
    private String studentName;
    private String previousRoomNumber;
    private String targetRoomNumber;
    private String status;
    private String message;
}
