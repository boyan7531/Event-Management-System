package com.softuni.event.service;

import com.softuni.event.model.dto.PaymentBasicDTO;
import com.softuni.event.model.enums.PaymentStatus;

import java.util.List;

public interface PaymentService {
    PaymentBasicDTO createPayment(Long ticketId, String paymentMethod);
    PaymentBasicDTO getPaymentById(Long id);
    PaymentBasicDTO getPaymentByTransactionId(String transactionId);
    PaymentBasicDTO getPaymentByTicketId(Long ticketId);
    List<PaymentBasicDTO> getPaymentsByUser(String username);
    void updatePaymentStatus(Long id, PaymentStatus status);
    double getTotalPaymentAmount(PaymentStatus status);
} 