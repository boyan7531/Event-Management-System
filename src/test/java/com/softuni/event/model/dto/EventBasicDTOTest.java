package com.softuni.event.model.dto;

import com.softuni.event.model.enums.EventStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventBasicDTOTest {

    @Test
    void testDefaultConstructor() {
        EventBasicDTO dto = new EventBasicDTO();
        assertNotNull(dto);
    }
    
    @Test
    void testAllArgsConstructor() {
        // Arrange
        Long id = 1L;
        String title = "Test Event";
        LocalDateTime eventDate = LocalDateTime.now();
        boolean isPaid = true;
        BigDecimal ticketPrice = new BigDecimal("25.50");
        EventStatus status = EventStatus.APPROVED;
        LocationBasicDTO location = new LocationBasicDTO();
        
        // Act
        EventBasicDTO dto = new EventBasicDTO(id, title, eventDate, isPaid, ticketPrice, status, location);
        
        // Assert
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(title, dto.getTitle());
        assertEquals(eventDate, dto.getEventDate());
        assertEquals(isPaid, dto.isPaid());
        assertEquals(ticketPrice, dto.getTicketPrice());
        assertEquals(status, dto.getStatus());
        assertEquals(location, dto.getLocation());
    }

    @Test
    void testGettersAndSetters() {
        // Arrange
        Long id = 1L;
        String title = "Test Event";
        LocalDateTime eventDate = LocalDateTime.now();
        boolean isPaid = true;
        BigDecimal ticketPrice = new BigDecimal("25.50");
        EventStatus status = EventStatus.APPROVED;
        LocationBasicDTO location = new LocationBasicDTO();
        location.setId(2L);
        location.setName("Test Venue");

        // Act
        EventBasicDTO dto = new EventBasicDTO();
        dto.setId(id);
        dto.setTitle(title);
        dto.setEventDate(eventDate);
        dto.setPaid(isPaid);
        dto.setTicketPrice(ticketPrice);
        dto.setStatus(status);
        dto.setLocation(location);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(title, dto.getTitle());
        assertEquals(eventDate, dto.getEventDate());
        assertEquals(isPaid, dto.isPaid());
        assertEquals(ticketPrice, dto.getTicketPrice());
        assertEquals(status, dto.getStatus());
        assertEquals(location, dto.getLocation());
        assertEquals(2L, dto.getLocation().getId());
        assertEquals("Test Venue", dto.getLocation().getName());
    }
} 