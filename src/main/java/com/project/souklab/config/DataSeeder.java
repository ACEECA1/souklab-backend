package com.project.souklab.config;

import com.project.souklab.dao.RoleRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.Role;
import com.project.souklab.model.User;
import com.project.souklab.util.EmailUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;
    private final EmailUtil emailUtil;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedAdminUser();
    }

    private void seedRoles() {
        List<String> roleNames = List.of("ROLE_ADMIN", "ROLE_ARTISAN", "ROLE_CLIENT");
        for (String name : roleNames) {
            if (roleRepository.findByName(name).isEmpty()) {
                Role role = new Role();
                role.setName(name);
                role.setDescription(name + " role");
                roleRepository.save(role);
            }
        }
    }

    private void seedAdminUser() {
        String defaultEmail = appProperties.getAdmin().getDefaultEmail();
        if (defaultEmail == null || defaultEmail.isBlank()) {
            throw new IllegalStateException("APP_ADMIN_DEFAULT_EMAIL must be configured in the environment");
        }
        String normalizedEmail = defaultEmail.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            return;
        }

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN role not found. Seed roles first."));

        String defaultPassword = appProperties.getAdmin().getDefaultPassword();
        if (defaultPassword == null || defaultPassword.isBlank()) {
            throw new IllegalStateException("APP_ADMIN_DEFAULT_PASSWORD must be configured in the environment");
        }

        User admin = User.builder()
                .email(normalizedEmail)
                .password(passwordEncoder.encode(defaultPassword))
                .firstName("System")
                .lastName("Administrator")
                .status(AccountStatus.ACTIVE)
                .emailVerified(true)
                .emailVerifiedAt(LocalDateTime.now())
                .roles(new HashSet<>(Set.of(adminRole)))
                .build();

        userRepository.save(admin);

        try {
            emailUtil.sendAdminWelcomeEmail(normalizedEmail, defaultPassword);
        } catch (Exception e) {
            log.error("Failed to send first-boot administrator welcome email to {}", normalizedEmail, e);
        }
    }
}
