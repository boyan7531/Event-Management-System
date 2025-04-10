package com.softuni.event.model.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileDTOTest {

    @Test
    void testDefaultConstructor() {
        UserProfileDTO dto = new UserProfileDTO();
        assertNotNull(dto);
    }
    
    @Test
    void testAllArgsConstructor() {
        // Arrange
        Long id = 1L;
        String username = "jsmith";
        String email = "john@example.com";
        String firstName = "John";
        String lastName = "Smith";
        String phone = "+1234567890";
        boolean isAdmin = true;
        
        // Act
        UserProfileDTO dto = new UserProfileDTO(id, username, email, firstName, lastName, phone, isAdmin);
        
        // Assert
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(username, dto.getUsername());
        assertEquals(email, dto.getEmail());
        assertEquals(firstName, dto.getFirstName());
        assertEquals(lastName, dto.getLastName());
        assertEquals(phone, dto.getPhone());
        assertEquals(isAdmin, dto.isAdmin());
    }

    @Test
    void testGettersAndSetters() {
        // Arrange
        Long id = 2L;
        String username = "jdoe";
        String email = "jane@example.com";
        String firstName = "Jane";
        String lastName = "Doe";
        String phone = "+9876543210";
        boolean isAdmin = false;

        // Act
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(id);
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setPhone(phone);
        dto.setAdmin(isAdmin);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(username, dto.getUsername());
        assertEquals(email, dto.getEmail());
        assertEquals(firstName, dto.getFirstName());
        assertEquals(lastName, dto.getLastName());
        assertEquals(phone, dto.getPhone());
        assertEquals(isAdmin, dto.isAdmin());
    }
} 