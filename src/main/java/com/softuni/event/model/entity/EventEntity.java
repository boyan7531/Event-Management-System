package com.softuni.event.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.softuni.event.model.enums.EventStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
public class EventEntity extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "registration_deadline")
    private LocalDateTime registrationDeadline;

    @Column(name = "is_paid", nullable = false)
    private boolean isPaid;

    @Column(name = "ticket_price")
    private BigDecimal ticketPrice;

    @Column(name = "available_tickets")
    private Integer availableTickets;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @JsonManagedReference(value = "user-organized-events")
    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    private UserEntity organizer;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private LocationEntity location;

    @JsonManagedReference(value = "user-attended-events")
    @ManyToMany
    @JoinTable(
            name = "event_attendees",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserEntity> attendees = new HashSet<>();

    @JsonManagedReference(value = "event-tickets")
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private Set<TicketEntity> tickets = new HashSet<>();
} 