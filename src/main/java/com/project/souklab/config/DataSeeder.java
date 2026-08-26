package com.project.souklab.config;

import lombok.RequiredArgsConstructor;
import com.project.souklab.dao.RoleRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.model.Role;
import com.project.souklab.model.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Value("${app.admin.default-password:admin123}")
    private String adminDefaultPassword;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        if (userRepository.count() == 0) {
            seedAdminUser();
        }
    }

    private void seedRoles() {
        List<String> roleNames = List.of("ADMIN", "ARTISAN", "CLIENT");
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
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode(adminDefaultPassword));
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setStatus(User.UserStatus.ACTIVE);
        
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        admin.setRoles(roles);

        userRepository.save(admin);
    }
}
