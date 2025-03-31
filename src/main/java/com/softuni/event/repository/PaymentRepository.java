package com.softuni.event.repository;

import com.softuni.event.model.entity.PaymentEntity;
import com.softuni.event.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    List<PaymentEntity> findByUserId(Long userId);
    List<PaymentEntity> findByStatus(PaymentStatus status);
    Optional<PaymentEntity> findByTransactionId(String transactionId);
    Optional<PaymentEntity> findByTicketId(Long ticketId);
    
    @Query("SELECT p FROM PaymentEntity p WHERE p.paymentDate BETWEEN ?1 AND ?2")
    List<PaymentEntity> findPaymentsBetweenDates(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT SUM(p.amount) FROM PaymentEntity p WHERE p.status = ?1")
    Double getTotalAmountByStatus(PaymentStatus status);
} 