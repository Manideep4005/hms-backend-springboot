package com.hms.config;

import com.hms.entity.Role;
import com.hms.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {

            List<String> roles = List.of(
                    "ADMIN",
                    "DOCTOR",
                    "RECEPTIONIST",
                    "PATIENT",
                    "PHARMACIST");

            for (String roleName : roles) {
                roleRepository.findByName(roleName)
                        .orElseGet(() -> roleRepository.save(new Role(roleName)));
            }
        };
    }
}
