package com.university.dormitory.dto.operation;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInRequestDto {

    @NotNull(message = "ID заявления обязателен")
    private Long applicationId;

    @NotNull(message = "ID комнаты обязателен")
    private Long roomId;

    @NotNull(message = "Дата заселения обязательна")
    private LocalDate checkInDate;

    @NotNull(message = "Дата выселения обязательна")
    private LocalDate checkOutDate;
}
