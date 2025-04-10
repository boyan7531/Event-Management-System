package com.softuni.event.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TicketEntityTest {

    @Test
    void testDefaultConstructor() {
        TicketEntity entity = new TicketEntity();
        assertNotNull(entity);
        assertFalse(entity.isUsed(), "Default used status should be false");
    }
    
    @Test
    void testInheritanceFromBaseEntity() {
        TicketEntity entity = new TicketEntity();
        
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
        TicketEntity entity = new TicketEntity();
        String ticketNumber = "TICKET-12345";
        LocalDateTime issueDate = LocalDateTime.now().minusHours(2);
        boolean used = true;
        
        // Act
        entity.setTicketNumber(ticketNumber);
        entity.setIssueDate(issueDate);
        entity.setUsed(used);
        
        // Assert
        assertEquals(ticketNumber, entity.getTicketNumber());
        assertEquals(issueDate, entity.getIssueDate());
        assertEquals(used, entity.isUsed());
    }
    
    @Test
    void testEventRelationship() {
        // Arrange
        TicketEntity entity = new TicketEntity();
        EventEntity event = new EventEntity();
        event.setId(1L);
        event.setTitle("Concert");
        
        // Act
        entity.setEvent(event);
        
        // Assert
        assertNotNull(entity.getEvent());
        assertEquals(event.getId(), entity.getEvent().getId());
        assertEquals(event.getTitle(), entity.getEvent().getTitle());
    }
    
    @Test
    void testUserRelationship() {
        // Arrange
        TicketEntity entity = new TicketEntity();
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("attendee");
        
        // Act
        entity.setUser(user);
        
        // Assert
        assertNotNull(entity.getUser());
        assertEquals(user.getId(), entity.getUser().getId());
        assertEquals(user.getUsername(), entity.getUser().getUsername());
    }
    
    @Test
    void testPaymentRelationship() {
        // Arrange
        TicketEntity entity = new TicketEntity();
        PaymentEntity payment = new PaymentEntity();
        payment.setId(1L);
        payment.setTransactionId("TRX-123");
        
        // Act
        entity.setPayment(payment);
        
        // Assert
        assertNotNull(entity.getPayment());
        assertEquals(payment.getId(), entity.getPayment().getId());
        assertEquals(payment.getTransactionId(), entity.getPayment().getTransactionId());
    }
    
    @Test
    void testPrePersistMethod() {
        // Arrange
        TicketEntity entity = new TicketEntity();
        
        // Act
        entity.prePersist();
        
        // Assert
        assertNotNull(entity.getTicketNumber(), "TicketNumber should be auto-generated");
        assertTrue(entity.getTicketNumber().length() > 10, "TicketNumber should be a valid UUID");
        assertNotNull(entity.getIssueDate(), "IssueDate should be auto-generated");
        assertTrue(entity.getIssueDate().isBefore(LocalDateTime.now().plusSeconds(1)), "IssueDate should be current or earlier");
        assertTrue(entity.getIssueDate().isAfter(LocalDateTime.now().minusSeconds(10)), "IssueDate should be recent");
    }
    
    @Test
    void testPrePersistDoesNotOverrideExistingValues() {
        // Arrange
        TicketEntity entity = new TicketEntity();
        String ticketNumber = "CUSTOM-TICKET";
        LocalDateTime issueDate = LocalDateTime.now().minusDays(1);
        
        entity.setTicketNumber(ticketNumber);
        entity.setIssueDate(issueDate);
        
        // Act
        entity.prePersist();
        
        // Assert
        assertEquals(ticketNumber, entity.getTicketNumber(), "Existing ticketNumber should not be overridden");
        assertEquals(issueDate, entity.getIssueDate(), "Existing issueDate should not be overridden");
    }
    
    @Test
    void testAllPropertiesTogether() {
        // Arrange
        TicketEntity entity = new TicketEntity();
        Long id = 1L;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        String ticketNumber = "TICKET-12345";
        LocalDateTime issueDate = LocalDateTime.now().minusHours(2);
        boolean used = true;
        
        EventEntity event = new EventEntity();
        event.setId(2L);
        event.setTitle("Workshop");
        
        UserEntity user = new UserEntity();
        user.setId(3L);
        user.setUsername("participant");
        
        PaymentEntity payment = new PaymentEntity();
        payment.setId(4L);
        payment.setTransactionId("TRX-456");
        
        // Act
        entity.setId(id);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);
        entity.setTicketNumber(ticketNumber);
        entity.setIssueDate(issueDate);
        entity.setUsed(used);
        entity.setEvent(event);
        entity.setUser(user);
        entity.setPayment(payment);
        
        // Assert
        assertEquals(id, entity.getId());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatedAt());
        assertEquals(ticketNumber, entity.getTicketNumber());
        assertEquals(issueDate, entity.getIssueDate());
        assertEquals(used, entity.isUsed());
        assertEquals(event, entity.getEvent());
        assertEquals(user, entity.getUser());
        assertEquals(payment, entity.getPayment());
    }
} 