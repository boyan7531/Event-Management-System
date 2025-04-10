package com.softuni.event.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ContactMessageEntityTest {

    @Test
    void testDefaultConstructor() {
        ContactMessageEntity entity = new ContactMessageEntity();
        assertNotNull(entity);
        assertFalse(entity.isRead(), "Default read status should be false");
    }
    
    @Test
    void testParameterizedConstructor() {
        // Arrange
        String name = "John Doe";
        String email = "john@example.com";
        String subject = "Test Subject";
        String message = "Test message content";
        
        // Act
        ContactMessageEntity entity = new ContactMessageEntity(name, email, subject, message);
        
        // Assert
        assertNotNull(entity);
        assertEquals(name, entity.getName());
        assertEquals(email, entity.getEmail());
        assertEquals(subject, entity.getSubject());
        assertEquals(message, entity.getMessage());
        assertNotNull(entity.getCreatedAt(), "Created timestamp should be set automatically");
        assertFalse(entity.isRead(), "Default read status should be false");
    }
    
    @Test
    void testIdGetterAndSetter() {
        // Arrange
        ContactMessageEntity entity = new ContactMessageEntity();
        Long id = 1L;
        
        // Act
        entity.setId(id);
        
        // Assert
        assertEquals(id, entity.getId());
    }
    
    @Test
    void testNameGetterAndSetter() {
        // Arrange
        ContactMessageEntity entity = new ContactMessageEntity();
        String name = "Jane Smith";
        
        // Act
        entity.setName(name);
        
        // Assert
        assertEquals(name, entity.getName());
    }
    
    @Test
    void testEmailGetterAndSetter() {
        // Arrange
        ContactMessageEntity entity = new ContactMessageEntity();
        String email = "jane@example.com";
        
        // Act
        entity.setEmail(email);
        
        // Assert
        assertEquals(email, entity.getEmail());
    }
    
    @Test
    void testSubjectGetterAndSetter() {
        // Arrange
        ContactMessageEntity entity = new ContactMessageEntity();
        String subject = "Inquiry";
        
        // Act
        entity.setSubject(subject);
        
        // Assert
        assertEquals(subject, entity.getSubject());
    }
    
    @Test
    void testMessageGetterAndSetter() {
        // Arrange
        ContactMessageEntity entity = new ContactMessageEntity();
        String message = "This is a detailed message about my inquiry.";
        
        // Act
        entity.setMessage(message);
        
        // Assert
        assertEquals(message, entity.getMessage());
    }
    
    @Test
    void testCreatedAtGetterAndSetter() {
        // Arrange
        ContactMessageEntity entity = new ContactMessageEntity();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        
        // Act
        entity.setCreatedAt(createdAt);
        
        // Assert
        assertEquals(createdAt, entity.getCreatedAt());
    }
    
    @Test
    void testReadGetterAndSetter() {
        // Arrange
        ContactMessageEntity entity = new ContactMessageEntity();
        
        // Act & Assert - Check default
        assertFalse(entity.isRead(), "Default read status should be false");
        
        // Act - Change value
        entity.setRead(true);
        
        // Assert
        assertTrue(entity.isRead());
    }
    
    @Test
    void testAllPropertiesTogether() {
        // Arrange
        ContactMessageEntity entity = new ContactMessageEntity();
        Long id = 1L;
        String name = "Test Name";
        String email = "test@example.com";
        String subject = "Test Subject";
        String message = "Test Message";
        LocalDateTime createdAt = LocalDateTime.now();
        boolean read = true;
        
        // Act
        entity.setId(id);
        entity.setName(name);
        entity.setEmail(email);
        entity.setSubject(subject);
        entity.setMessage(message);
        entity.setCreatedAt(createdAt);
        entity.setRead(read);
        
        // Assert
        assertEquals(id, entity.getId());
        assertEquals(name, entity.getName());
        assertEquals(email, entity.getEmail());
        assertEquals(subject, entity.getSubject());
        assertEquals(message, entity.getMessage());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(read, entity.isRead());
    }
} 