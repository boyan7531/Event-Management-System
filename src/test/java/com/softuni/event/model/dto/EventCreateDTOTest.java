package com.softuni.event.model.dto;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventCreateDTOTest {

    @Test
    void testDefaultConstructor() {
        EventCreateDTO dto = new EventCreateDTO();
        assertNotNull(dto);
    }
    
    @Test
    void testAllArgsConstructor() {
        // Arrange
        String title = "Test Event";
        String description = "Test Event Description";
        LocalDateTime eventDate = LocalDateTime.now().plusDays(1);
        LocalDateTime registrationDeadline = LocalDateTime.now().plusHours(12);
        boolean paid = true;
        BigDecimal ticketPrice = new BigDecimal("25.50");
        Integer availableTickets = 100;
        Long locationId = 1L;
        
        // Act
        EventCreateDTO dto = new EventCreateDTO(
                title, 
                description, 
                eventDate, 
                registrationDeadline, 
                paid, 
                ticketPrice, 
                availableTickets, 
                locationId
        );
        
        // Assert
        assertNotNull(dto);
        assertEquals(title, dto.getTitle());
        assertEquals(description, dto.getDescription());
        assertEquals(eventDate, dto.getEventDate());
        assertEquals(registrationDeadline, dto.getRegistrationDeadline());
        assertEquals(paid, dto.isPaid());
        assertEquals(ticketPrice, dto.getTicketPrice());
        assertEquals(availableTickets, dto.getAvailableTickets());
        assertEquals(locationId, dto.getLocationId());
    }

    @Test
    void testGettersAndSetters() {
        // Arrange
        String title = "Test Event";
        String description = "Test Event Description";
        LocalDateTime eventDate = LocalDateTime.now().plusDays(1);
        LocalDateTime registrationDeadline = LocalDateTime.now().plusHours(12);
        boolean paid = true;
        BigDecimal ticketPrice = new BigDecimal("25.50");
        Integer availableTickets = 100;
        Long locationId = 1L;

        // Act
        EventCreateDTO dto = new EventCreateDTO();
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setEventDate(eventDate);
        dto.setRegistrationDeadline(registrationDeadline);
        dto.setPaid(paid);
        dto.setTicketPrice(ticketPrice);
        dto.setAvailableTickets(availableTickets);
        dto.setLocationId(locationId);

        // Assert
        assertEquals(title, dto.getTitle());
        assertEquals(description, dto.getDescription());
        assertEquals(eventDate, dto.getEventDate());
        assertEquals(registrationDeadline, dto.getRegistrationDeadline());
        assertEquals(paid, dto.isPaid());
        assertEquals(ticketPrice, dto.getTicketPrice());
        assertEquals(availableTickets, dto.getAvailableTickets());
        assertEquals(locationId, dto.getLocationId());
    }
} 