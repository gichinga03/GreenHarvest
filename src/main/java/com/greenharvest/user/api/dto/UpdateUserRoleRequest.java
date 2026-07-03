package com.greenharvest.user.api.dto;

import com.greenharvest.auth.enums.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull(message = "Role is required")
        Role role
) {}