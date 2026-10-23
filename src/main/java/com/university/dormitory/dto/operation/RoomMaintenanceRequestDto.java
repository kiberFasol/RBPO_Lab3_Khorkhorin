package com.university.dormitory.dto.operation;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomMaintenanceRequestDto {
    private String reason;
    private MaintenancePolicy policy;

    public enum MaintenancePolicy {
        REJECT_IF_OCCUPIED,
        FORCE_TERMINATE_CONTRACTS
    }
}
