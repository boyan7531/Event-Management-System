package com.softuni.event.repository;

import com.softuni.event.model.entity.EventEntity;
import com.softuni.event.model.entity.LocationEntity;
import com.softuni.event.model.entity.UserEntity;
import com.softuni.event.model.enums.EventStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LocationRepository locationRepository;
    
    private UserEntity organizer;
    private LocationEntity location;
    private EventEntity event1; // Upcoming approved event
    private EventEntity event2; // Past approved event
    private EventEntity event3; // Upcoming pending event

    @BeforeEach
    void setUp() {
        // Create test users
        organizer = new UserEntity();
        organizer.setUsername("organizer");
        organizer.setEmail("organizer@example.com");
        organizer.setPassword("password");
        organizer.setFirstName("Test");
        organizer.setLastName("Organizer");
        organizer = userRepository.save(organizer);
        
        // Create test locations
        location = new LocationEntity();
        location.setName("Test Venue");
        location.setAddress("123 Test St");
        location.setCapacity(100);
        location = locationRepository.save(location);
        
        // Create test events
        LocalDateTime now = LocalDateTime.now();
        
        // Event 1 - Upcoming approved event
        event1 = new EventEntity();
        event1.setTitle("Future Conference");
        event1.setDescription("A conference about the future");
        event1.setOrganizer(organizer);
        event1.setLocation(location);
        event1.setEventDate(now.plusDays(10));
        event1.setTicketPrice(new BigDecimal("10.00"));
        event1.setAvailableTickets(50);
        event1.setPaid(true);
        event1.setStatus(EventStatus.APPROVED);
        event1.setAttendees(new HashSet<>());
        event1.setTickets(new HashSet<>());
        
        // Event 2 - Past approved event
        event2 = new EventEntity();
        event2.setTitle("Past Workshop");
        event2.setDescription("A workshop that already happened");
        event2.setOrganizer(organizer);
        event2.setLocation(location);
        event2.setEventDate(now.minusDays(10));
        event2.setTicketPrice(new BigDecimal("15.00"));
        event2.setAvailableTickets(30);
        event2.setPaid(true);
        event2.setStatus(EventStatus.APPROVED);
        event2.setAttendees(new HashSet<>());
        event2.setTickets(new HashSet<>());
        
        // Event 3 - Upcoming pending event
        event3 = new EventEntity();
        event3.setTitle("Pending Meetup");
        event3.setDescription("A meetup waiting for approval");
        event3.setOrganizer(organizer);
        event3.setLocation(location);
        event3.setEventDate(now.plusDays(5));
        event3.setTicketPrice(new BigDecimal("5.00"));
        event3.setAvailableTickets(20);
        event3.setPaid(true);
        event3.setStatus(EventStatus.PENDING);
        event3.setAttendees(new HashSet<>());
        event3.setTickets(new HashSet<>());
        
        // Save events
        event1 = eventRepository.save(event1);
        event2 = eventRepository.save(event2);
        event3 = eventRepository.save(event3);
    }
    
    @Test
    void testFindByStatus() {
        // Act
        List<EventEntity> approvedEvents = eventRepository.findByStatus(EventStatus.APPROVED);
        List<EventEntity> pendingEvents = eventRepository.findByStatus(EventStatus.PENDING);
        
        // Assert
        assertEquals(2, approvedEvents.size());
        assertTrue(approvedEvents.contains(event1));
        assertTrue(approvedEvents.contains(event2));
        
        assertEquals(1, pendingEvents.size());
        assertTrue(pendingEvents.contains(event3));
    }
    
    @Test
    void testFindByOrganizerId() {
        // Act
        List<EventEntity> organizerEvents = eventRepository.findByOrganizerId(organizer.getId());
        
        // Assert
        assertEquals(3, organizerEvents.size());
        assertTrue(organizerEvents.contains(event1));
        assertTrue(organizerEvents.contains(event2));
        assertTrue(organizerEvents.contains(event3));
    }
    
    @Test
    void testFindUpcomingEvents() {
        // Act
        LocalDateTime now = LocalDateTime.now();
        List<EventEntity> upcomingApprovedEvents = eventRepository.findUpcomingEvents(now, EventStatus.APPROVED);
        
        // Assert
        assertEquals(1, upcomingApprovedEvents.size());
        assertEquals(event1.getId(), upcomingApprovedEvents.get(0).getId());
    }
    
    @Test
    void testFindPastEvents() {
        // Act
        LocalDateTime now = LocalDateTime.now();
        List<EventEntity> pastApprovedEvents = eventRepository.findPastEvents(now, EventStatus.APPROVED);
        
        // Assert
        assertEquals(1, pastApprovedEvents.size());
        assertEquals(event2.getId(), pastApprovedEvents.get(0).getId());
    }
    
    @Test
    void testFindEventsBetweenDates() {
        // Act
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(15);
        List<EventEntity> eventsInRange = eventRepository.findEventsBetweenDates(start, end, EventStatus.APPROVED);
        
        // Assert
        assertEquals(1, eventsInRange.size());
        assertEquals(event1.getId(), eventsInRange.get(0).getId());
    }
    
    @Test
    void testSearchByTitleOrDescription() {
        // Act
        List<EventEntity> conferenceEvents = eventRepository.searchByTitleOrDescription("Conference");
        List<EventEntity> futureEvents = eventRepository.searchByTitleOrDescription("future");
        List<EventEntity> nonExistentEvents = eventRepository.searchByTitleOrDescription("nonexistent");
        
        // Assert
        assertEquals(1, conferenceEvents.size());
        assertEquals(event1.getId(), conferenceEvents.get(0).getId());
        
        assertEquals(1, futureEvents.size());
        assertEquals(event1.getId(), futureEvents.get(0).getId());
        
        assertEquals(0, nonExistentEvents.size());
    }
    
    @Test
    void testFindByEventDateBetweenAndStatus() {
        // Act
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(15);
        List<EventEntity> approvedEventsInRange = eventRepository.findByEventDateBetweenAndStatus(start, end, EventStatus.APPROVED);
        List<EventEntity> pendingEventsInRange = eventRepository.findByEventDateBetweenAndStatus(start, end, EventStatus.PENDING);
        
        // Assert
        assertEquals(1, approvedEventsInRange.size());
        assertEquals(event1.getId(), approvedEventsInRange.get(0).getId());
        
        assertEquals(1, pendingEventsInRange.size());
        assertEquals(event3.getId(), pendingEventsInRange.get(0).getId());
    }
    
    @Test
    void testFindOverlappingEvents() {
        // Create a new event at the same time and location
        EventEntity overlappingEvent = new EventEntity();
        overlappingEvent.setTitle("Overlapping Event");
        overlappingEvent.setDescription("This event overlaps with event1");
        overlappingEvent.setOrganizer(organizer);
        overlappingEvent.setLocation(location);
        overlappingEvent.setEventDate(event1.getEventDate());
        overlappingEvent.setTicketPrice(new BigDecimal("20.00"));
        overlappingEvent.setAvailableTickets(30);
        overlappingEvent.setPaid(true);
        overlappingEvent.setStatus(EventStatus.APPROVED);
        overlappingEvent.setAttendees(new HashSet<>());
        overlappingEvent.setTickets(new HashSet<>());
        
        overlappingEvent = eventRepository.save(overlappingEvent);
        
        // Act
        List<EventStatus> statuses = List.of(EventStatus.APPROVED);
        List<EventEntity> overlappingEvents = eventRepository.findOverlappingEvents(
                location.getId(),
                event1.getEventDate(),
                event1.getEventDate().plusHours(3),
                event1.getId(),
                statuses
        );
        
        // Assert
        assertEquals(1, overlappingEvents.size());
        assertEquals(overlappingEvent.getId(), overlappingEvents.get(0).getId());
    }
    
    @Test
    void testFindByIdWithAssociations() {
        // Act
        EventEntity foundEvent = eventRepository.findByIdWithAssociations(event1.getId());
        
        // Assert
        assertNotNull(foundEvent);
        assertEquals(event1.getId(), foundEvent.getId());
        assertNotNull(foundEvent.getOrganizer());
        assertEquals(organizer.getId(), foundEvent.getOrganizer().getId());
        assertNotNull(foundEvent.getLocation());
        assertEquals(location.getId(), foundEvent.getLocation().getId());
        assertNotNull(foundEvent.getAttendees());
    }
    
    @Test
    void testBasicJpaRepositoryMethods() {
        // Test findById
        Optional<EventEntity> foundEvent = eventRepository.findById(event1.getId());
        assertTrue(foundEvent.isPresent());
        assertEquals("Future Conference", foundEvent.get().getTitle());
        
        // Test findAll
        List<EventEntity> allEvents = eventRepository.findAll();
        assertEquals(3, allEvents.size()); // 3 events from setup
        
        // Test update
        EventEntity eventToUpdate = foundEvent.get();
        eventToUpdate.setTitle("Updated Conference");
        eventRepository.save(eventToUpdate);
        
        Optional<EventEntity> updatedEvent = eventRepository.findById(event1.getId());
        assertTrue(updatedEvent.isPresent());
        assertEquals("Updated Conference", updatedEvent.get().getTitle());
        
        // Test delete
        eventRepository.delete(eventToUpdate);
        assertEquals(2, eventRepository.count());
        assertFalse(eventRepository.findById(event1.getId()).isPresent());
    }
} 