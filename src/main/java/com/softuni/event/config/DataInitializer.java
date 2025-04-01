package com.softuni.event.config;

import com.softuni.event.model.entity.UserEntity;
import com.softuni.event.model.entity.UserRoleEntity;
import com.softuni.event.model.enums.UserRole;
import com.softuni.event.repository.UserRepository;
import com.softuni.event.repository.UserRoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeData(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder) {
        
        return args -> {
            // Initialize roles if they don't exist
            if (userRoleRepository.count() == 0) {
                System.out.println("Initializing roles...");
                
                // Create USER role
                UserRoleEntity userRole = new UserRoleEntity();
                userRole.setRole(UserRole.USER);
                
                // Create ADMIN role
                UserRoleEntity adminRole = new UserRoleEntity();
                adminRole.setRole(UserRole.ADMIN);
                
                // User roles will be saved with the users
                System.out.println("Roles initialized!");
            }
            
            // Initialize admin user if no users exist
            if (userRepository.count() == 0) {
                System.out.println("Initializing admin user...");
                
                // Create admin user
                UserEntity adminUser = new UserEntity();
                adminUser.setUsername("admin");
                adminUser.setEmail("admin@example.com");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setFirstName("Admin");
                adminUser.setLastName("User");
                adminUser.setCreatedAt(LocalDateTime.now());
                
                // Initialize collections
                adminUser.setRoles(new HashSet<>());
                adminUser.setOrganizedEvents(new HashSet<>());
                adminUser.setAttendedEvents(new HashSet<>());
                adminUser.setTickets(new HashSet<>());
                adminUser.setPayments(new HashSet<>());
                
                // Save admin user first
                adminUser = userRepository.save(adminUser);
                
                // Create and assign ADMIN role
                UserRoleEntity adminRole = new UserRoleEntity();
                adminRole.setRole(UserRole.ADMIN);
                adminRole.setUser(adminUser);
                
                // Create and assign USER role
                UserRoleEntity userRole = new UserRoleEntity();
                userRole.setRole(UserRole.USER);
                userRole.setUser(adminUser);
                
                // Save roles
                userRoleRepository.save(adminRole);
                userRoleRepository.save(userRole);
                
                // Add roles to user
                adminUser.getRoles().add(adminRole);
                adminUser.getRoles().add(userRole);
                
                // Save user with roles
                userRepository.save(adminUser);
                
                System.out.println("Admin user created with username: admin and password: admin123");
            }
        };
    }
} 