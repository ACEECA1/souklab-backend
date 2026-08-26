package com.project.souklab.config;

import com.project.souklab.dao.RoleRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.Role;
import com.project.souklab.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        if (userRepository.count() == 0) {
            seedAdminUser();
        }
    }

    private void seedRoles() {
        List<String> roleNames = List.of("ROLE_ADMIN", "ROLE_ARTISAN", "ROLE_CLIENT", "ADMIN", "ARTISAN", "CLIENT");
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
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .or(() -> roleRepository.findByName("ADMIN"))
                .orElseThrow();

        String defaultPassword = appProperties.getAdmin().getDefaultPassword();
        if (defaultPassword == null || defaultPassword.isBlank()) {
            throw new IllegalStateException("APP_ADMIN_DEFAULT_PASSWORD must be configured in the environment");
        }

        User admin = User.builder()
                .email("admin@souklab.dz")
                .password(passwordEncoder.encode(defaultPassword))
                .firstName("System")
                .lastName("Administrator")
                .status(AccountStatus.ACTIVE)
                .emailVerified(true)
                .emailVerifiedAt(LocalDateTime.now())
                .roles(new HashSet<>(Set.of(adminRole)))
                .build();

        userRepository.save(admin);
    }
}
