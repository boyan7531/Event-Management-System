package com.softuni.event.repository;

import com.softuni.event.model.entity.UserEntity;
import com.softuni.event.model.entity.UserRoleEntity;
import com.softuni.event.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = Replace.ANY)
class UserRoleRepositoryTest {

    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private UserEntity user;
    private UserRoleEntity userRole;
    private UserRoleEntity adminRole;
    
    @BeforeEach
    void setUp() {
        // Clean up existing data
        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create a test user
        user = new UserEntity();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);
        
        // Create USER role
        userRole = new UserRoleEntity();
        userRole.setRole(UserRole.USER);
        userRole.setUser(user);
        userRole.setCreatedAt(LocalDateTime.now());
        userRole = userRoleRepository.save(userRole);
        
        // Create ADMIN role
        adminRole = new UserRoleEntity();
        adminRole.setRole(UserRole.ADMIN);
        adminRole.setUser(user);
        adminRole.setCreatedAt(LocalDateTime.now());
        adminRole = userRoleRepository.save(adminRole);
    }
    
    @Test
    void testFindByRole() {
        // Act
        List<UserRoleEntity> userRoles = userRoleRepository.findByRole(UserRole.USER);
        List<UserRoleEntity> adminRoles = userRoleRepository.findByRole(UserRole.ADMIN);
        
        // Assert
        assertEquals(1, userRoles.size());
        assertEquals(userRole.getId(), userRoles.get(0).getId());
        assertEquals(UserRole.USER, userRoles.get(0).getRole());
        assertEquals(user.getId(), userRoles.get(0).getUser().getId());
        
        assertEquals(1, adminRoles.size());
        assertEquals(adminRole.getId(), adminRoles.get(0).getId());
        assertEquals(UserRole.ADMIN, adminRoles.get(0).getRole());
        assertEquals(user.getId(), adminRoles.get(0).getUser().getId());
    }
    
    @Test
    void testFindByRoleWithNonExistentRole() {
        // Act - Looking for roles we know don't exist in the test data
        List<UserRoleEntity> nonExistentRoles = userRoleRepository.findByRole(null);
        
        // Assert
        assertTrue(nonExistentRoles.isEmpty());
    }
    
    @Test
    void testBasicJpaRepositoryMethods() {
        // Test findById
        Optional<UserRoleEntity> foundRole = userRoleRepository.findById(userRole.getId());
        assertTrue(foundRole.isPresent());
        assertEquals(UserRole.USER, foundRole.get().getRole());
        
        // Test findAll
        List<UserRoleEntity> allRoles = userRoleRepository.findAll();
        assertEquals(2, allRoles.size());
        
        // Test update
        UserRoleEntity roleToUpdate = foundRole.get();
        LocalDateTime updatedTime = LocalDateTime.now().plusHours(1);
        roleToUpdate.setUpdatedAt(updatedTime);
        userRoleRepository.save(roleToUpdate);
        
        Optional<UserRoleEntity> updatedRole = userRoleRepository.findById(userRole.getId());
        assertTrue(updatedRole.isPresent());
        assertEquals(updatedTime, updatedRole.get().getUpdatedAt());
        
        // Test delete
        userRoleRepository.delete(roleToUpdate);
        assertEquals(1, userRoleRepository.count());
        assertFalse(userRoleRepository.findById(userRole.getId()).isPresent());
        
        // Verify only the ADMIN role remains
        List<UserRoleEntity> remainingRoles = userRoleRepository.findByRole(UserRole.ADMIN);
        assertEquals(1, remainingRoles.size());
    }
    
    @Test
    void testUserRelationship() {
        // Create another user
        UserEntity anotherUser = new UserEntity();
        anotherUser.setUsername("anotherUser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password");
        anotherUser.setCreatedAt(LocalDateTime.now());
        anotherUser = userRepository.save(anotherUser);
        
        // Create a role for this user
        UserRoleEntity newRole = new UserRoleEntity();
        newRole.setRole(UserRole.USER);
        newRole.setUser(anotherUser);
        newRole.setCreatedAt(LocalDateTime.now());
        newRole = userRoleRepository.save(newRole);
        
        // Verify the role was saved correctly
        Optional<UserRoleEntity> foundRole = userRoleRepository.findById(newRole.getId());
        assertTrue(foundRole.isPresent());
        assertEquals(anotherUser.getId(), foundRole.get().getUser().getId());
        assertEquals(anotherUser.getUsername(), foundRole.get().getUser().getUsername());
        
        // Verify we now have 3 roles in total
        assertEquals(3, userRoleRepository.count());
        
        // Verify we can find roles by user
        List<UserRoleEntity> userRoles = userRoleRepository.findByRole(UserRole.USER);
        assertEquals(2, userRoles.size());
    }
    
    @Test
    void testCreateDuplicateRoles() {
        // Create another USER role for the same user
        UserRoleEntity duplicateRole = new UserRoleEntity();
        duplicateRole.setRole(UserRole.USER); // Same role as existing userRole
        duplicateRole.setUser(user); // Same user
        duplicateRole.setCreatedAt(LocalDateTime.now());
        duplicateRole = userRoleRepository.save(duplicateRole);
        
        // Verify the role was created
        assertNotNull(duplicateRole.getId());
        
        // Verify we now have 3 roles in total
        assertEquals(3, userRoleRepository.count());
        
        // We should now have 2 USER roles
        List<UserRoleEntity> userRoles = userRoleRepository.findByRole(UserRole.USER);
        assertEquals(2, userRoles.size());
        
        // The roles should have different IDs
        assertNotEquals(userRoles.get(0).getId(), userRoles.get(1).getId());
    }
} 