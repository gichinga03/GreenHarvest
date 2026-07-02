package com.greenharvest.auth.service;

// 1. Core Auth API DTO imports (reflecting your new consolidated folder structure)
import com.greenharvest.auth.api.dto.AuthResponse;
import com.greenharvest.auth.api.dto.LoginRequest;
import com.greenharvest.auth.api.dto.RegisterRequest;
import com.greenharvest.auth.api.dto.VerifyTokenRequest;

// 2. Auth Core Engine Model, Repository, and Component imports
import com.greenharvest.auth.model.User;
import com.greenharvest.auth.model.VerificationToken;
import com.greenharvest.auth.repository.UserRepository;
import com.greenharvest.auth.repository.VerificationTokenRepository;

// 3. Make sure this points precisely to where your JwtService and EmailService are located
import com.greenharvest.auth.service.JwtService;
import com.greenharvest.auth.service.EmailService;

// Spring & Standard library imports
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       VerificationTokenRepository tokenRepository,
                       JwtService jwtService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    @Transactional
    public String initiateLogin(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Account not found."));

        if (!user.isActive()) {
            throw new IllegalStateException("Your account has been deactivated.");
        }

        // Clean out any old outstanding tokens for this user
        tokenRepository.deleteByUser(user);

        // Generate a high-entropy 6-digit numeric token (or a clean UUID string)
        String rawToken = String.format("%06d", new Random().nextInt(999999));

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(rawToken);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(5)); // Valid for 5 mins

        tokenRepository.save(verificationToken);

        // Fire off email dispatch
        emailService.sendVerificationEmail(user.getEmail(), rawToken);

        return "Verification code has been dispatched to your email.";
    }

    @Transactional
    public AuthResponse verifyAndGenerateBearerToken(VerifyTokenRequest request) {
        VerificationToken verificationToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired verification token."));

        if (!verificationToken.getUser().getEmail().equalsIgnoreCase(request.getEmail())) {
            throw new BadCredentialsException("Token mismatch for requested email target.");
        }

        if (verificationToken.isExpired()) {
            tokenRepository.delete(verificationToken);
            throw new BadCredentialsException("This token has expired. Please request a new code.");
        }

        User user = verificationToken.getUser();

        // Issue real Authorization Bearer JWT
        String bearerJwt = jwtService.generateToken(user);

        // Burn the single-use token immediately
        tokenRepository.delete(verificationToken);

        return new AuthResponse(bearerJwt, user.getEmail(), user.getRole().name());
    }
}