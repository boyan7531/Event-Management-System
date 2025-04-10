package com.softuni.event.model.dto;

import com.softuni.event.model.enums.EventStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EventDetailDTOTest {

    @Test
    void testDefaultConstructor() {
        EventDetailDTO dto = new EventDetailDTO();
        assertNotNull(dto);
    }
    
    @Test
    void testAllArgsConstructor() {
        // Arrange
        Long id = 1L;
        String title = "Test Event";
        String description = "Test Event Description";
        LocalDateTime eventDate = LocalDateTime.now().plusDays(1);
        LocalDateTime registrationDeadline = LocalDateTime.now().plusHours(12);
        boolean isPaid = true;
        BigDecimal ticketPrice = new BigDecimal("25.50");
        Integer availableTickets = 100;
        EventStatus status = EventStatus.APPROVED;
        
        UserBasicDTO organizer = new UserBasicDTO();
        organizer.setId(10L);
        organizer.setUsername("organizer");
        
        LocationBasicDTO location = new LocationBasicDTO();
        location.setId(20L);
        location.setName("Test Venue");
        
        UserBasicDTO attendee1 = new UserBasicDTO();
        attendee1.setId(11L);
        attendee1.setUsername("attendee1");
        
        UserBasicDTO attendee2 = new UserBasicDTO();
        attendee2.setId(12L);
        attendee2.setUsername("attendee2");
        
        Set<UserBasicDTO> attendees = new HashSet<>();
        attendees.add(attendee1);
        attendees.add(attendee2);
        
        int totalAttendees = 2;
        boolean joinedByCurrentUser = false;
        String weatherForecast = "Sunny, 25°C";
        
        // Act
        EventDetailDTO dto = new EventDetailDTO(
                id, title, description, eventDate, registrationDeadline, 
                isPaid, ticketPrice, availableTickets, status, organizer, 
                location, attendees, totalAttendees, joinedByCurrentUser, weatherForecast
        );
        
        // Assert
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(title, dto.getTitle());
        assertEquals(description, dto.getDescription());
        assertEquals(eventDate, dto.getEventDate());
        assertEquals(registrationDeadline, dto.getRegistrationDeadline());
        assertEquals(isPaid, dto.isPaid());
        assertEquals(ticketPrice, dto.getTicketPrice());
        assertEquals(availableTickets, dto.getAvailableTickets());
        assertEquals(status, dto.getStatus());
        
        assertNotNull(dto.getOrganizer());
        assertEquals(10L, dto.getOrganizer().getId());
        assertEquals("organizer", dto.getOrganizer().getUsername());
        
        assertNotNull(dto.getLocation());
        assertEquals(20L, dto.getLocation().getId());
        assertEquals("Test Venue", dto.getLocation().getName());
        
        assertNotNull(dto.getAttendees());
        assertEquals(2, dto.getAttendees().size());
        
        assertEquals(totalAttendees, dto.getTotalAttendees());
        assertEquals(joinedByCurrentUser, dto.isJoinedByCurrentUser());
        assertEquals(weatherForecast, dto.getWeatherForecast());
    }

    @Test
    void testGettersAndSetters() {
        // Arrange
        Long id = 1L;
        String title = "Test Event";
        String description = "Test Event Description";
        LocalDateTime eventDate = LocalDateTime.now().plusDays(1);
        LocalDateTime registrationDeadline = LocalDateTime.now().plusHours(12);
        boolean isPaid = true;
        BigDecimal ticketPrice = new BigDecimal("25.50");
        Integer availableTickets = 100;
        EventStatus status = EventStatus.APPROVED;
        
        UserBasicDTO organizer = new UserBasicDTO();
        organizer.setId(10L);
        organizer.setUsername("organizer");
        
        LocationBasicDTO location = new LocationBasicDTO();
        location.setId(20L);
        location.setName("Test Venue");
        
        UserBasicDTO attendee1 = new UserBasicDTO();
        attendee1.setId(11L);
        attendee1.setUsername("attendee1");
        
        UserBasicDTO attendee2 = new UserBasicDTO();
        attendee2.setId(12L);
        attendee2.setUsername("attendee2");
        
        Set<UserBasicDTO> attendees = new HashSet<>();
        attendees.add(attendee1);
        attendees.add(attendee2);
        
        int totalAttendees = 2;
        boolean joinedByCurrentUser = false;
        String weatherForecast = "Sunny, 25°C";

        // Act
        EventDetailDTO dto = new EventDetailDTO();
        dto.setId(id);
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setEventDate(eventDate);
        dto.setRegistrationDeadline(registrationDeadline);
        dto.setPaid(isPaid);
        dto.setTicketPrice(ticketPrice);
        dto.setAvailableTickets(availableTickets);
        dto.setStatus(status);
        dto.setOrganizer(organizer);
        dto.setLocation(location);
        dto.setAttendees(attendees);
        dto.setTotalAttendees(totalAttendees);
        dto.setJoinedByCurrentUser(joinedByCurrentUser);
        dto.setWeatherForecast(weatherForecast);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(title, dto.getTitle());
        assertEquals(description, dto.getDescription());
        assertEquals(eventDate, dto.getEventDate());
        assertEquals(registrationDeadline, dto.getRegistrationDeadline());
        assertEquals(isPaid, dto.isPaid());
        assertEquals(ticketPrice, dto.getTicketPrice());
        assertEquals(availableTickets, dto.getAvailableTickets());
        assertEquals(status, dto.getStatus());
        
        assertNotNull(dto.getOrganizer());
        assertEquals(10L, dto.getOrganizer().getId());
        assertEquals("organizer", dto.getOrganizer().getUsername());
        
        assertNotNull(dto.getLocation());
        assertEquals(20L, dto.getLocation().getId());
        assertEquals("Test Venue", dto.getLocation().getName());
        
        assertNotNull(dto.getAttendees());
        assertEquals(2, dto.getAttendees().size());
        assertTrue(dto.getAttendees().contains(attendee1));
        assertTrue(dto.getAttendees().contains(attendee2));
        
        assertEquals(totalAttendees, dto.getTotalAttendees());
        assertEquals(joinedByCurrentUser, dto.isJoinedByCurrentUser());
        assertEquals(weatherForecast, dto.getWeatherForecast());
    }
} 