package com.greenharvest.user.model;

import com.greenharvest.auth.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * The User entity is intentionally NOT returned directly from any controller.
 * It carries the password hash and a tokenVersion used to invalidate
 * outstanding JWTs on role change (see Challenge D) — both of which must
 * never leave the backend. Controllers only ever see UserResponse DTOs.
 */
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** BCrypt hash. Never serialized in any DTO. */
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * Bumped whenever an admin changes this user's role or deactivates the
     * account. The JWT carries the tokenVersion at time of login; the
     * filter rejects any token whose version doesn't match the current
     * value, effectively killing already-issued tokens without needing a
     * blacklist table for this specific case. See Challenge D.
     */
    @Column(nullable = false)
    @Builder.Default
    private int tokenVersion = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
