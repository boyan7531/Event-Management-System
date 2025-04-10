package com.softuni.event.service.impl;

import com.softuni.event.model.entity.EventEntity;
import com.softuni.event.model.entity.NotificationEntity;
import com.softuni.event.model.entity.UserEntity;
import com.softuni.event.model.enums.EventStatus;
import com.softuni.event.model.enums.NotificationType;
import com.softuni.event.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleNotificationServiceImplTest {
    
    @Mock
    private EventRepository eventRepository;
    
    @InjectMocks
    private SimpleNotificationServiceImpl notificationService;
    
    private UserEntity testUser;
    private UserEntity testOrganizer;
    private EventEntity testEvent;
    
    @BeforeEach
    void setUp() {
        // Set up test data
        testUser = new UserEntity();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("user@example.com");
        
        testOrganizer = new UserEntity();
        testOrganizer.setId(3L);
        testOrganizer.setUsername("organizer");
        testOrganizer.setEmail("organizer@example.com");
        
        Set<UserEntity> attendees = new HashSet<>();
        attendees.add(testUser);
        
        testEvent = new EventEntity();
        testEvent.setId(1L);
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setEventDate(LocalDateTime.now().plusDays(1));
        testEvent.setOrganizer(testOrganizer);
        testEvent.setAttendees(attendees);
        testEvent.setStatus(EventStatus.APPROVED);
    }
    
    @Test
    void createNotification_ShouldReturnDummyNotification() {
        // Arrange
        String message = "Test message";
        NotificationType type = NotificationType.EVENT_REMINDER;
        String link = "/test/link";
        
        // Act
        NotificationEntity result = notificationService.createNotification(message, type, testUser, link);
        
        // Assert
        assertNotNull(result);
        assertEquals(message, result.getMessage());
        assertEquals(type, result.getType());
        assertEquals(testUser, result.getUser());
        assertEquals(link, result.getLink());
    }
    
    @Test
    void getNotificationsForUser_ShouldReturnEmptyList() {
        // Act
        List<NotificationEntity> result = notificationService.getNotificationsForUser(testUser);
        
        // Assert
        assertTrue(result.isEmpty());
    }
    
    @Test
    void getUnreadNotificationsForUser_ShouldReturnEmptyList() {
        // Act
        List<NotificationEntity> result = notificationService.getUnreadNotificationsForUser(testUser);
        
        // Assert
        assertTrue(result.isEmpty());
    }
    
    @Test
    void getUnreadNotificationCount_ShouldReturnZero() {
        // Act
        long result = notificationService.getUnreadNotificationCount(testUser);
        
        // Assert
        assertEquals(0, result);
    }
    
    @Test
    void notifyUpcomingEvents_ShouldAddToInMemoryNotifications() {
        // Arrange
        List<EventEntity> upcomingEvents = Arrays.asList(testEvent);
        when(eventRepository.findEventsBetweenDates(any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.APPROVED)))
                .thenReturn(upcomingEvents);
        
        // Act
        notificationService.notifyUpcomingEvents();
        
        // Assert
        verify(eventRepository).findEventsBetweenDates(any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.APPROVED));
        
        // Verify notifications in memory map
        List<String> organizerNotifications = notificationService.getRecentNotifications(testOrganizer.getUsername());
        List<String> userNotifications = notificationService.getRecentNotifications(testUser.getUsername());
        
        assertFalse(organizerNotifications.isEmpty());
        assertFalse(userNotifications.isEmpty());
        assertTrue(organizerNotifications.get(0).contains("Your event"));
        assertTrue(userNotifications.get(0).contains("Reminder"));
    }
    
    @Test
    void notifyEventStatusChange_ShouldAddToInMemoryNotifications() {
        // Act
        notificationService.notifyEventStatusChange(testEvent);
        
        // Assert - Check in-memory notifications
        List<String> organizerNotifications = notificationService.getRecentNotifications(testOrganizer.getUsername());
        List<String> userNotifications = notificationService.getRecentNotifications(testUser.getUsername());
        
        assertFalse(organizerNotifications.isEmpty());
        assertFalse(userNotifications.isEmpty());
        assertTrue(organizerNotifications.get(0).contains("status has been changed"));
        assertTrue(userNotifications.get(0).contains("status has been changed"));
    }
    
    @Test
    void notifyEventCreated_ShouldAddToInMemoryNotifications() {
        // Act
        notificationService.notifyEventCreated(testEvent);
        
        // Assert
        List<String> organizerNotifications = notificationService.getRecentNotifications(testOrganizer.getUsername());
        
        assertFalse(organizerNotifications.isEmpty());
        assertTrue(organizerNotifications.get(0).contains("has been created"));
    }
    
    @Test
    void notifyEventCancelled_ShouldAddToInMemoryNotifications() {
        // Act
        notificationService.notifyEventCancelled(testEvent);
        
        // Assert
        List<String> userNotifications = notificationService.getRecentNotifications(testUser.getUsername());
        
        assertFalse(userNotifications.isEmpty());
        assertTrue(userNotifications.get(0).contains("has been cancelled"));
    }
    
    @Test
    void notifyUserJoinedEvent_ShouldAddToInMemoryNotifications() {
        // Act
        notificationService.notifyUserJoinedEvent(testUser, testEvent);
        
        // Assert
        List<String> organizerNotifications = notificationService.getRecentNotifications(testOrganizer.getUsername());
        List<String> userNotifications = notificationService.getRecentNotifications(testUser.getUsername());
        
        assertFalse(organizerNotifications.isEmpty());
        assertFalse(userNotifications.isEmpty());
        assertTrue(organizerNotifications.get(0).contains("has joined your event"));
        assertTrue(userNotifications.get(0).contains("successfully joined"));
    }
    
    @Test
    void getRecentNotifications_ShouldReturnEmptyListWhenNoNotifications() {
        // Act
        List<String> result = notificationService.getRecentNotifications("unknownuser");
        
        // Assert
        assertTrue(result.isEmpty());
    }
    
    @Test
    void getRecentNotifications_ShouldReturnNotificationsWhenAvailable() {
        // Pre-populate some notifications
        notificationService.notifyEventCreated(testEvent);
        
        // Act
        List<String> result = notificationService.getRecentNotifications(testOrganizer.getUsername());
        
        // Assert
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).contains("has been created"));
    }
    
    @Test
    void createEventStatusNotification_ShouldUseNotifyEventStatusChange() {
        // Act
        notificationService.createEventStatusNotification(testEvent, NotificationType.EVENT_APPROVED);
        
        // Assert - Check if it added a notification via notifyEventStatusChange
        List<String> organizerNotifications = notificationService.getRecentNotifications(testOrganizer.getUsername());
        List<String> userNotifications = notificationService.getRecentNotifications(testUser.getUsername());
        
        assertFalse(organizerNotifications.isEmpty());
        assertFalse(userNotifications.isEmpty());
        assertTrue(organizerNotifications.get(0).contains("status has been changed"));
    }
    
    @Test
    void createPendingEventNotification_ShouldUseNotifyEventCreated() {
        // Act
        notificationService.createPendingEventNotification(testEvent);
        
        // Assert - Check if it added a notification via notifyEventCreated
        List<String> organizerNotifications = notificationService.getRecentNotifications(testOrganizer.getUsername());
        
        assertFalse(organizerNotifications.isEmpty());
        assertTrue(organizerNotifications.get(0).contains("has been created"));
    }
    
    @Test
    void markAsRead_ShouldDoNothing() {
        // This is a no-op in SimpleNotificationServiceImpl but should not throw exceptions
        notificationService.markAsRead(1L);
    }
    
    @Test
    void markAllAsRead_ShouldDoNothing() {
        // This is a no-op in SimpleNotificationServiceImpl but should not throw exceptions
        notificationService.markAllAsRead(testUser);
    }
    
    @Test
    void deleteNotification_ShouldDoNothing() {
        // This is a no-op in SimpleNotificationServiceImpl but should not throw exceptions
        notificationService.deleteNotification(1L);
    }
} 