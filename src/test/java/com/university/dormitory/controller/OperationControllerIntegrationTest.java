package com.university.dormitory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.dormitory.dto.operation.CheckInRequestDto;
import com.university.dormitory.dto.operation.RoomMaintenanceRequestDto;
import com.university.dormitory.dto.operation.TerminateContractRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OperationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("CRUD проверка: получение списка пользователей из БД")
    void getUsers_ReturnsList() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("CRUD проверка: получение списка комнат из БД")
    void getRooms_ReturnsList() throws Exception {
        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Операция 4 (интеграция): вызов регламентного закрытия договоров")
    void processExpirations_ReturnsSuccess() throws Exception {
        mockMvc.perform(post("/api/operations/contracts/process-expirations")
                        .param("asOfDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiredContractsCount").isNumber())
                .andExpect(jsonPath("$.freedRoomsCount").isNumber());
    }

    @Test
    @DisplayName("Операция 1 (интеграция): ошибка валидации дат заселения (выезд раньше заезда)")
    void checkIn_InvalidDates_ReturnsBadRequest() throws Exception {
        CheckInRequestDto dto = CheckInRequestDto.builder()
                .applicationId(3L)
                .roomId(1L)
                .checkInDate(LocalDate.now().plusMonths(2))
                .checkOutDate(LocalDate.now().plusMonths(1)) // раньше заезда!
                .build();

        mockMvc.perform(post("/api/operations/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
