package com.university.dormitory.dto;

import lombok.Data;

@Data
public class RoomRequestDto {

    private String roomNumber;

    private Integer capacity;

    private Boolean isAvailable;
}
