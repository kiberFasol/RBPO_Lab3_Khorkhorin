package com.university.dormitory.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class HousingApplicationResponseDto {
    private Long id;
    private Long userId;
    private String userFullName;
    private LocalDate submissionDate;
    private String status;
}
