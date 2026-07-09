package com.greenharvest.user.api.controller;

import com.greenharvest.user.api.dto.UpdateUserRoleRequest;
import com.greenharvest.user.api.dto.UserResponse;
import com.greenharvest.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMINISTRATOR')") // Admin-only protection for systemic user management
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable Long id,
            @RequestBody UpdateUserRoleRequest request) {
        UserResponse updatedUser = userService.updateUserRole(id, request.role());
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        UserResponse updatedUser = userService.toggleUserStatus(id, active);
        return ResponseEntity.ok(updatedUser);
    }
}