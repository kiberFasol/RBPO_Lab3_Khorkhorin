package com.university.dormitory.dto;

import lombok.Data;

@Data
public class HousingApplicationRequestDto {

    private Long userId;

    /** Status is optional on create (defaults to PENDING). Can be set on update. */
    private String status;
}
