package com.greenharvest.user.service;

import com.greenharvest.common.exception.ResourceNotFoundException;
import com.greenharvest.auth.enums.Role;
import com.greenharvest.user.api.dto.UserResponse;
import com.greenharvest.user.model.User;
import com.greenharvest.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse updateUserRole(Long id, Role newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setRole(newRole);
        // Challenge D: Bump the tokenVersion to immediately invalidate existing JWTs
        user.setTokenVersion(user.getTokenVersion() + 1);

        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse toggleUserStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setActive(active);
        // Deactivation should also invalidate current active sessions
        user.setTokenVersion(user.getTokenVersion() + 1);

        return mapToResponse(userRepository.save(user));
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getTokenVersion(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}