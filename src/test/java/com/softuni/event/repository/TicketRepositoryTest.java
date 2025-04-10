package com.softuni.event.repository;

import com.softuni.event.model.entity.EventEntity;
import com.softuni.event.model.entity.LocationEntity;
import com.softuni.event.model.entity.TicketEntity;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = Replace.ANY)
class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private LocationRepository locationRepository;
    
    private UserEntity user1;
    private UserEntity user2;
    private LocationEntity location1;
    private LocationEntity location2;
    private EventEntity event1;
    private EventEntity event2;
    private TicketEntity ticket1; // User1, Event1, Used=false
    private TicketEntity ticket2; // User1, Event1, Used=true
    private TicketEntity ticket3; // User2, Event1, Used=false
    private TicketEntity ticket4; // User1, Event2, Used=false
    
    @BeforeEach
    void setUp() {
        // Create test users
        user1 = new UserEntity();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("password");
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1 = userRepository.save(user1);
        
        user2 = new UserEntity();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("password");
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2 = userRepository.save(user2);
        
        // Create test locations
        location1 = new LocationEntity();
        location1.setName("Concert Hall");
        location1.setAddress("123 Music St");
        location1.setCity("New York");
        location1.setCountry("USA");
        location1.setCapacity(500);
        location1 = locationRepository.save(location1);
        
        location2 = new LocationEntity();
        location2.setName("Workshop Center");
        location2.setAddress("456 Tech Ave");
        location2.setCity("San Francisco");
        location2.setCountry("USA");
        location2.setCapacity(100);
        location2 = locationRepository.save(location2);
        
        // Create test events
        LocalDateTime eventDate1 = LocalDateTime.now().plusDays(10);
        event1 = new EventEntity();
        event1.setTitle("Test Concert");
        event1.setDescription("A test concert event");
        event1.setEventDate(eventDate1);
        event1.setOrganizer(user1);
        event1.setStatus(EventStatus.APPROVED);
        event1.setPaid(true);
        event1.setTicketPrice(new BigDecimal("50.00"));
        event1.setAvailableTickets(100);
        event1.setLocation(location1);
        event1 = eventRepository.save(event1);
        
        LocalDateTime eventDate2 = LocalDateTime.now().plusDays(20);
        event2 = new EventEntity();
        event2.setTitle("Test Workshop");
        event2.setDescription("A test workshop event");
        event2.setEventDate(eventDate2);
        event2.setOrganizer(user2);
        event2.setStatus(EventStatus.APPROVED);
        event2.setPaid(true);
        event2.setTicketPrice(new BigDecimal("75.00"));
        event2.setAvailableTickets(50);
        event2.setLocation(location2);
        event2 = eventRepository.save(event2);
        
        // Create test tickets
        ticket1 = new TicketEntity();
        ticket1.setTicketNumber(UUID.randomUUID().toString());
        ticket1.setEvent(event1);
        ticket1.setUser(user1);
        ticket1.setIssueDate(LocalDateTime.now().minusDays(5));
        ticket1.setUsed(false);
        ticket1 = ticketRepository.save(ticket1);
        
        ticket2 = new TicketEntity();
        ticket2.setTicketNumber(UUID.randomUUID().toString());
        ticket2.setEvent(event1);
        ticket2.setUser(user1);
        ticket2.setIssueDate(LocalDateTime.now().minusDays(4));
        ticket2.setUsed(true);
        ticket2 = ticketRepository.save(ticket2);
        
        ticket3 = new TicketEntity();
        ticket3.setTicketNumber(UUID.randomUUID().toString());
        ticket3.setEvent(event1);
        ticket3.setUser(user2);
        ticket3.setIssueDate(LocalDateTime.now().minusDays(3));
        ticket3.setUsed(false);
        ticket3 = ticketRepository.save(ticket3);
        
        ticket4 = new TicketEntity();
        ticket4.setTicketNumber(UUID.randomUUID().toString());
        ticket4.setEvent(event2);
        ticket4.setUser(user1);
        ticket4.setIssueDate(LocalDateTime.now().minusDays(2));
        ticket4.setUsed(false);
        ticket4 = ticketRepository.save(ticket4);
    }
    
    @Test
    void testFindByEventId() {
        // Act
        List<TicketEntity> event1Tickets = ticketRepository.findByEventId(event1.getId());
        List<TicketEntity> event2Tickets = ticketRepository.findByEventId(event2.getId());
        List<TicketEntity> nonExistentEventTickets = ticketRepository.findByEventId(999L);
        
        // Assert
        assertEquals(3, event1Tickets.size());
        assertTrue(event1Tickets.contains(ticket1));
        assertTrue(event1Tickets.contains(ticket2));
        assertTrue(event1Tickets.contains(ticket3));
        
        assertEquals(1, event2Tickets.size());
        assertTrue(event2Tickets.contains(ticket4));
        
        assertEquals(0, nonExistentEventTickets.size());
    }
    
    @Test
    void testFindByUserId() {
        // Act
        List<TicketEntity> user1Tickets = ticketRepository.findByUserId(user1.getId());
        List<TicketEntity> user2Tickets = ticketRepository.findByUserId(user2.getId());
        List<TicketEntity> nonExistentUserTickets = ticketRepository.findByUserId(999L);
        
        // Assert
        assertEquals(3, user1Tickets.size());
        assertTrue(user1Tickets.contains(ticket1));
        assertTrue(user1Tickets.contains(ticket2));
        assertTrue(user1Tickets.contains(ticket4));
        
        assertEquals(1, user2Tickets.size());
        assertTrue(user2Tickets.contains(ticket3));
        
        assertEquals(0, nonExistentUserTickets.size());
    }
    
    @Test
    void testFindByTicketNumber() {
        // Act
        Optional<TicketEntity> foundTicket1 = ticketRepository.findByTicketNumber(ticket1.getTicketNumber());
        Optional<TicketEntity> foundTicket2 = ticketRepository.findByTicketNumber(ticket2.getTicketNumber());
        Optional<TicketEntity> nonExistentTicket = ticketRepository.findByTicketNumber("non-existent-number");
        
        // Assert
        assertTrue(foundTicket1.isPresent());
        assertEquals(ticket1.getId(), foundTicket1.get().getId());
        
        assertTrue(foundTicket2.isPresent());
        assertEquals(ticket2.getId(), foundTicket2.get().getId());
        
        assertFalse(nonExistentTicket.isPresent());
    }
    
    @Test
    void testCountTicketsByEventId() {
        // Act
        Long event1TicketCount = ticketRepository.countTicketsByEventId(event1.getId());
        Long event2TicketCount = ticketRepository.countTicketsByEventId(event2.getId());
        Long nonExistentEventTicketCount = ticketRepository.countTicketsByEventId(999L);
        
        // Assert
        assertEquals(3, event1TicketCount);
        assertEquals(1, event2TicketCount);
        assertEquals(0, nonExistentEventTicketCount);
    }
    
    @Test
    void testFindByEventIdAndUsed() {
        // Act
        List<TicketEntity> event1UsedTickets = ticketRepository.findByEventIdAndUsed(event1.getId(), true);
        List<TicketEntity> event1UnusedTickets = ticketRepository.findByEventIdAndUsed(event1.getId(), false);
        List<TicketEntity> event2UsedTickets = ticketRepository.findByEventIdAndUsed(event2.getId(), true);
        List<TicketEntity> event2UnusedTickets = ticketRepository.findByEventIdAndUsed(event2.getId(), false);
        List<TicketEntity> nonExistentEventTickets = ticketRepository.findByEventIdAndUsed(999L, false);
        
        // Assert
        assertEquals(1, event1UsedTickets.size());
        assertTrue(event1UsedTickets.contains(ticket2));
        
        assertEquals(2, event1UnusedTickets.size());
        assertTrue(event1UnusedTickets.contains(ticket1));
        assertTrue(event1UnusedTickets.contains(ticket3));
        
        assertEquals(0, event2UsedTickets.size());
        
        assertEquals(1, event2UnusedTickets.size());
        assertTrue(event2UnusedTickets.contains(ticket4));
        
        assertEquals(0, nonExistentEventTickets.size());
    }
    
    @Test
    void testBasicJpaRepositoryMethods() {
        // Test findById
        Optional<TicketEntity> foundTicket = ticketRepository.findById(ticket1.getId());
        assertTrue(foundTicket.isPresent());
        assertEquals(ticket1.getTicketNumber(), foundTicket.get().getTicketNumber());
        
        // Test findAll
        List<TicketEntity> allTickets = ticketRepository.findAll();
        assertEquals(4, allTickets.size());
        
        // Test update
        TicketEntity ticketToUpdate = foundTicket.get();
        ticketToUpdate.setUsed(true);
        ticketRepository.save(ticketToUpdate);
        
        Optional<TicketEntity> updatedTicket = ticketRepository.findById(ticket1.getId());
        assertTrue(updatedTicket.isPresent());
        assertTrue(updatedTicket.get().isUsed());
        
        // Test delete
        ticketRepository.delete(ticketToUpdate);
        assertEquals(3, ticketRepository.count());
        assertFalse(ticketRepository.findById(ticket1.getId()).isPresent());
    }
} 