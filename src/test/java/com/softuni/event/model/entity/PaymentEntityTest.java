package com.softuni.event.model.entity;

import com.softuni.event.model.enums.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PaymentEntityTest {

    @Test
    void testDefaultConstructor() {
        PaymentEntity entity = new PaymentEntity();
        assertNotNull(entity);
    }
    
    @Test
    void testInheritanceFromBaseEntity() {
        PaymentEntity entity = new PaymentEntity();
        
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
        PaymentEntity entity = new PaymentEntity();
        String transactionId = "TRX-12345";
        BigDecimal amount = new BigDecimal("99.99");
        LocalDateTime paymentDate = LocalDateTime.now();
        PaymentStatus status = PaymentStatus.COMPLETED;
        String paymentMethod = "Credit Card";
        
        // Act
        entity.setTransactionId(transactionId);
        entity.setAmount(amount);
        entity.setPaymentDate(paymentDate);
        entity.setStatus(status);
        entity.setPaymentMethod(paymentMethod);
        
        // Assert
        assertEquals(transactionId, entity.getTransactionId());
        assertEquals(amount, entity.getAmount());
        assertEquals(paymentDate, entity.getPaymentDate());
        assertEquals(status, entity.getStatus());
        assertEquals(paymentMethod, entity.getPaymentMethod());
    }
    
    @Test
    void testUserRelationship() {
        // Arrange
        PaymentEntity entity = new PaymentEntity();
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("customer");
        
        // Act
        entity.setUser(user);
        
        // Assert
        assertNotNull(entity.getUser());
        assertEquals(user.getId(), entity.getUser().getId());
        assertEquals(user.getUsername(), entity.getUser().getUsername());
    }
    
    @Test
    void testTicketRelationship() {
        // Arrange
        PaymentEntity entity = new PaymentEntity();
        TicketEntity ticket = new TicketEntity();
        ticket.setId(1L);
        ticket.setTicketNumber("TICKET-001");
        
        // Act
        entity.setTicket(ticket);
        
        // Assert
        assertNotNull(entity.getTicket());
        assertEquals(ticket.getId(), entity.getTicket().getId());
        assertEquals(ticket.getTicketNumber(), entity.getTicket().getTicketNumber());
    }
    
    @Test
    void testAllPropertiesTogether() {
        // Arrange
        PaymentEntity entity = new PaymentEntity();
        Long id = 1L;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        String transactionId = "TRX-67890";
        BigDecimal amount = new BigDecimal("149.99");
        LocalDateTime paymentDate = LocalDateTime.now().minusHours(2);
        PaymentStatus status = PaymentStatus.COMPLETED;
        String paymentMethod = "PayPal";
        
        UserEntity user = new UserEntity();
        user.setId(2L);
        user.setUsername("buyer");
        
        TicketEntity ticket = new TicketEntity();
        ticket.setId(3L);
        ticket.setTicketNumber("TICKET-002");
        
        // Act
        entity.setId(id);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);
        entity.setTransactionId(transactionId);
        entity.setAmount(amount);
        entity.setPaymentDate(paymentDate);
        entity.setStatus(status);
        entity.setPaymentMethod(paymentMethod);
        entity.setUser(user);
        entity.setTicket(ticket);
        
        // Assert
        assertEquals(id, entity.getId());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatedAt());
        assertEquals(transactionId, entity.getTransactionId());
        assertEquals(amount, entity.getAmount());
        assertEquals(paymentDate, entity.getPaymentDate());
        assertEquals(status, entity.getStatus());
        assertEquals(paymentMethod, entity.getPaymentMethod());
        assertEquals(user, entity.getUser());
        assertEquals(ticket, entity.getTicket());
    }
} 