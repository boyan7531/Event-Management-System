package com.softuni.event.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BaseEntityTest {

    // Concrete implementation of the abstract BaseEntity for testing
    private static class TestEntity extends BaseEntity {
        // No additional properties
    }
    
    @Test
    void testIdGetterAndSetter() {
        // Arrange
        TestEntity entity = new TestEntity();
        Long id = 1L;
        
        // Act
        entity.setId(id);
        
        // Assert
        assertEquals(id, entity.getId());
    }
    
    @Test
    void testCreatedAtGetterAndSetter() {
        // Arrange
        TestEntity entity = new TestEntity();
        LocalDateTime createdAt = LocalDateTime.now();
        
        // Act
        entity.setCreatedAt(createdAt);
        
        // Assert
        assertEquals(createdAt, entity.getCreatedAt());
    }
    
    @Test
    void testUpdatedAtGetterAndSetter() {
        // Arrange
        TestEntity entity = new TestEntity();
        LocalDateTime updatedAt = LocalDateTime.now();
        
        // Act
        entity.setUpdatedAt(updatedAt);
        
        // Assert
        assertEquals(updatedAt, entity.getUpdatedAt());
    }
    
    @Test
    void testAllPropertiesTogether() {
        // Arrange
        TestEntity entity = new TestEntity();
        Long id = 1L;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        
        // Act
        entity.setId(id);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);
        
        // Assert
        assertEquals(id, entity.getId());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatedAt());
    }
} 