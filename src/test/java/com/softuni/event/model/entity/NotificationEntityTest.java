package com.softuni.event.model.entity;

import com.softuni.event.model.enums.NotificationType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationEntityTest {

    @Test
    void testDefaultConstructor() {
        // Act
        NotificationEntity entity = new NotificationEntity();
        
        // Assert
        assertNotNull(entity);
        assertNotNull(entity.getCreatedAt(), "CreatedAt should be initialized automatically");
        assertTrue(entity.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)), "CreatedAt should be set to current time");
        assertTrue(entity.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(3)), "CreatedAt should be set to current time");
        assertFalse(entity.isRead(), "Read status should be initialized to false");
    }
    
    @Test
    void testParameterizedConstructor() {
        // Arrange
        String message = "New event created";
        NotificationType type = NotificationType.EVENT_CREATED;
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("testuser");
        String link = "/events/1";
        
        // Act
        NotificationEntity entity = new NotificationEntity(message, type, user, link);
        
        // Assert
        assertNotNull(entity);
        assertEquals(message, entity.getMessage());
        assertEquals(type, entity.getType());
        assertEquals(user, entity.getUser());
        assertEquals(link, entity.getLink());
        assertNotNull(entity.getCreatedAt(), "CreatedAt should be initialized automatically");
        assertFalse(entity.isRead(), "Read status should be initialized to false");
    }
    
    @Test
    void testGettersAndSetters() {
        // Arrange
        NotificationEntity entity = new NotificationEntity();
        Long id = 1L;
        String message = "Event approved";
        NotificationType type = NotificationType.EVENT_APPROVED;
        UserEntity user = new UserEntity();
        user.setId(2L);
        user.setUsername("organizer");
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        boolean read = true;
        String link = "/events/2";
        
        // Act
        entity.setId(id);
        entity.setMessage(message);
        entity.setType(type);
        entity.setUser(user);
        entity.setCreatedAt(createdAt);
        entity.setRead(read);
        entity.setLink(link);
        
        // Assert
        assertEquals(id, entity.getId());
        assertEquals(message, entity.getMessage());
        assertEquals(type, entity.getType());
        assertEquals(user, entity.getUser());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(read, entity.isRead());
        assertEquals(link, entity.getLink());
    }
    
    @Test
    void testMethodChaining() {
        // Arrange
        NotificationEntity entity = new NotificationEntity();
        Long id = 1L;
        String message = "Event canceled";
        NotificationType type = NotificationType.EVENT_CANCELED;
        UserEntity user = new UserEntity();
        LocalDateTime createdAt = LocalDateTime.now();
        boolean read = true;
        String link = "/events/3";
        
        // Act
        NotificationEntity returnedEntity = entity
                .setId(id)
                .setMessage(message)
                .setType(type)
                .setUser(user)
                .setCreatedAt(createdAt)
                .setRead(read)
                .setLink(link);
        
        // Assert
        assertSame(entity, returnedEntity, "Method chaining should return the same entity instance");
        assertEquals(id, entity.getId());
        assertEquals(message, entity.getMessage());
        assertEquals(type, entity.getType());
        assertEquals(user, entity.getUser());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(read, entity.isRead());
        assertEquals(link, entity.getLink());
    }
} 