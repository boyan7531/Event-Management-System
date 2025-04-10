package com.softuni.event.model.dto;

import com.softuni.event.model.enums.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PaymentBasicDTOTest {

    @Test
    void testDefaultConstructor() {
        PaymentBasicDTO dto = new PaymentBasicDTO();
        assertNotNull(dto);
    }
    
    @Test
    void testAllArgsConstructor() {
        // Arrange
        Long id = 1L;
        String transactionId = "TRX-12345";
        BigDecimal amount = new BigDecimal("99.99");
        LocalDateTime paymentDate = LocalDateTime.now();
        PaymentStatus status = PaymentStatus.COMPLETED;
        String paymentMethod = "Credit Card";
        
        // Act
        PaymentBasicDTO dto = new PaymentBasicDTO(id, transactionId, amount, paymentDate, status, paymentMethod);
        
        // Assert
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(transactionId, dto.getTransactionId());
        assertEquals(amount, dto.getAmount());
        assertEquals(paymentDate, dto.getPaymentDate());
        assertEquals(status, dto.getStatus());
        assertEquals(paymentMethod, dto.getPaymentMethod());
    }

    @Test
    void testGettersAndSetters() {
        // Arrange
        Long id = 2L;
        String transactionId = "TRX-67890";
        BigDecimal amount = new BigDecimal("149.99");
        LocalDateTime paymentDate = LocalDateTime.now().minusDays(1);
        PaymentStatus status = PaymentStatus.PENDING;
        String paymentMethod = "PayPal";

        // Act
        PaymentBasicDTO dto = new PaymentBasicDTO();
        dto.setId(id);
        dto.setTransactionId(transactionId);
        dto.setAmount(amount);
        dto.setPaymentDate(paymentDate);
        dto.setStatus(status);
        dto.setPaymentMethod(paymentMethod);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(transactionId, dto.getTransactionId());
        assertEquals(amount, dto.getAmount());
        assertEquals(paymentDate, dto.getPaymentDate());
        assertEquals(status, dto.getStatus());
        assertEquals(paymentMethod, dto.getPaymentMethod());
    }
    
    @Test
    void testDifferentPaymentStatuses() {
        // Arrange
        PaymentBasicDTO dto = new PaymentBasicDTO();
        
        // Act & Assert
        dto.setStatus(PaymentStatus.PENDING);
        assertEquals(PaymentStatus.PENDING, dto.getStatus());
        
        dto.setStatus(PaymentStatus.COMPLETED);
        assertEquals(PaymentStatus.COMPLETED, dto.getStatus());
        
        dto.setStatus(PaymentStatus.FAILED);
        assertEquals(PaymentStatus.FAILED, dto.getStatus());
        
        dto.setStatus(PaymentStatus.REFUNDED);
        assertEquals(PaymentStatus.REFUNDED, dto.getStatus());
    }
} 