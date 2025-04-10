package com.softuni.event.service.impl;

import com.softuni.event.model.entity.EventEntity;
import com.softuni.event.model.entity.LocationEntity;
import com.softuni.event.model.entity.NotificationEntity;
import com.softuni.event.model.entity.UserEntity;
import com.softuni.event.model.enums.EventStatus;
import com.softuni.event.model.enums.NotificationType;
import com.softuni.event.repository.EventRepository;
import com.softuni.event.service.EmailService;
import com.softuni.event.service.NotificationService;
import com.softuni.event.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventReminderServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

    @InjectMocks
    private EventReminderServiceImpl eventReminderService;

    private EventEntity testEvent;
    private UserEntity organizer;
    private UserEntity attendee;
    private LocationEntity location;
    private LocalDateTime tomorrow;

    @BeforeEach
    void setUp() {
        // Set up common test data
        tomorrow = LocalDateTime.now().plusDays(1);
        
        organizer = new UserEntity();
        organizer.setId(1L);
        organizer.setUsername("organizer");
        organizer.setEmail("organizer@example.com");
        organizer.setFirstName("Event");
        organizer.setLastName("Organizer");

        attendee = new UserEntity();
        attendee.setId(2L);
        attendee.setUsername("attendee");
        attendee.setEmail("attendee@example.com");
        attendee.setFirstName("Test");
        attendee.setLastName("Attendee");

        Set<UserEntity> attendees = new HashSet<>();
        attendees.add(attendee);

        location = new LocationEntity();
        location.setId(1L);
        location.setName("Test Location");
        location.setAddress("123 Test St");
        location.setCity("Test City");

        testEvent = new EventEntity();
        testEvent.setId(1L);
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setEventDate(tomorrow);
        testEvent.setOrganizer(organizer);
        testEvent.setLocation(location);
        testEvent.setAttendees(attendees);
        testEvent.setStatus(EventStatus.APPROVED);
    }

    @Test
    void processReminderSchedule_ShouldSendRemindersForUpcomingEvents() {
        // Arrange
        LocalDateTime windowStart = tomorrow.minusMinutes(30);
        LocalDateTime windowEnd = tomorrow.plusMinutes(30);
        
        when(eventRepository.findByEventDateBetweenAndStatus(any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.APPROVED)))
            .thenReturn(List.of(testEvent));
        
        NotificationEntity mockNotification = new NotificationEntity();
        when(notificationService.createNotification(anyString(), any(NotificationType.class), any(UserEntity.class), anyString()))
            .thenReturn(mockNotification);
            
        doNothing().when(emailService).sendEventReminder(any(UserEntity.class), any(EventEntity.class), anyBoolean());
        
        // Act
        int remindersCount = eventReminderService.processReminderSchedule();
        
        // Assert
        assertEquals(1, remindersCount);
        verify(eventRepository).findByEventDateBetweenAndStatus(any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.APPROVED));
        verify(notificationService, times(2)).createNotification(anyString(), eq(NotificationType.EVENT_REMINDER), any(UserEntity.class), anyString());
        verify(emailService).sendEventReminder(eq(organizer), eq(testEvent), eq(true));
        verify(emailService).sendEventReminder(eq(attendee), eq(testEvent), eq(false));
    }

    @Test
    void processReminderSchedule_ShouldReturnZeroWhenNoUpcomingEvents() {
        // Arrange
        when(eventRepository.findByEventDateBetweenAndStatus(any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.APPROVED)))
            .thenReturn(Collections.emptyList());
        
        // Act
        int remindersCount = eventReminderService.processReminderSchedule();
        
        // Assert
        assertEquals(0, remindersCount);
        verify(eventRepository).findByEventDateBetweenAndStatus(any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.APPROVED));
        verifyNoInteractions(notificationService, emailService);
    }

    @Test
    void sendReminderForEvent_ShouldSendRemindersForExistingEvent() {
        // Arrange
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(eventRepository.findByIdWithAssociations(1L)).thenReturn(testEvent);
        
        NotificationEntity mockNotification = new NotificationEntity();
        when(notificationService.createNotification(anyString(), any(NotificationType.class), any(UserEntity.class), anyString()))
            .thenReturn(mockNotification);
            
        doNothing().when(emailService).sendEventReminder(any(UserEntity.class), any(EventEntity.class), anyBoolean());
        
        // Act
        boolean result = eventReminderService.sendReminderForEvent(1L);
        
        // Assert
        assertTrue(result);
        verify(eventRepository).existsById(1L);
        verify(eventRepository).findByIdWithAssociations(1L);
        verify(notificationService, times(2)).createNotification(anyString(), eq(NotificationType.EVENT_REMINDER), any(UserEntity.class), anyString());
        verify(emailService).sendEventReminder(eq(organizer), eq(testEvent), eq(true));
        verify(emailService).sendEventReminder(eq(attendee), eq(testEvent), eq(false));
    }

    @Test
    void sendReminderForEvent_ShouldReturnFalseWhenEventDoesNotExist() {
        // Arrange
        when(eventRepository.existsById(999L)).thenReturn(false);
        
        // Act
        boolean result = eventReminderService.sendReminderForEvent(999L);
        
        // Assert
        assertFalse(result);
        verify(eventRepository).existsById(999L);
        verifyNoMoreInteractions(eventRepository);
        verifyNoInteractions(notificationService, emailService);
    }

    @Test
    void sendReminderForEvent_ShouldReturnFalseWhenEventCantBeLoadedWithAssociations() {
        // Arrange
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(eventRepository.findByIdWithAssociations(1L)).thenReturn(null);
        
        // Act
        boolean result = eventReminderService.sendReminderForEvent(1L);
        
        // Assert
        assertFalse(result);
        verify(eventRepository).existsById(1L);
        verify(eventRepository).findByIdWithAssociations(1L);
        verifyNoInteractions(notificationService, emailService);
    }

    @Test
    void sendReminderForEvent_ShouldHandleEventWithNoAttendees() {
        // Arrange
        EventEntity eventWithNoAttendees = new EventEntity();
        eventWithNoAttendees.setId(2L);
        eventWithNoAttendees.setTitle("Event With No Attendees");
        eventWithNoAttendees.setEventDate(tomorrow);
        eventWithNoAttendees.setOrganizer(organizer);
        eventWithNoAttendees.setLocation(location);
        eventWithNoAttendees.setAttendees(new HashSet<>());
        eventWithNoAttendees.setStatus(EventStatus.APPROVED);
        
        when(eventRepository.existsById(2L)).thenReturn(true);
        when(eventRepository.findByIdWithAssociations(2L)).thenReturn(eventWithNoAttendees);
        
        NotificationEntity mockNotification = new NotificationEntity();
        when(notificationService.createNotification(anyString(), any(NotificationType.class), any(UserEntity.class), anyString()))
            .thenReturn(mockNotification);
            
        doNothing().when(emailService).sendEventReminder(any(UserEntity.class), any(EventEntity.class), anyBoolean());
        
        // Act
        boolean result = eventReminderService.sendReminderForEvent(2L);
        
        // Assert
        assertTrue(result);
        verify(eventRepository).existsById(2L);
        verify(eventRepository).findByIdWithAssociations(2L);
        verify(notificationService).createNotification(anyString(), eq(NotificationType.EVENT_REMINDER), eq(organizer), anyString());
        verify(emailService).sendEventReminder(eq(organizer), eq(eventWithNoAttendees), eq(true));
        // No attendee-related calls should be made
        verify(notificationService, times(1)).createNotification(anyString(), any(), any(), anyString());
        verify(emailService, times(1)).sendEventReminder(any(), any(), anyBoolean());
    }

    @Test
    void sendReminderForEvent_ShouldContinueWhenNotificationServiceFails() {
        // Arrange
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(eventRepository.findByIdWithAssociations(1L)).thenReturn(testEvent);
        
        // Notification service fails for organizer
        doThrow(new RuntimeException("Test exception")).when(notificationService).createNotification(
            anyString(), eq(NotificationType.EVENT_REMINDER), eq(organizer), anyString());
        
        // Email service works fine
        doNothing().when(emailService).sendEventReminder(any(UserEntity.class), any(EventEntity.class), anyBoolean());
        
        // Notification service works for attendee
        NotificationEntity mockAttendeeNotification = new NotificationEntity();
        when(notificationService.createNotification(
            anyString(), eq(NotificationType.EVENT_REMINDER), eq(attendee), anyString()))
            .thenReturn(mockAttendeeNotification);
        
        // Act
        boolean result = eventReminderService.sendReminderForEvent(1L);
        
        // Assert
        assertTrue(result);
        verify(eventRepository).existsById(1L);
        verify(eventRepository).findByIdWithAssociations(1L);
        
        // Verify notification service was called for both users
        verify(notificationService).createNotification(anyString(), eq(NotificationType.EVENT_REMINDER), eq(organizer), anyString());
        verify(notificationService).createNotification(anyString(), eq(NotificationType.EVENT_REMINDER), eq(attendee), anyString());
        
        // Verify emails were sent to both users
        verify(emailService).sendEventReminder(eq(organizer), eq(testEvent), eq(true));
        verify(emailService).sendEventReminder(eq(attendee), eq(testEvent), eq(false));
    }
} 