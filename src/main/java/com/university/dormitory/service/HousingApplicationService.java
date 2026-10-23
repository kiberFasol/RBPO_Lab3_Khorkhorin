package com.university.dormitory.service;

import com.university.dormitory.dto.HousingApplicationRequestDto;
import com.university.dormitory.dto.HousingApplicationResponseDto;
import com.university.dormitory.entity.HousingApplication;
import com.university.dormitory.entity.HousingApplication.ApplicationStatus;
import com.university.dormitory.entity.User;
import com.university.dormitory.exception.ResourceNotFoundException;
import com.university.dormitory.repository.HousingApplicationRepository;
import com.university.dormitory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HousingApplicationService {

    private final HousingApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public List<HousingApplicationResponseDto> findAll() {
        return applicationRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public HousingApplicationResponseDto findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public HousingApplicationResponseDto create(HousingApplicationRequestDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", dto.getUserId()));

        HousingApplication app = HousingApplication.builder()
                .user(user)
                .submissionDate(LocalDate.now())   // server-side timestamp
                .status(ApplicationStatus.PENDING)
                .build();
        return toResponse(applicationRepository.save(app));
    }

    @Transactional
    public HousingApplicationResponseDto update(Long id, HousingApplicationRequestDto dto) {
        HousingApplication app = getOrThrow(id);

        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", dto.getUserId()));
            app.setUser(user);
        }
        if (dto.getStatus() != null) {
            app.setStatus(ApplicationStatus.valueOf(dto.getStatus().toUpperCase()));
        }
        // submissionDate is NEVER overridden by client
        return toResponse(applicationRepository.save(app));
    }

    @Transactional
    public void delete(Long id) {
        HousingApplication app = getOrThrow(id);
        applicationRepository.delete(app);
    }

    // --- helpers ---

    HousingApplication getOrThrow(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HousingApplication", id));
    }

    private HousingApplicationResponseDto toResponse(HousingApplication a) {
        return HousingApplicationResponseDto.builder()
                .id(a.getId())
                .userId(a.getUser().getId())
                .userFullName(a.getUser().getFullName())
                .submissionDate(a.getSubmissionDate())
                .status(a.getStatus().name())
                .build();
    }
}
