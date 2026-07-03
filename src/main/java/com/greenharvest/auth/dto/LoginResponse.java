package com.greenharvest.auth.dto;

import com.greenharvest.auth.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Deliberately excludes password and internal tokenVersion — only what
 * the frontend needs to attach the token to subsequent requests and
 * render role-based UI.
 */
@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType; // always "Bearer"
    private long expiresInMs;
    private Long userId;
    private String fullName;
    private String email;
    private Role role;
}
