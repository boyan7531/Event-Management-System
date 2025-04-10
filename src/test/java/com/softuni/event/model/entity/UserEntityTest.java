package com.softuni.event.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {

    @Test
    void testDefaultConstructor() {
        UserEntity entity = new UserEntity();
        assertNotNull(entity);
        assertNotNull(entity.getRoles(), "Roles set should be initialized");
        assertTrue(entity.getRoles().isEmpty(), "Roles set should be empty");
        assertNotNull(entity.getOrganizedEvents(), "OrganizedEvents set should be initialized");
        assertTrue(entity.getOrganizedEvents().isEmpty(), "OrganizedEvents set should be empty");
        assertNotNull(entity.getAttendedEvents(), "AttendedEvents set should be initialized");
        assertTrue(entity.getAttendedEvents().isEmpty(), "AttendedEvents set should be empty");
        assertNotNull(entity.getTickets(), "Tickets set should be initialized");
        assertTrue(entity.getTickets().isEmpty(), "Tickets set should be empty");
        assertNotNull(entity.getPayments(), "Payments set should be initialized");
        assertTrue(entity.getPayments().isEmpty(), "Payments set should be empty");
    }
    
    @Test
    void testInheritanceFromBaseEntity() {
        UserEntity entity = new UserEntity();
        
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
        UserEntity entity = new UserEntity();
        String username = "jsmith";
        String email = "john@example.com";
        String password = "password123";
        String firstName = "John";
        String lastName = "Smith";
        String phone = "+1234567890";
        
        // Act
        entity.setUsername(username);
        entity.setEmail(email);
        entity.setPassword(password);
        entity.setFirstName(firstName);
        entity.setLastName(lastName);
        entity.setPhone(phone);
        
        // Assert
        assertEquals(username, entity.getUsername());
        assertEquals(email, entity.getEmail());
        assertEquals(password, entity.getPassword());
        assertEquals(firstName, entity.getFirstName());
        assertEquals(lastName, entity.getLastName());
        assertEquals(phone, entity.getPhone());
    }
    
    @Test
    void testRolesRelationship() {
        // Arrange
        UserEntity entity = new UserEntity();
        
        UserRoleEntity role1 = new UserRoleEntity();
        role1.setId(1L);
        
        UserRoleEntity role2 = new UserRoleEntity();
        role2.setId(2L);
        
        Set<UserRoleEntity> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);
        
        // Act
        entity.setRoles(roles);
        
        // Assert
        assertNotNull(entity.getRoles());
        assertEquals(2, entity.getRoles().size());
        
        // Verify we can add more roles
        UserRoleEntity role3 = new UserRoleEntity();
        role3.setId(3L);
        
        entity.getRoles().add(role3);
        assertEquals(3, entity.getRoles().size());
    }
    
    @Test
    void testExplicitRolesGetterWithNull() {
        // Arrange
        UserEntity entity = new UserEntity();
        entity.setRoles(null);
        
        // Act
        Set<UserRoleEntity> roles = entity.getRoles();
        
        // Assert
        assertNotNull(roles, "Roles should never be null, even when set to null");
        assertTrue(roles.isEmpty(), "Roles should be empty when initialized from null");
    }
    
    @Test
    void testOrganizedEventsRelationship() {
        // Arrange
        UserEntity entity = new UserEntity();
        
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
        entity.setOrganizedEvents(events);
        
        // Assert
        assertNotNull(entity.getOrganizedEvents());
        assertEquals(2, entity.getOrganizedEvents().size());
        
        // Verify we can add more events
        EventEntity event3 = new EventEntity();
        event3.setId(3L);
        event3.setTitle("Meetup");
        
        entity.getOrganizedEvents().add(event3);
        assertEquals(3, entity.getOrganizedEvents().size());
    }
    
    @Test
    void testAttendedEventsRelationship() {
        // Arrange
        UserEntity entity = new UserEntity();
        
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
        entity.setAttendedEvents(events);
        
        // Assert
        assertNotNull(entity.getAttendedEvents());
        assertEquals(2, entity.getAttendedEvents().size());
        
        // Verify we can add more events
        EventEntity event3 = new EventEntity();
        event3.setId(3L);
        event3.setTitle("Meetup");
        
        entity.getAttendedEvents().add(event3);
        assertEquals(3, entity.getAttendedEvents().size());
    }
    
    @Test
    void testTicketsRelationship() {
        // Arrange
        UserEntity entity = new UserEntity();
        
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
    
    @Test
    void testPaymentsRelationship() {
        // Arrange
        UserEntity entity = new UserEntity();
        
        PaymentEntity payment1 = new PaymentEntity();
        payment1.setId(1L);
        payment1.setTransactionId("TRX-001");
        
        PaymentEntity payment2 = new PaymentEntity();
        payment2.setId(2L);
        payment2.setTransactionId("TRX-002");
        
        Set<PaymentEntity> payments = new HashSet<>();
        payments.add(payment1);
        payments.add(payment2);
        
        // Act
        entity.setPayments(payments);
        
        // Assert
        assertNotNull(entity.getPayments());
        assertEquals(2, entity.getPayments().size());
        
        // Verify we can add more payments
        PaymentEntity payment3 = new PaymentEntity();
        payment3.setId(3L);
        payment3.setTransactionId("TRX-003");
        
        entity.getPayments().add(payment3);
        assertEquals(3, entity.getPayments().size());
    }
    
    @Test
    void testAllPropertiesTogether() {
        // Arrange
        UserEntity entity = new UserEntity();
        
        // Base entity properties
        Long id = 1L;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        
        // Basic fields
        String username = "jsmith";
        String email = "john@example.com";
        String password = "password123";
        String firstName = "John";
        String lastName = "Smith";
        String phone = "+1234567890";
        
        // Collections
        Set<UserRoleEntity> roles = new HashSet<>();
        Set<EventEntity> organizedEvents = new HashSet<>();
        Set<EventEntity> attendedEvents = new HashSet<>();
        Set<TicketEntity> tickets = new HashSet<>();
        Set<PaymentEntity> payments = new HashSet<>();
        
        // Act
        entity.setId(id);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);
        entity.setUsername(username);
        entity.setEmail(email);
        entity.setPassword(password);
        entity.setFirstName(firstName);
        entity.setLastName(lastName);
        entity.setPhone(phone);
        entity.setRoles(roles);
        entity.setOrganizedEvents(organizedEvents);
        entity.setAttendedEvents(attendedEvents);
        entity.setTickets(tickets);
        entity.setPayments(payments);
        
        // Assert
        assertEquals(id, entity.getId());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatedAt());
        assertEquals(username, entity.getUsername());
        assertEquals(email, entity.getEmail());
        assertEquals(password, entity.getPassword());
        assertEquals(firstName, entity.getFirstName());
        assertEquals(lastName, entity.getLastName());
        assertEquals(phone, entity.getPhone());
        assertSame(roles, entity.getRoles());
        assertSame(organizedEvents, entity.getOrganizedEvents());
        assertSame(attendedEvents, entity.getAttendedEvents());
        assertSame(tickets, entity.getTickets());
        assertSame(payments, entity.getPayments());
    }
} 