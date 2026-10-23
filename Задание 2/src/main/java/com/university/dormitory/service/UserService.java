package com.university.dormitory.service;

import com.university.dormitory.dto.UserRequestDto;
import com.university.dormitory.dto.UserResponseDto;
import com.university.dormitory.entity.User;
import com.university.dormitory.exception.ResourceNotFoundException;
import com.university.dormitory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponseDto> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponseDto findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public UserResponseDto create(UserRequestDto dto) {
        User user = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .role(dto.getRole())
                .build();
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponseDto update(Long id, UserRequestDto dto) {
        User user = getOrThrow(id);
        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getRole() != null) user.setRole(dto.getRole());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        User user = getOrThrow(id);
        userRepository.delete(user);
    }

    // --- helpers ---

    private User getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private UserResponseDto toResponse(User u) {
        return UserResponseDto.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .role(u.getRole())
                .build();
    }
}
