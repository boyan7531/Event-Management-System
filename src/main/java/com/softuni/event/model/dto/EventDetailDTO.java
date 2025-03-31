package com.softuni.event.model.dto;

import com.softuni.event.model.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventDetailDTO {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private LocalDateTime registrationDeadline;
    private boolean isPaid;
    private BigDecimal ticketPrice;
    private Integer availableTickets;
    private String imageUrl;
    private EventStatus status;
    private UserBasicDTO organizer;
    private LocationBasicDTO location;
    private Set<UserBasicDTO> attendees;
    private int totalAttendees;
    private boolean joinedByCurrentUser;
    private String weatherForecast;
} 