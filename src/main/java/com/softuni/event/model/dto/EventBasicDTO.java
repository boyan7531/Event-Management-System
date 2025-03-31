package com.softuni.event.model.dto;

import com.softuni.event.model.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventBasicDTO {
    private Long id;
    private String title;
    private LocalDateTime eventDate;
    private boolean isPaid;
    private BigDecimal ticketPrice;
    private String imageUrl;
    private EventStatus status;
    private LocationBasicDTO location;
} 