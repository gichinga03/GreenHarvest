package com.greenharvest.auth.api.controller; // 1. Update this package declaration

// 2. Fix these imports to point to your new 'api.dto' folder structure
import com.greenharvest.auth.api.dto.AuthResponse;
import com.greenharvest.auth.api.dto.LoginRequest;
import com.greenharvest.auth.api.dto.RegisterRequest;
import com.greenharvest.auth.api.dto.VerifyTokenRequest;

import com.greenharvest.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Step 1: Submit email to drop token into user inbox
    @PostMapping("/login/request")
    public ResponseEntity<String> requestLoginToken(@Valid @RequestBody LoginRequest request) {
        String message = authService.initiateLogin(request);
        return ResponseEntity.ok(message);
    }

    // Step 2: Submit email + token to secure final Bearer authorization string
    @PostMapping("/login/verify")
    public ResponseEntity<AuthResponse> verifyAndLogin(@Valid @RequestBody VerifyTokenRequest request) {
        AuthResponse response = authService.verifyAndGenerateBearerToken(request);
        return ResponseEntity.ok(response);
    }
}