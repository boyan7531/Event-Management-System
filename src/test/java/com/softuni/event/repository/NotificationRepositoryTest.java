package com.softuni.event.repository;

import com.softuni.event.model.entity.NotificationEntity;
import com.softuni.event.model.entity.UserEntity;
import com.softuni.event.model.enums.NotificationType;
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
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private UserEntity user1;
    private UserEntity user2;
    private NotificationEntity notification1; // Old read notification for user1
    private NotificationEntity notification2; // Recent unread notification for user1
    private NotificationEntity notification3; // Very recent unread notification for user1
    private NotificationEntity notification4; // Notification for user2
    
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
        
        // Create test notifications
        LocalDateTime now = LocalDateTime.now();
        
        // Notification 1 - Old, read notification for user1
        notification1 = new NotificationEntity();
        notification1.setMessage("Welcome to the platform");
        notification1.setType(NotificationType.SYSTEM);
        notification1.setUser(user1);
        notification1.setRead(true);
        notification1.setCreatedAt(now.minusDays(10));
        
        // Notification 2 - Recent unread notification for user1
        notification2 = new NotificationEntity();
        notification2.setMessage("New event in your area");
        notification2.setType(NotificationType.EVENT_CREATED);
        notification2.setUser(user1);
        notification2.setRead(false);
        notification2.setCreatedAt(now.minusDays(2));
        
        // Notification 3 - Very recent unread notification for user1
        notification3 = new NotificationEntity();
        notification3.setMessage("Your registered event starts tomorrow");
        notification3.setType(NotificationType.EVENT_REMINDER);
        notification3.setUser(user1);
        notification3.setRead(false);
        notification3.setCreatedAt(now.minusHours(5));
        
        // Notification 4 - Notification for user2
        notification4 = new NotificationEntity();
        notification4.setMessage("Welcome to the platform");
        notification4.setType(NotificationType.SYSTEM);
        notification4.setUser(user2);
        notification4.setRead(false);
        notification4.setCreatedAt(now.minusDays(1));
        
        // Save notifications
        notification1 = notificationRepository.save(notification1);
        notification2 = notificationRepository.save(notification2);
        notification3 = notificationRepository.save(notification3);
        notification4 = notificationRepository.save(notification4);
    }
    
    @Test
    void testFindByUserOrderByCreatedAtDesc() {
        // Act
        List<NotificationEntity> user1Notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user1);
        List<NotificationEntity> user2Notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user2);
        
        // Assert
        assertEquals(3, user1Notifications.size());
        assertEquals(notification3.getId(), user1Notifications.get(0).getId()); // Most recent first
        assertEquals(notification2.getId(), user1Notifications.get(1).getId());
        assertEquals(notification1.getId(), user1Notifications.get(2).getId()); // Oldest last
        
        assertEquals(1, user2Notifications.size());
        assertEquals(notification4.getId(), user2Notifications.get(0).getId());
    }
    
    @Test
    void testFindByUserAndReadFalseOrderByCreatedAtDesc() {
        // Act
        List<NotificationEntity> user1UnreadNotifications = notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user1);
        List<NotificationEntity> user2UnreadNotifications = notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user2);
        
        // Assert
        assertEquals(2, user1UnreadNotifications.size());
        assertEquals(notification3.getId(), user1UnreadNotifications.get(0).getId()); // Most recent first
        assertEquals(notification2.getId(), user1UnreadNotifications.get(1).getId());
        
        assertEquals(1, user2UnreadNotifications.size());
        assertEquals(notification4.getId(), user2UnreadNotifications.get(0).getId());
    }
    
    @Test
    void testCountByUserAndReadFalse() {
        // Act
        long user1UnreadCount = notificationRepository.countByUserAndReadFalse(user1);
        long user2UnreadCount = notificationRepository.countByUserAndReadFalse(user2);
        
        // Assert
        assertEquals(2, user1UnreadCount);
        assertEquals(1, user2UnreadCount);
        
        // Mark one notification as read and test again
        notification2.setRead(true);
        notificationRepository.save(notification2);
        
        long updatedUser1UnreadCount = notificationRepository.countByUserAndReadFalse(user1);
        assertEquals(1, updatedUser1UnreadCount);
    }
    
    @Test
    void testBasicJpaRepositoryMethods() {
        // Test findById
        Optional<NotificationEntity> foundNotification = notificationRepository.findById(notification1.getId());
        assertTrue(foundNotification.isPresent());
        assertEquals("Welcome to the platform", foundNotification.get().getMessage());
        
        // Test findAll
        List<NotificationEntity> allNotifications = notificationRepository.findAll();
        assertEquals(4, allNotifications.size());
        
        // Test update
        NotificationEntity notificationToUpdate = foundNotification.get();
        notificationToUpdate.setMessage("Updated welcome message");
        notificationRepository.save(notificationToUpdate);
        
        Optional<NotificationEntity> updatedNotification = notificationRepository.findById(notification1.getId());
        assertTrue(updatedNotification.isPresent());
        assertEquals("Updated welcome message", updatedNotification.get().getMessage());
        
        // Test delete
        notificationRepository.delete(notificationToUpdate);
        assertEquals(3, notificationRepository.count());
        assertFalse(notificationRepository.findById(notification1.getId()).isPresent());
    }
} 