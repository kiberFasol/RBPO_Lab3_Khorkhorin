package com.university.dormitory.controller;

import com.university.dormitory.dto.HousingApplicationRequestDto;
import com.university.dormitory.dto.HousingApplicationResponseDto;
import com.university.dormitory.service.HousingApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/housing-applications")
@RequiredArgsConstructor
public class HousingApplicationController {

    private final HousingApplicationService applicationService;

    @GetMapping
    public ResponseEntity<List<HousingApplicationResponseDto>> getAll() {
        return ResponseEntity.ok(applicationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HousingApplicationResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.findById(id));
    }

    @PostMapping
    public ResponseEntity<HousingApplicationResponseDto> create(
            @RequestBody HousingApplicationRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.create(dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<HousingApplicationResponseDto> update(
            @PathVariable Long id,
            @RequestBody HousingApplicationRequestDto dto) {
        return ResponseEntity.ok(applicationService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        applicationService.delete(id);
    }
}
