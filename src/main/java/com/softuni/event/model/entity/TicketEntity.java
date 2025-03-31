package com.softuni.event.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
public class TicketEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String ticketNumber;

    @Column(nullable = false)
    private LocalDateTime issueDate;

    @Column(nullable = false)
    private boolean used;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private EventEntity event;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToOne(mappedBy = "ticket")
    private PaymentEntity payment;

    @PrePersist
    public void prePersist() {
        if (ticketNumber == null) {
            ticketNumber = UUID.randomUUID().toString();
        }
        if (issueDate == null) {
            issueDate = LocalDateTime.now();
        }
    }
} 