package com.greenharvest.auth.dto;

import com.greenharvest.auth.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String tokenType;
    private long expiresInMs;
    private Long userId;
    private String fullName;
    private String email;
    private Role role;
}