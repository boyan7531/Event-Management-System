package com.softuni.event.service.impl;

import com.softuni.event.model.entity.UserEntity;
import com.softuni.event.model.entity.UserRoleEntity;
import com.softuni.event.model.enums.UserRole;
import com.softuni.event.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppUserDetailsService appUserDetailsService;

    private UserEntity testUser;
    private UserRoleEntity userRole;
    private UserRoleEntity adminRole;

    @BeforeEach
    void setUp() {
        // Create user roles
        userRole = new UserRoleEntity();
        userRole.setId(1L);
        userRole.setRole(UserRole.USER);

        adminRole = new UserRoleEntity();
        adminRole.setId(2L);
        adminRole.setRole(UserRole.ADMIN);

        // Create test user
        testUser = new UserEntity();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded_password");
        
        // Add roles to user
        Set<UserRoleEntity> roles = new HashSet<>();
        roles.add(userRole);
        roles.add(adminRole);
        testUser.setRoles(roles);
        
        // Set up bidirectional relationship
        userRole.setUser(testUser);
        adminRole.setUser(testUser);
    }

    @Test
    void loadUserByUsername_ExistingUser_ShouldReturnUserDetails() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = appUserDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encoded_password", userDetails.getPassword());
        assertEquals(2, userDetails.getAuthorities().size());
        
        // Verify authorities
        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        
        assertTrue(authorities.contains("ROLE_USER"));
        assertTrue(authorities.contains("ROLE_ADMIN"));
    }

    @Test
    void loadUserByUsername_NonExistingUser_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("nonexistinguser")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            appUserDetailsService.loadUserByUsername("nonexistinguser");
        });

        // Verify exception message
        String expectedMessage = "User with username nonexistinguser not found";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
    
    @Test
    void loadUserByUsername_UserWithNoRoles_ShouldReturnUserDetailsWithNoAuthorities() {
        // Arrange
        UserEntity userWithNoRoles = new UserEntity();
        userWithNoRoles.setId(2L);
        userWithNoRoles.setUsername("noroles");
        userWithNoRoles.setPassword("encoded_password");
        userWithNoRoles.setRoles(new HashSet<>());
        
        when(userRepository.findByUsername("noroles")).thenReturn(Optional.of(userWithNoRoles));

        // Act
        UserDetails userDetails = appUserDetailsService.loadUserByUsername("noroles");

        // Assert
        assertNotNull(userDetails);
        assertEquals("noroles", userDetails.getUsername());
        assertEquals("encoded_password", userDetails.getPassword());
        assertEquals(0, userDetails.getAuthorities().size());
    }
    
    @Test
    void loadUserByUsername_UserWithOnlyUserRole_ShouldReturnUserDetailsWithUserAuthority() {
        // Arrange
        UserEntity userWithUserRole = new UserEntity();
        userWithUserRole.setId(3L);
        userWithUserRole.setUsername("userrole");
        userWithUserRole.setPassword("encoded_password");
        
        UserRoleEntity onlyUserRole = new UserRoleEntity();
        onlyUserRole.setId(3L);
        onlyUserRole.setRole(UserRole.USER);
        onlyUserRole.setUser(userWithUserRole);
        
        Set<UserRoleEntity> roles = new HashSet<>();
        roles.add(onlyUserRole);
        userWithUserRole.setRoles(roles);
        
        when(userRepository.findByUsername("userrole")).thenReturn(Optional.of(userWithUserRole));

        // Act
        UserDetails userDetails = appUserDetailsService.loadUserByUsername("userrole");

        // Assert
        assertNotNull(userDetails);
        assertEquals("userrole", userDetails.getUsername());
        assertEquals("encoded_password", userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        
        // Verify authority
        String authority = userDetails.getAuthorities().iterator().next().getAuthority();
        assertEquals("ROLE_USER", authority);
    }
    
    @Test
    void loadUserByUsername_UserWithOnlyAdminRole_ShouldReturnUserDetailsWithAdminAuthority() {
        // Arrange
        UserEntity userWithAdminRole = new UserEntity();
        userWithAdminRole.setId(4L);
        userWithAdminRole.setUsername("adminrole");
        userWithAdminRole.setPassword("encoded_password");
        
        UserRoleEntity onlyAdminRole = new UserRoleEntity();
        onlyAdminRole.setId(4L);
        onlyAdminRole.setRole(UserRole.ADMIN);
        onlyAdminRole.setUser(userWithAdminRole);
        
        Set<UserRoleEntity> roles = new HashSet<>();
        roles.add(onlyAdminRole);
        userWithAdminRole.setRoles(roles);
        
        when(userRepository.findByUsername("adminrole")).thenReturn(Optional.of(userWithAdminRole));

        // Act
        UserDetails userDetails = appUserDetailsService.loadUserByUsername("adminrole");

        // Assert
        assertNotNull(userDetails);
        assertEquals("adminrole", userDetails.getUsername());
        assertEquals("encoded_password", userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        
        // Verify authority
        String authority = userDetails.getAuthorities().iterator().next().getAuthority();
        assertEquals("ROLE_ADMIN", authority);
    }
} 