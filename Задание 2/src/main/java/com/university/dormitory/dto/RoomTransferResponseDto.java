package com.university.dormitory.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RoomTransferResponseDto {
    private Long id;
    private Long contractId;
    private Long targetRoomId;
    private String targetRoomNumber;
    private LocalDate submissionDate;
    private String status;
}
