package com.softuni.event.service.impl;

import com.softuni.event.exception.ResourceNotFoundException;
import com.softuni.event.model.dto.PaymentBasicDTO;
import com.softuni.event.model.entity.PaymentEntity;
import com.softuni.event.model.entity.TicketEntity;
import com.softuni.event.model.enums.PaymentStatus;
import com.softuni.event.repository.PaymentRepository;
import com.softuni.event.repository.TicketRepository;
import com.softuni.event.service.PaymentService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final ModelMapper modelMapper;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                             TicketRepository ticketRepository,
                             ModelMapper modelMapper) {
        this.paymentRepository = paymentRepository;
        this.ticketRepository = ticketRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public PaymentBasicDTO createPayment(Long ticketId, String paymentMethod) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        
        // Check if payment already exists for this ticket
        if (paymentRepository.findByTicketId(ticketId).isPresent()) {
            throw new IllegalStateException("Payment already exists for ticket: " + ticketId);
        }
        
        PaymentEntity payment = new PaymentEntity();
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setAmount(ticket.getEvent().getTicketPrice());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(paymentMethod);
        payment.setUser(ticket.getUser());
        payment.setTicket(ticket);
        payment.setCreatedAt(LocalDateTime.now());
        
        PaymentEntity savedPayment = paymentRepository.save(payment);
        
        return modelMapper.map(savedPayment, PaymentBasicDTO.class);
    }

    @Override
    public PaymentBasicDTO getPaymentById(Long id) {
        PaymentEntity payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        
        return modelMapper.map(payment, PaymentBasicDTO.class);
    }

    @Override
    public PaymentBasicDTO getPaymentByTransactionId(String transactionId) {
        PaymentEntity payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionId", transactionId));
        
        return modelMapper.map(payment, PaymentBasicDTO.class);
    }

    @Override
    public PaymentBasicDTO getPaymentByTicketId(Long ticketId) {
        PaymentEntity payment = paymentRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "ticketId", ticketId));
        
        return modelMapper.map(payment, PaymentBasicDTO.class);
    }

    @Override
    public List<PaymentBasicDTO> getPaymentsByUser(String username) {
        List<PaymentEntity> payments = paymentRepository.findByUserId(null); // Need to implement this
        
        return payments.stream()
                .map(payment -> modelMapper.map(payment, PaymentBasicDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updatePaymentStatus(Long id, PaymentStatus status) {
        PaymentEntity payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        
        payment.setStatus(status);
        payment.setUpdatedAt(LocalDateTime.now());
        
        paymentRepository.save(payment);
    }

    @Override
    public double getTotalPaymentAmount(PaymentStatus status) {
        Double total = paymentRepository.getTotalAmountByStatus(status);
        return total != null ? total : 0.0;
    }
} 