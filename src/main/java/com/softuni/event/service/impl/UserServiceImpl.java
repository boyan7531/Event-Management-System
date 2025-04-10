package com.softuni.event.service.impl;

import com.softuni.event.model.dto.UserProfileDTO;
import com.softuni.event.model.dto.UserRegisterDTO;
import com.softuni.event.model.entity.UserEntity;
import com.softuni.event.model.entity.UserRoleEntity;
import com.softuni.event.model.enums.UserRole;
import com.softuni.event.repository.UserRepository;
import com.softuni.event.repository.UserRoleRepository;
import com.softuni.event.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserRoleRepository userRoleRepository, 
                          ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void registerUser(UserRegisterDTO userRegisterDTO) {
        // Create user entity
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userRegisterDTO.getUsername());
        userEntity.setEmail(userRegisterDTO.getEmail());
        userEntity.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));
        userEntity.setFirstName(userRegisterDTO.getFirstName());
        userEntity.setLastName(userRegisterDTO.getLastName());
        userEntity.setPhone(userRegisterDTO.getPhone());
        userEntity.setCreatedAt(LocalDateTime.now());
        
        // Initialize collections
        userEntity.setRoles(new HashSet<>());
        userEntity.setOrganizedEvents(new HashSet<>());
        userEntity.setAttendedEvents(new HashSet<>());
        userEntity.setTickets(new HashSet<>());
        userEntity.setPayments(new HashSet<>());
        
        // Save user first
        userEntity = userRepository.save(userEntity);
        
        // Find or create USER role
        List<UserRoleEntity> userRoles = userRoleRepository.findByRole(UserRole.USER);
        UserRoleEntity userRole;
        
        if (userRoles.isEmpty()) {
            // Create new USER role if none exists
            userRole = new UserRoleEntity();
            userRole.setRole(UserRole.USER);
        } else {
            // Use the first USER role found
            userRole = userRoles.get(0);
        }
        
        // Set up bidirectional relationship
        userRole.setUser(userEntity);
        userEntity.getRoles().add(userRole);
        
        // Save role first because it has the foreign key
        userRoleRepository.save(userRole);
        
        // Save the user with updated roles
        userRepository.save(userEntity);
    }

    @Override
    public UserProfileDTO getUserProfile(String username) {
        UserEntity userEntity = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        UserProfileDTO userProfileDTO = modelMapper.map(userEntity, UserProfileDTO.class);
        userProfileDTO.setAdmin(hasAdminRole(userEntity));
        
        return userProfileDTO;
    }

    @Override
    public UserProfileDTO getUserById(Long id) {
        UserEntity userEntity = userRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        UserProfileDTO userProfileDTO = modelMapper.map(userEntity, UserProfileDTO.class);
        userProfileDTO.setAdmin(hasAdminRole(userEntity));
        
        return userProfileDTO;
    }

    @Override
    @Transactional
    public void updateUserProfile(UserProfileDTO userProfileDTO) {
        UserEntity userEntity = userRepository
                .findById(userProfileDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        userEntity.setUsername(userProfileDTO.getUsername());
        userEntity.setEmail(userProfileDTO.getEmail());
        userEntity.setFirstName(userProfileDTO.getFirstName());
        userEntity.setLastName(userProfileDTO.getLastName());
        userEntity.setPhone(userProfileDTO.getPhone());
        userEntity.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(userEntity);
    }

    @Override
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public List<UserProfileDTO> getAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(user -> {
                    UserProfileDTO dto = modelMapper.map(user, UserProfileDTO.class);
                    dto.setAdmin(hasAdminRole(user));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void changeUserRole(Long userId, String role) {
        UserEntity userEntity = userRepository
                .findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        UserRole requestedRole = UserRole.valueOf(role);
        
        // Handle special case for admin role
        if (requestedRole == UserRole.USER) {
            // Request to make user a regular user (remove ADMIN role)
            Optional<UserRoleEntity> adminRole = userEntity.getRoles().stream()
                .filter(r -> r.getRole() == UserRole.ADMIN)
                .findFirst();
            
            if (adminRole.isPresent()) {
                // Don't allow removing admin role from superadmin (ID 1)
                if (userId == 1) {
                    throw new IllegalStateException("Cannot remove ADMIN role from the super administrator");
                }
                
                // Remove ADMIN role
                userEntity.getRoles().remove(adminRole.get());
                userRoleRepository.delete(adminRole.get());
            }
        } else if (requestedRole == UserRole.ADMIN) {
            // Request to make user an admin (add ADMIN role)
            boolean hasAdminRole = userEntity.getRoles().stream()
                .anyMatch(r -> r.getRole() == UserRole.ADMIN);
            
            if (!hasAdminRole) {
                // Add ADMIN role
                UserRoleEntity newRole = new UserRoleEntity();
                newRole.setRole(UserRole.ADMIN);
                newRole.setUser(userEntity);
                newRole = userRoleRepository.save(newRole);
                
                userEntity.getRoles().add(newRole);
            }
        }
        
        userRepository.save(userEntity);
    }
    
    private boolean hasAdminRole(UserEntity userEntity) {
        return userEntity
                .getRoles()
                .stream()
                .anyMatch(role -> role.getRole() == UserRole.ADMIN);
    }
    
    @Override
    public UserEntity getUserByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
    
    @Override
    public List<UserEntity> getAllAdmins() {
        return userRepository
                .findAll()
                .stream()
                .filter(this::hasAdminRole)
                .collect(Collectors.toList());
    }
} 