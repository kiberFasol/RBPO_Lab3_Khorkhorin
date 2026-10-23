package com.university.dormitory.controller;

import com.university.dormitory.dto.ResidenceContractRequestDto;
import com.university.dormitory.dto.ResidenceContractResponseDto;
import com.university.dormitory.service.ResidenceContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/residence-contracts")
@RequiredArgsConstructor
public class ResidenceContractController {

    private final ResidenceContractService contractService;

    @GetMapping
    public ResponseEntity<List<ResidenceContractResponseDto>> getAll() {
        return ResponseEntity.ok(contractService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResidenceContractResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ResidenceContractResponseDto> create(
            @RequestBody ResidenceContractRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contractService.create(dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResidenceContractResponseDto> update(
            @PathVariable Long id,
            @RequestBody ResidenceContractRequestDto dto) {
        return ResponseEntity.ok(contractService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        contractService.delete(id);
    }
}
