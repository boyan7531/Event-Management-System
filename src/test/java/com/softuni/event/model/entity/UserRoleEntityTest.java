package com.softuni.event.model.entity;

import com.softuni.event.model.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserRoleEntityTest {

    @Test
    void testDefaultConstructor() {
        UserRoleEntity entity = new UserRoleEntity();
        assertNotNull(entity);
    }
    
    @Test
    void testParameterizedConstructor() {
        // Arrange
        UserRole role = UserRole.ADMIN;
        
        // Act
        UserRoleEntity entity = new UserRoleEntity(role);
        
        // Assert
        assertNotNull(entity);
        assertEquals(role, entity.getRole());
    }
    
    @Test
    void testInheritanceFromBaseEntity() {
        UserRoleEntity entity = new UserRoleEntity();
        
        // Setting BaseEntity properties
        Long id = 1L;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        
        entity.setId(id);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);
        
        // Assert BaseEntity properties
        assertEquals(id, entity.getId());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatedAt());
    }
    
    @Test
    void testRoleGetterAndSetter() {
        // Arrange
        UserRoleEntity entity = new UserRoleEntity();
        UserRole role = UserRole.USER;
        
        // Act
        entity.setRole(role);
        
        // Assert
        assertEquals(role, entity.getRole());
        
        // Test with another role value
        UserRole adminRole = UserRole.ADMIN;
        entity.setRole(adminRole);
        assertEquals(adminRole, entity.getRole());
    }
    
    @Test
    void testExplicitRoleGetterAndSetter() {
        // Arrange
        UserRoleEntity entity = new UserRoleEntity();
        UserRole role = UserRole.ADMIN;
        
        // Act - use the explicit setter
        entity.setRole(role);
        
        // Assert - use the explicit getter
        assertEquals(role, entity.getRole());
    }
    
    @Test
    void testUserRelationship() {
        // Arrange
        UserRoleEntity entity = new UserRoleEntity();
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("testuser");
        
        // Act
        entity.setUser(user);
        
        // Assert
        assertNotNull(entity.getUser());
        assertEquals(user.getId(), entity.getUser().getId());
        assertEquals(user.getUsername(), entity.getUser().getUsername());
    }
    
    @Test
    void testAllPropertiesTogether() {
        // Arrange
        UserRoleEntity entity = new UserRoleEntity();
        Long id = 1L;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        UserRole role = UserRole.ADMIN;
        
        UserEntity user = new UserEntity();
        user.setId(2L);
        user.setUsername("adminuser");
        
        // Act
        entity.setId(id);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);
        entity.setRole(role);
        entity.setUser(user);
        
        // Assert
        assertEquals(id, entity.getId());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatedAt());
        assertEquals(role, entity.getRole());
        assertEquals(user, entity.getUser());
    }
} 