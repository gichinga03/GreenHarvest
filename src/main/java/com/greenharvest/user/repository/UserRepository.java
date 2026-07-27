package com.greenharvest.user.repository;

import com.greenharvest.auth.enums.Role;
import com.greenharvest.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByRole(Role role);
}
