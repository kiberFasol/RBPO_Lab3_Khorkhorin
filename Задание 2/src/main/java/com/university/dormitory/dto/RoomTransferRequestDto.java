package com.university.dormitory.dto;

import lombok.Data;

@Data
public class RoomTransferRequestDto {

    private Long contractId;

    private Long targetRoomId;

    /** Status is optional on create (defaults to PENDING). */
    private String status;
}
