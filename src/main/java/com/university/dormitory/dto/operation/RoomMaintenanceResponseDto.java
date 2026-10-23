package com.university.dormitory.dto.operation;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomMaintenanceResponseDto {
    private Long roomId;
    private String roomNumber;
    private Boolean isAvailable;
    private int terminatedContractsCount;
    private String message;
}
