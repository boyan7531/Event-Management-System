package com.softuni.event.model.dto;

import com.softuni.event.model.enums.EventStatus;
import com.softuni.event.model.enums.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TicketDTOTest {

    @Test
    void testDefaultConstructor() {
        TicketDTO dto = new TicketDTO();
        assertNotNull(dto);
    }
    
    @Test
    void testAllArgsConstructor() {
        // Arrange
        Long id = 1L;
        String ticketNumber = "TICKET-123";
        LocalDateTime issueDate = LocalDateTime.now();
        boolean used = false;
        
        EventBasicDTO event = new EventBasicDTO();
        event.setId(10L);
        event.setTitle("Concert");
        
        UserBasicDTO user = new UserBasicDTO();
        user.setId(20L);
        user.setUsername("john.doe");
        
        PaymentBasicDTO payment = new PaymentBasicDTO();
        payment.setId(30L);
        payment.setTransactionId("TRX-ABC");
        payment.setStatus(PaymentStatus.COMPLETED);
        
        // Act
        TicketDTO dto = new TicketDTO(id, ticketNumber, issueDate, used, event, user, payment);
        
        // Assert
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(ticketNumber, dto.getTicketNumber());
        assertEquals(issueDate, dto.getIssueDate());
        assertEquals(used, dto.isUsed());
        
        assertNotNull(dto.getEvent());
        assertEquals(10L, dto.getEvent().getId());
        assertEquals("Concert", dto.getEvent().getTitle());
        
        assertNotNull(dto.getUser());
        assertEquals(20L, dto.getUser().getId());
        assertEquals("john.doe", dto.getUser().getUsername());
        
        assertNotNull(dto.getPayment());
        assertEquals(30L, dto.getPayment().getId());
        assertEquals("TRX-ABC", dto.getPayment().getTransactionId());
        assertEquals(PaymentStatus.COMPLETED, dto.getPayment().getStatus());
    }

    @Test
    void testGettersAndSetters() {
        // Arrange
        Long id = 2L;
        String ticketNumber = "TICKET-456";
        LocalDateTime issueDate = LocalDateTime.now().minusDays(1);
        boolean used = true;
        
        EventBasicDTO event = new EventBasicDTO();
        event.setId(11L);
        event.setTitle("Conference");
        event.setStatus(EventStatus.APPROVED);
        
        UserBasicDTO user = new UserBasicDTO();
        user.setId(21L);
        user.setUsername("jane.doe");
        user.setFullName("Jane Doe");
        
        PaymentBasicDTO payment = new PaymentBasicDTO();
        payment.setId(31L);
        payment.setTransactionId("TRX-DEF");
        payment.setAmount(new BigDecimal("50.00"));
        payment.setStatus(PaymentStatus.COMPLETED);

        // Act
        TicketDTO dto = new TicketDTO();
        dto.setId(id);
        dto.setTicketNumber(ticketNumber);
        dto.setIssueDate(issueDate);
        dto.setUsed(used);
        dto.setEvent(event);
        dto.setUser(user);
        dto.setPayment(payment);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(ticketNumber, dto.getTicketNumber());
        assertEquals(issueDate, dto.getIssueDate());
        assertEquals(used, dto.isUsed());
        
        assertNotNull(dto.getEvent());
        assertEquals(11L, dto.getEvent().getId());
        assertEquals("Conference", dto.getEvent().getTitle());
        assertEquals(EventStatus.APPROVED, dto.getEvent().getStatus());
        
        assertNotNull(dto.getUser());
        assertEquals(21L, dto.getUser().getId());
        assertEquals("jane.doe", dto.getUser().getUsername());
        assertEquals("Jane Doe", dto.getUser().getFullName());
        
        assertNotNull(dto.getPayment());
        assertEquals(31L, dto.getPayment().getId());
        assertEquals("TRX-DEF", dto.getPayment().getTransactionId());
        assertEquals(new BigDecimal("50.00"), dto.getPayment().getAmount());
        assertEquals(PaymentStatus.COMPLETED, dto.getPayment().getStatus());
    }
} 