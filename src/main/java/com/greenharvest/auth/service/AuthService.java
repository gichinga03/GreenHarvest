package com.greenharvest.auth.service;

import com.greenharvest.auth.dto.LoginRequest;
import com.greenharvest.auth.dto.LoginResponse;
import com.greenharvest.auth.dto.RegisterRequest;
import com.greenharvest.common.exception.DuplicateResourceException;
import com.greenharvest.common.exception.ValidationException;
import com.greenharvest.user.api.dto.UserResponse;
import com.greenharvest.user.model.User;
import com.greenharvest.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    //checking the parameters if the match
    @Transactional
    public UserResponse register(RegisterRequest request) {
        //  MANUAL VALIDATION FOR REGISTRATION (Requirement 7)
        if (request.fullName() == null || request.fullName().trim().isBlank()) {
            throw new ValidationException("Full name is required");
        }
        if (request.email() == null || request.email().trim().isBlank()) {
            throw new ValidationException("Email is required");
        }
        if (!request.email().contains("@") || !request.email().contains(".")) {
            throw new ValidationException("Email must be a valid email address");
        }
        if (request.password() == null || request.password().length() < 6) {
            throw new ValidationException("Password is required and must be at least 6 characters long");
        }
        if (request.role() == null) {
            throw new ValidationException("User role assignment is required");
        }

        if (userRepository.existsByEmail(request.email().trim())) {
            throw new DuplicateResourceException("Email is already registered: " + request.email());
        }

        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(request.email().toLowerCase().trim())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .active(true)
                .tokenVersion(0)
                .build();

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.isActive(),
                savedUser.getTokenVersion(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt()
        );
    }


    public LoginResponse login(LoginRequest request) {
        //  MANUAL VALIDATION FOR LOGIN (Requirement 7)
        if (request.getEmail() == null || request.getEmail().trim().isBlank()) {
            throw new ValidationException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ValidationException("Password is required");
        }


        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().trim(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail().trim())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found — this should never happen"));

        if (!user.isActive()) {
            // Throws your project's custom ValidationException instead of DisabledException
            throw new ValidationException("Account has been deactivated. Please contact administration.");
        }

        String token = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInMs(jwtService.getExpirationMs())
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public void logout(String token) {
        String jti = jwtService.extractJti(token);
        Instant expiry = jwtService.extractExpiration(token).toInstant();
        tokenBlacklistService.blacklist(jti, expiry);
    }
}