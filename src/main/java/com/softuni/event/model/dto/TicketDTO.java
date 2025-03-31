package com.softuni.event.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {
    private Long id;
    private String ticketNumber;
    private LocalDateTime issueDate;
    private boolean used;
    private EventBasicDTO event;
    private UserBasicDTO user;
    private PaymentBasicDTO payment;
} 