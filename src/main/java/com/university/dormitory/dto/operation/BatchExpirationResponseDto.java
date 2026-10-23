package com.university.dormitory.dto.operation;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchExpirationResponseDto {
    private LocalDate asOfDate;
    private int expiredContractsCount;
    private int freedRoomsCount;
    private List<Long> expiredContractIds;
    private String message;
}
