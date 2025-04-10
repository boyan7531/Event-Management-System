package com.softuni.event.model.entity;

import com.softuni.event.model.enums.EventStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EventEntityTest {

    @Test
    void testDefaultConstructor() {
        EventEntity entity = new EventEntity();
        assertNotNull(entity);
        assertNotNull(entity.getAttendees(), "Attendees set should be initialized");
        assertTrue(entity.getAttendees().isEmpty(), "Attendees set should be empty");
        assertNotNull(entity.getTickets(), "Tickets set should be initialized");
        assertTrue(entity.getTickets().isEmpty(), "Tickets set should be empty");
    }
    
    @Test
    void testInheritanceFromBaseEntity() {
        EventEntity entity = new EventEntity();
        
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
        EventEntity entity = new EventEntity();
        String title = "Conference Event";
        String description = "A detailed description of the conference";
        LocalDateTime eventDate = LocalDateTime.now().plusDays(30);
        LocalDateTime registrationDeadline = LocalDateTime.now().plusDays(15);
        boolean isPaid = true;
        BigDecimal ticketPrice = new BigDecimal("99.99");
        Integer availableTickets = 100;
        EventStatus status = EventStatus.APPROVED;
        
        // Act
        entity.setTitle(title);
        entity.setDescription(description);
        entity.setEventDate(eventDate);
        entity.setRegistrationDeadline(registrationDeadline);
        entity.setPaid(isPaid);
        entity.setTicketPrice(ticketPrice);
        entity.setAvailableTickets(availableTickets);
        entity.setStatus(status);
        
        // Assert
        assertEquals(title, entity.getTitle());
        assertEquals(description, entity.getDescription());
        assertEquals(eventDate, entity.getEventDate());
        assertEquals(registrationDeadline, entity.getRegistrationDeadline());
        assertEquals(isPaid, entity.isPaid());
        assertEquals(ticketPrice, entity.getTicketPrice());
        assertEquals(availableTickets, entity.getAvailableTickets());
        assertEquals(status, entity.getStatus());
    }
    
    @Test
    void testOrganizerRelationship() {
        // Arrange
        EventEntity entity = new EventEntity();
        UserEntity organizer = new UserEntity();
        organizer.setId(1L);
        organizer.setUsername("organizer");
        
        // Act
        entity.setOrganizer(organizer);
        
        // Assert
        assertNotNull(entity.getOrganizer());
        assertEquals(organizer.getId(), entity.getOrganizer().getId());
        assertEquals(organizer.getUsername(), entity.getOrganizer().getUsername());
    }
    
    @Test
    void testLocationRelationship() {
        // Arrange
        EventEntity entity = new EventEntity();
        LocationEntity location = new LocationEntity();
        location.setId(1L);
        location.setName("Conference Center");
        
        // Act
        entity.setLocation(location);
        
        // Assert
        assertNotNull(entity.getLocation());
        assertEquals(location.getId(), entity.getLocation().getId());
        assertEquals(location.getName(), entity.getLocation().getName());
    }
    
    @Test
    void testAttendeesRelationship() {
        // Arrange
        EventEntity entity = new EventEntity();
        
        UserEntity attendee1 = new UserEntity();
        attendee1.setId(1L);
        attendee1.setUsername("user1");
        
        UserEntity attendee2 = new UserEntity();
        attendee2.setId(2L);
        attendee2.setUsername("user2");
        
        Set<UserEntity> attendees = new HashSet<>();
        attendees.add(attendee1);
        attendees.add(attendee2);
        
        // Act
        entity.setAttendees(attendees);
        
        // Assert
        assertNotNull(entity.getAttendees());
        assertEquals(2, entity.getAttendees().size());
        
        // Verify we can add more attendees
        UserEntity attendee3 = new UserEntity();
        attendee3.setId(3L);
        attendee3.setUsername("user3");
        
        entity.getAttendees().add(attendee3);
        assertEquals(3, entity.getAttendees().size());
    }
    
    @Test
    void testTicketsRelationship() {
        // Arrange
        EventEntity entity = new EventEntity();
        
        TicketEntity ticket1 = new TicketEntity();
        ticket1.setId(1L);
        ticket1.setTicketNumber("TICKET-001");
        
        TicketEntity ticket2 = new TicketEntity();
        ticket2.setId(2L);
        ticket2.setTicketNumber("TICKET-002");
        
        Set<TicketEntity> tickets = new HashSet<>();
        tickets.add(ticket1);
        tickets.add(ticket2);
        
        // Act
        entity.setTickets(tickets);
        
        // Assert
        assertNotNull(entity.getTickets());
        assertEquals(2, entity.getTickets().size());
        
        // Verify we can add more tickets
        TicketEntity ticket3 = new TicketEntity();
        ticket3.setId(3L);
        ticket3.setTicketNumber("TICKET-003");
        
        entity.getTickets().add(ticket3);
        assertEquals(3, entity.getTickets().size());
    }
} 