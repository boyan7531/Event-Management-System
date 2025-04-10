package com.softuni.event.model.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationBasicDTOTest {

    @Test
    void testDefaultConstructor() {
        LocationBasicDTO dto = new LocationBasicDTO();
        assertNotNull(dto);
    }
    
    @Test
    void testAllArgsConstructor() {
        // Arrange
        Long id = 1L;
        String name = "Conference Center";
        String address = "123 Main St";
        String city = "New York";
        String country = "USA";
        Integer capacity = 500;
        
        // Act
        LocationBasicDTO dto = new LocationBasicDTO(id, name, address, city, country, capacity);
        
        // Assert
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
        assertEquals(address, dto.getAddress());
        assertEquals(city, dto.getCity());
        assertEquals(country, dto.getCountry());
        assertEquals(capacity, dto.getCapacity());
    }

    @Test
    void testGettersAndSetters() {
        // Arrange
        Long id = 2L;
        String name = "Concert Hall";
        String address = "456 Broadway";
        String city = "London";
        String country = "UK";
        Integer capacity = 1000;

        // Act
        LocationBasicDTO dto = new LocationBasicDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setAddress(address);
        dto.setCity(city);
        dto.setCountry(country);
        dto.setCapacity(capacity);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
        assertEquals(address, dto.getAddress());
        assertEquals(city, dto.getCity());
        assertEquals(country, dto.getCountry());
        assertEquals(capacity, dto.getCapacity());
    }
} 