package com.university.dormitory.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ResidenceContractRequestDto {

    private Long applicationId;

    private Long roomId;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    /** Status is optional on create (defaults to ACTIVE). */
    private String status;
}
