package com.softuni.event.model.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserBasicDTOTest {

    @Test
    void testDefaultConstructor() {
        UserBasicDTO dto = new UserBasicDTO();
        assertNotNull(dto);
    }
    
    @Test
    void testAllArgsConstructor() {
        // Arrange
        Long id = 1L;
        String username = "jsmith";
        String fullName = "John Smith";
        
        // Act
        UserBasicDTO dto = new UserBasicDTO(id, username, fullName);
        
        // Assert
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(username, dto.getUsername());
        assertEquals(fullName, dto.getFullName());
    }

    @Test
    void testGettersAndSetters() {
        // Arrange
        Long id = 2L;
        String username = "jdoe";
        String fullName = "Jane Doe";

        // Act
        UserBasicDTO dto = new UserBasicDTO();
        dto.setId(id);
        dto.setUsername(username);
        dto.setFullName(fullName);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(username, dto.getUsername());
        assertEquals(fullName, dto.getFullName());
    }
} 