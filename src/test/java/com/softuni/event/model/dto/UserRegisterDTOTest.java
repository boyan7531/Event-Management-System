package com.softuni.event.model.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRegisterDTOTest {

    @Test
    void testDefaultConstructor() {
        UserRegisterDTO dto = new UserRegisterDTO();
        assertNotNull(dto);
    }
    
    @Test
    void testAllArgsConstructor() {
        // Arrange
        String username = "jsmith";
        String email = "john@example.com";
        String password = "password123";
        String confirmPassword = "password123";
        String firstName = "John";
        String lastName = "Smith";
        String phone = "+1234567890";
        
        // Act
        UserRegisterDTO dto = new UserRegisterDTO(username, email, password, confirmPassword, firstName, lastName, phone);
        
        // Assert
        assertNotNull(dto);
        assertEquals(username, dto.getUsername());
        assertEquals(email, dto.getEmail());
        assertEquals(password, dto.getPassword());
        assertEquals(confirmPassword, dto.getConfirmPassword());
        assertEquals(firstName, dto.getFirstName());
        assertEquals(lastName, dto.getLastName());
        assertEquals(phone, dto.getPhone());
    }

    @Test
    void testGettersAndSetters() {
        // Arrange
        String username = "jdoe";
        String email = "jane@example.com";
        String password = "securePass456";
        String confirmPassword = "securePass456";
        String firstName = "Jane";
        String lastName = "Doe";
        String phone = "+9876543210";

        // Act
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setConfirmPassword(confirmPassword);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setPhone(phone);

        // Assert
        assertEquals(username, dto.getUsername());
        assertEquals(email, dto.getEmail());
        assertEquals(password, dto.getPassword());
        assertEquals(confirmPassword, dto.getConfirmPassword());
        assertEquals(firstName, dto.getFirstName());
        assertEquals(lastName, dto.getLastName());
        assertEquals(phone, dto.getPhone());
    }
    
    @Test
    void testPasswordsMatch_WhenMatching_ReturnsTrue() {
        // Arrange
        UserRegisterDTO dto = new UserRegisterDTO();
        String password = "securePassword123";
        
        // Act
        dto.setPassword(password);
        dto.setConfirmPassword(password);
        
        // Assert
        assertTrue(dto.passwordsMatch());
    }
    
    @Test
    void testPasswordsMatch_WhenNotMatching_ReturnsFalse() {
        // Arrange
        UserRegisterDTO dto = new UserRegisterDTO();
        
        // Act
        dto.setPassword("password123");
        dto.setConfirmPassword("differentPassword");
        
        // Assert
        assertFalse(dto.passwordsMatch());
    }
    
    @Test
    void testPasswordsMatch_WhenPasswordIsNull_ReturnsFalse() {
        // Arrange
        UserRegisterDTO dto = new UserRegisterDTO();
        
        // Act
        dto.setPassword(null);
        dto.setConfirmPassword("password123");
        
        // Assert
        assertFalse(dto.passwordsMatch());
    }
} 