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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Registers a new system user securely, converting internal dates safely.
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email is already registered: " + request.email());
        }

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
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

    /**
     * Authenticates credentials and issues the custom LoginResponse payload block.
     */

    public LoginResponse login(LoginRequest request) {
        // Delegates to Spring Security's provider, which uses
        // CustomUserDetailsService + BCryptPasswordEncoder under the hood.
        // Throws BadCredentialsException on mismatch — handled centrally
        // by GlobalExceptionHandler as a 401.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
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
