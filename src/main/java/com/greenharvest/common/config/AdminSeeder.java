package com.greenharvest.common.config;

import com.greenharvest.auth.enums.Role;
import com.greenharvest.user.model.User;
import com.greenharvest.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.full-name:Maxwell Mwaura}")
    private String adminFullName;

    @Value("${app.admin.email:maxwell@greenharvest.com}")
    private String adminEmail;

    @Value("${app.admin.password:SecurePassword123}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        String normalizedEmail = adminEmail.toLowerCase().trim();

        boolean adminExists = userRepository.existsByRole(Role.ADMINISTRATOR);

        if (!adminExists) {
            log.info("No ADMINISTRATOR user found in database. Seeding default Admin user: {}", normalizedEmail);

            User adminUser = User.builder()
                    .fullName(adminFullName)
                    .email(normalizedEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMINISTRATOR)
                    .active(true)
                    .tokenVersion(0)
                    .build();

            userRepository.save(adminUser);
            log.info("Default ADMINISTRATOR user successfully created.");
        } else {
            log.info("ADMINISTRATOR user already exists in database. Skipping Admin seeding.");
        }
    }
}