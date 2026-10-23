package com.university.dormitory.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomResponseDto {
    private Long id;
    private String roomNumber;
    private Integer capacity;
    private Boolean isAvailable;
}
