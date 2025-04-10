package com.softuni.event.model.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ContactMessageDTOTest {

    @Test
    void testDefaultConstructor() {
        ContactMessageDTO dto = new ContactMessageDTO();
        assertNotNull(dto);
    }

    @Test
    void testGettersAndSetters() {
        // Arrange
        Long id = 1L;
        String name = "Test Name";
        String email = "test@example.com";
        String subject = "Test Subject";
        String message = "Test Message";
        LocalDateTime createdAt = LocalDateTime.now();
        boolean read = true;

        // Act
        ContactMessageDTO dto = new ContactMessageDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setEmail(email);
        dto.setSubject(subject);
        dto.setMessage(message);
        dto.setCreatedAt(createdAt);
        dto.setRead(read);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
        assertEquals(email, dto.getEmail());
        assertEquals(subject, dto.getSubject());
        assertEquals(message, dto.getMessage());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(read, dto.isRead());
    }
} 