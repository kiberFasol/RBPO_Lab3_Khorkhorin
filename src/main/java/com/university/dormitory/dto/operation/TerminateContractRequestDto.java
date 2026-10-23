package com.university.dormitory.dto.operation;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TerminateContractRequestDto {
    private LocalDate terminationDate;
    private String reason;
}
