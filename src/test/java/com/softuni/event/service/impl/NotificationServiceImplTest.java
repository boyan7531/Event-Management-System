package com.softuni.event.service.impl;

import com.softuni.event.model.entity.EventEntity;
import com.softuni.event.model.entity.NotificationEntity;
import com.softuni.event.model.entity.UserEntity;
import com.softuni.event.model.enums.EventStatus;
import com.softuni.event.model.enums.NotificationType;
import com.softuni.event.repository.EventRepository;
import com.softuni.event.repository.NotificationRepository;
import com.softuni.event.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {
    
    @Mock
    private EventRepository eventRepository;
    
    @Mock
    private NotificationRepository notificationRepository;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private NotificationServiceImpl notificationService;
    
    @Captor
    private ArgumentCaptor<NotificationEntity> notificationCaptor;
    
    @Captor
    private ArgumentCaptor<List<NotificationEntity>> notificationsCaptor;
    
    private UserEntity testUser;
    private UserEntity testAdmin;
    private UserEntity testOrganizer;
    private EventEntity testEvent;
    private NotificationEntity testNotification;
    
    @BeforeEach
    void setUp() {
        // Set up test data
        testUser = new UserEntity();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("user@example.com");
        
        testAdmin = new UserEntity();
        testAdmin.setId(2L);
        testAdmin.setUsername("admin");
        testAdmin.setEmail("admin@example.com");
        
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
        
        testNotification = new NotificationEntity();
        testNotification.setId(1L);
        testNotification.setMessage("Test notification");
        testNotification.setType(NotificationType.EVENT_REMINDER);
        testNotification.setUser(testUser);
        testNotification.setLink("/test/link");
        testNotification.setCreatedAt(LocalDateTime.now().minusHours(1));
        testNotification.setRead(false);
    }
    
    @Test
    void createNotification_ShouldCreateAndReturnNotification() {
        // Arrange
        String message = "Test message";
        NotificationType type = NotificationType.EVENT_REMINDER;
        String link = "/test/link";
        
        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(testNotification);
        
        // Act
        NotificationEntity result = notificationService.createNotification(message, type, testUser, link);
        
        // Assert
        assertEquals(testNotification, result);
        verify(notificationRepository).save(notificationCaptor.capture());
        
        NotificationEntity capturedNotification = notificationCaptor.getValue();
        assertEquals(message, capturedNotification.getMessage());
        assertEquals(type, capturedNotification.getType());
        assertEquals(testUser, capturedNotification.getUser());
        assertEquals(link, capturedNotification.getLink());
    }
    
    @Test
    void createEventStatusNotification_ShouldCreateApprovedNotification() {
        // Arrange
        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(testNotification);
        
        // Act
        notificationService.createEventStatusNotification(testEvent, NotificationType.EVENT_APPROVED);
        
        // Assert
        verify(notificationRepository).save(notificationCaptor.capture());
        
        NotificationEntity capturedNotification = notificationCaptor.getValue();
        assertTrue(capturedNotification.getMessage().contains("approved"));
        assertEquals(NotificationType.EVENT_APPROVED, capturedNotification.getType());
        assertEquals(testOrganizer, capturedNotification.getUser());
        assertEquals("/events/details/1", capturedNotification.getLink());
    }
    
    @Test
    void createEventStatusNotification_ShouldCreateRejectedNotification() {
        // Arrange
        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(testNotification);
        
        // Act
        notificationService.createEventStatusNotification(testEvent, NotificationType.EVENT_REJECTED);
        
        // Assert
        verify(notificationRepository).save(notificationCaptor.capture());
        
        NotificationEntity capturedNotification = notificationCaptor.getValue();
        assertTrue(capturedNotification.getMessage().contains("rejected"));
        assertEquals(NotificationType.EVENT_REJECTED, capturedNotification.getType());
        assertEquals(testOrganizer, capturedNotification.getUser());
        assertEquals("/events/details/1", capturedNotification.getLink());
    }
    
    @Test
    void createPendingEventNotification_ShouldNotifyAllAdmins() {
        // Arrange
        List<UserEntity> admins = Arrays.asList(testAdmin);
        when(userService.getAllAdmins()).thenReturn(admins);
        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(testNotification);
        
        // Act
        notificationService.createPendingEventNotification(testEvent);
        
        // Assert
        verify(userService).getAllAdmins();
        verify(notificationRepository).save(notificationCaptor.capture());
        
        NotificationEntity capturedNotification = notificationCaptor.getValue();
        assertTrue(capturedNotification.getMessage().contains("waiting for approval"));
        assertEquals(NotificationType.NEW_EVENT_PENDING, capturedNotification.getType());
        assertEquals(testAdmin, capturedNotification.getUser());
        assertEquals("/users/admin/users", capturedNotification.getLink());
    }
    
    @Test
    void getNotificationsForUser_ShouldReturnUserNotifications() {
        // Arrange
        List<NotificationEntity> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(notifications);
        
        // Act
        List<NotificationEntity> result = notificationService.getNotificationsForUser(testUser);
        
        // Assert
        assertEquals(notifications, result);
        verify(notificationRepository).findByUserOrderByCreatedAtDesc(testUser);
    }
    
    @Test
    void getUnreadNotificationsForUser_ShouldReturnUnreadNotifications() {
        // Arrange
        List<NotificationEntity> unreadNotifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(testUser)).thenReturn(unreadNotifications);
        
        // Act
        List<NotificationEntity> result = notificationService.getUnreadNotificationsForUser(testUser);
        
        // Assert
        assertEquals(unreadNotifications, result);
        verify(notificationRepository).findByUserAndReadFalseOrderByCreatedAtDesc(testUser);
    }
    
    @Test
    void getUnreadNotificationCount_ShouldReturnCount() {
        // Arrange
        when(notificationRepository.countByUserAndReadFalse(testUser)).thenReturn(5L);
        
        // Act
        long result = notificationService.getUnreadNotificationCount(testUser);
        
        // Assert
        assertEquals(5L, result);
        verify(notificationRepository).countByUserAndReadFalse(testUser);
    }
    
    @Test
    void markAsRead_ShouldMarkNotificationAsRead() {
        // Arrange
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(testNotification);
        
        // Act
        notificationService.markAsRead(1L);
        
        // Assert
        verify(notificationRepository).findById(1L);
        verify(notificationRepository).save(notificationCaptor.capture());
        
        NotificationEntity capturedNotification = notificationCaptor.getValue();
        assertTrue(capturedNotification.isRead());
    }
    
    @Test
    void markAllAsRead_ShouldMarkAllUserNotificationsAsRead() {
        // Arrange
        List<NotificationEntity> unreadNotifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(testUser)).thenReturn(unreadNotifications);
        when(notificationRepository.saveAll(anyList())).thenReturn(unreadNotifications);
        
        // Act
        notificationService.markAllAsRead(testUser);
        
        // Assert
        verify(notificationRepository).findByUserAndReadFalseOrderByCreatedAtDesc(testUser);
        verify(notificationRepository).saveAll(notificationsCaptor.capture());
        
        List<NotificationEntity> capturedNotifications = notificationsCaptor.getValue();
        assertEquals(1, capturedNotifications.size());
        assertTrue(capturedNotifications.get(0).isRead());
    }
    
    @Test
    void deleteNotification_ShouldDeleteNotification() {
        // Arrange
        doNothing().when(notificationRepository).deleteById(1L);
        
        // Act
        notificationService.deleteNotification(1L);
        
        // Assert
        verify(notificationRepository).deleteById(1L);
    }
    
    @Test
    void notifyUpcomingEvents_ShouldSendNotificationsForTomorrowEvents() {
        // Arrange
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime tomorrowEnd = tomorrow.plusDays(1);
        
        List<EventEntity> upcomingEvents = Arrays.asList(testEvent);
        when(eventRepository.findEventsBetweenDates(any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.APPROVED)))
                .thenReturn(upcomingEvents);
        
        // Act
        notificationService.notifyUpcomingEvents();
        
        // Assert
        verify(eventRepository).findEventsBetweenDates(any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.APPROVED));
        
        // Verify notifications in memory map - this is a bit tricky as it's internal state
        List<String> organizerNotifications = notificationService.getRecentNotifications(testOrganizer.getUsername());
        List<String> userNotifications = notificationService.getRecentNotifications(testUser.getUsername());
        
        assertFalse(organizerNotifications.isEmpty());
        assertFalse(userNotifications.isEmpty());
        assertTrue(organizerNotifications.get(0).contains("Your event"));
        assertTrue(userNotifications.get(0).contains("Reminder"));
    }
    
    @Test
    void notifyEventStatusChange_ShouldSendNotificationsForStatusChange() {
        // Arrange - Event with APPROVED status
        
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
    void notifyEventCreated_ShouldSendNotificationToOrganizer() {
        // Act
        notificationService.notifyEventCreated(testEvent);
        
        // Assert
        List<String> organizerNotifications = notificationService.getRecentNotifications(testOrganizer.getUsername());
        
        assertFalse(organizerNotifications.isEmpty());
        assertTrue(organizerNotifications.get(0).contains("has been created"));
    }
    
    @Test
    void notifyEventCancelled_ShouldSendNotificationToAttendees() {
        // Act
        notificationService.notifyEventCancelled(testEvent);
        
        // Assert
        List<String> userNotifications = notificationService.getRecentNotifications(testUser.getUsername());
        
        assertFalse(userNotifications.isEmpty());
        assertTrue(userNotifications.get(0).contains("has been cancelled"));
    }
    
    @Test
    void notifyUserJoinedEvent_ShouldSendNotificationsToOrganizerAndUser() {
        // Act
        notificationService.notifyUserJoinedEvent(testUser, testEvent);
        
        // Assert
        List<String> organizerNotifications = notificationService.getRecentNotifications(testOrganizer.getUsername());
        List<String> userNotifications = notificationService.getRecentNotifications(testUser.getUsername());
        
        assertFalse(organizerNotifications.isEmpty());
        assertFalse(userNotifications.isEmpty());
        assertTrue(organizerNotifications.get(0).contains("has joined your event"));
        assertTrue(userNotifications.get(0).contains("have successfully joined"));
    }
    
    @Test
    void getRecentNotifications_ShouldReturnNotifications() {
        // Pre-populate some notifications
        notificationService.notifyEventCreated(testEvent);
        
        // Act
        List<String> result = notificationService.getRecentNotifications(testOrganizer.getUsername());
        
        // Assert
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).contains("has been created"));
    }
} 