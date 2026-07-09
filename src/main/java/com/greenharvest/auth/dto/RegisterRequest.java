package com.greenharvest.auth.dto;

import com.greenharvest.auth.enums.Role;

public record RegisterRequest(
        String fullName,
        String email,
        String password,
        Role role
) {}