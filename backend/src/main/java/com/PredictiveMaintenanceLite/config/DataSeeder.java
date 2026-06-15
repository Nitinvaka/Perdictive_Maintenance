package com.PredictiveMaintenanceLite.config;

import com.PredictiveMaintenanceLite.entity.User;
import com.PredictiveMaintenanceLite.enums.Role;
import com.PredictiveMaintenanceLite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds default users on startup when they are not already in the database.
 * Safe to run on every restart — existing usernames are skipped.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUser("Admin", "admin@pml.com", "Admin@1234", Role.ROLE_ADMIN);
        seedUser("Manager", "manager@pml.com", "Manager@1234", Role.ROLE_MANAGER);
        seedUser("Technician", "technician@pml.com", "Tech@1234", Role.ROLE_TECHNICIAN);
    }

    private void seedUser(String username, String email, String rawPassword, Role role) {
        if (userRepository.existsByUsername(username)) {
            log.debug("Seed user '{}' already exists — skipping", username);
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setActive(true);
        userRepository.save(user);
        log.info("Seeded user: {} ({})", username, role);
    }
}
