package com.greenharvest.auth.repository;

import com.greenharvest.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Finds a user by their unique email during login workflows
    Optional<User> findByEmail(String email);

    // Used to prevent creating duplicate accounts with the same email
    boolean existsByEmail(String email);
}