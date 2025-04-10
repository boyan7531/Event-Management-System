package com.softuni.event.repository;

import com.softuni.event.model.entity.ContactMessageEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ContactMessageRepositoryTest {

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    private ContactMessageEntity message1;
    private ContactMessageEntity message2;
    private ContactMessageEntity message3;

    @BeforeEach
    void setUp() {
        // Create test data
        message1 = new ContactMessageEntity();
        message1.setName("John Doe");
        message1.setEmail("john@example.com");
        message1.setSubject("Inquiry 1");
        message1.setMessage("This is message 1");
        message1.setCreatedAt(LocalDateTime.now().minusDays(2));
        message1.setRead(true);

        message2 = new ContactMessageEntity();
        message2.setName("Jane Smith");
        message2.setEmail("jane@example.com");
        message2.setSubject("Inquiry 2");
        message2.setMessage("This is message 2");
        message2.setCreatedAt(LocalDateTime.now().minusDays(1));
        message2.setRead(false);

        message3 = new ContactMessageEntity();
        message3.setName("Alice Johnson");
        message3.setEmail("alice@example.com");
        message3.setSubject("Inquiry 3");
        message3.setMessage("This is message 3");
        message3.setCreatedAt(LocalDateTime.now());
        message3.setRead(false);

        // Save test data
        message1 = contactMessageRepository.save(message1);
        message2 = contactMessageRepository.save(message2);
        message3 = contactMessageRepository.save(message3);
    }

    @Test
    void testFindAllByOrderByCreatedAtDesc() {
        // Act
        List<ContactMessageEntity> messages = contactMessageRepository.findAllByOrderByCreatedAtDesc();

        // Assert
        assertEquals(3, messages.size());
        assertEquals(message3.getId(), messages.get(0).getId()); // Newest first
        assertEquals(message2.getId(), messages.get(1).getId());
        assertEquals(message1.getId(), messages.get(2).getId()); // Oldest last
    }

    @Test
    void testFindByReadOrderByCreatedAtDesc_Unread() {
        // Act
        List<ContactMessageEntity> unreadMessages = contactMessageRepository.findByReadOrderByCreatedAtDesc(false);

        // Assert
        assertEquals(2, unreadMessages.size());
        assertEquals(message3.getId(), unreadMessages.get(0).getId()); // Newest first
        assertEquals(message2.getId(), unreadMessages.get(1).getId());
    }

    @Test
    void testFindByReadOrderByCreatedAtDesc_Read() {
        // Act
        List<ContactMessageEntity> readMessages = contactMessageRepository.findByReadOrderByCreatedAtDesc(true);

        // Assert
        assertEquals(1, readMessages.size());
        assertEquals(message1.getId(), readMessages.get(0).getId());
    }

    @Test
    void testCountByRead_Unread() {
        // Act
        long unreadCount = contactMessageRepository.countByRead(false);

        // Assert
        assertEquals(2, unreadCount);
    }

    @Test
    void testCountByRead_Read() {
        // Act
        long readCount = contactMessageRepository.countByRead(true);

        // Assert
        assertEquals(1, readCount);
    }

    @Test
    void testBasicJpaRepositoryMethods() {
        // Test findById
        Optional<ContactMessageEntity> foundMessage = contactMessageRepository.findById(message1.getId());
        assertTrue(foundMessage.isPresent());
        assertEquals("John Doe", foundMessage.get().getName());

        // Test findAll
        List<ContactMessageEntity> allMessages = contactMessageRepository.findAll();
        assertEquals(3, allMessages.size());

        // Test update
        ContactMessageEntity messageToUpdate = foundMessage.get();
        messageToUpdate.setSubject("Updated Subject");
        contactMessageRepository.save(messageToUpdate);

        Optional<ContactMessageEntity> updatedMessage = contactMessageRepository.findById(message1.getId());
        assertTrue(updatedMessage.isPresent());
        assertEquals("Updated Subject", updatedMessage.get().getSubject());

        // Test delete
        contactMessageRepository.delete(messageToUpdate);
        assertEquals(2, contactMessageRepository.count());
        assertFalse(contactMessageRepository.findById(message1.getId()).isPresent());
    }
} 