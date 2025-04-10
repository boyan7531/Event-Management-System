package com.softuni.event.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LocationEntityTest {

    @Test
    void testDefaultConstructor() {
        LocationEntity entity = new LocationEntity();
        assertNotNull(entity);
        assertNotNull(entity.getEvents(), "Events set should be initialized");
        assertTrue(entity.getEvents().isEmpty(), "Events set should be empty");
    }
    
    @Test
    void testInheritanceFromBaseEntity() {
        LocationEntity entity = new LocationEntity();
        
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
    void testBasicFieldGettersAndSetters() {
        // Arrange
        LocationEntity entity = new LocationEntity();
        String name = "Convention Center";
        String address = "123 Main Street";
        String city = "New York";
        String country = "USA";
        String zipCode = "10001";
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Integer capacity = 500;
        String description = "Large convention center in downtown";
        
        // Act
        entity.setName(name);
        entity.setAddress(address);
        entity.setCity(city);
        entity.setCountry(country);
        entity.setZipCode(zipCode);
        entity.setLatitude(latitude);
        entity.setLongitude(longitude);
        entity.setCapacity(capacity);
        entity.setDescription(description);
        
        // Assert
        assertEquals(name, entity.getName());
        assertEquals(address, entity.getAddress());
        assertEquals(city, entity.getCity());
        assertEquals(country, entity.getCountry());
        assertEquals(zipCode, entity.getZipCode());
        assertEquals(latitude, entity.getLatitude());
        assertEquals(longitude, entity.getLongitude());
        assertEquals(capacity, entity.getCapacity());
        assertEquals(description, entity.getDescription());
    }
    
    @Test
    void testEventsRelationship() {
        // Arrange
        LocationEntity entity = new LocationEntity();
        
        EventEntity event1 = new EventEntity();
        event1.setId(1L);
        event1.setTitle("Conference");
        
        EventEntity event2 = new EventEntity();
        event2.setId(2L);
        event2.setTitle("Workshop");
        
        Set<EventEntity> events = new HashSet<>();
        events.add(event1);
        events.add(event2);
        
        // Act
        entity.setEvents(events);
        
        // Assert
        assertNotNull(entity.getEvents());
        assertEquals(2, entity.getEvents().size());
        
        // Verify we can add more events
        EventEntity event3 = new EventEntity();
        event3.setId(3L);
        event3.setTitle("Meetup");
        
        entity.getEvents().add(event3);
        assertEquals(3, entity.getEvents().size());
    }
    
    @Test
    void testFieldEdgeCases() {
        // Arrange
        LocationEntity entity = new LocationEntity();
        
        // Act & Assert
        entity.setCapacity(null);
        assertNull(entity.getCapacity(), "Capacity should allow null values");
        
        entity.setLatitude(null);
        assertNull(entity.getLatitude(), "Latitude should allow null values");
        
        entity.setLongitude(null);
        assertNull(entity.getLongitude(), "Longitude should allow null values");
        
        entity.setDescription(null);
        assertNull(entity.getDescription(), "Description should allow null values");
        
        entity.setZipCode(null);
        assertNull(entity.getZipCode(), "ZipCode should allow null values");
    }
} 