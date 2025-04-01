package com.softuni.event.service.impl;

import com.softuni.event.model.entity.EventEntity;
import com.softuni.event.model.entity.NotificationEntity;
import com.softuni.event.model.entity.UserEntity;
import com.softuni.event.model.enums.EventStatus;
import com.softuni.event.model.enums.NotificationType;
import com.softuni.event.repository.EventRepository;
import com.softuni.event.repository.NotificationRepository;
import com.softuni.event.service.NotificationService;
import com.softuni.event.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

@Service
@Primary
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int MAX_NOTIFICATIONS = 20;
    
    private final EventRepository eventRepository;
    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final Map<String, Deque<String>> userNotifications = new ConcurrentHashMap<>();

    public NotificationServiceImpl(EventRepository eventRepository, NotificationRepository notificationRepository, UserService userService) {
        this.eventRepository = eventRepository;
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    @Override
    @Scheduled(cron = "0 0 8 * * *") // Run at 8:00 AM every day
    public void notifyUpcomingEvents() {
        logger.info("Checking for upcoming events to send notifications...");
        
        // Get events that are happening tomorrow
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime tomorrowEnd = tomorrow.plusDays(1);
        
        List<EventEntity> upcomingEvents = eventRepository.findEventsBetweenDates(
                tomorrow, tomorrowEnd, EventStatus.APPROVED);
        
        for (EventEntity event : upcomingEvents) {
            // Notify organizer
            String organizerMessage = String.format("Your event '%s' is happening tomorrow at %s",
                    event.getTitle(), event.getEventDate().format(formatter));
            addNotification(event.getOrganizer().getUsername(), organizerMessage);
            
            // Notify attendees
            String attendeeMessage = String.format("Reminder: Event '%s' that you're attending is tomorrow at %s",
                    event.getTitle(), event.getEventDate().format(formatter));
            
            for (UserEntity attendee : event.getAttendees()) {
                addNotification(attendee.getUsername(), attendeeMessage);
            }
            
            logger.info("Sent notifications for upcoming event: {}", event.getTitle());
        }
    }

    @Override
    public void notifyEventStatusChange(EventEntity event) {
        String message = String.format("Event '%s' status has been changed to %s",
                event.getTitle(), event.getStatus());
        
        // Notify organizer
        addNotification(event.getOrganizer().getUsername(), message);
        
        // Notify attendees if the event was approved or canceled
        if (event.getStatus() == EventStatus.APPROVED || event.getStatus() == EventStatus.CANCELED) {
            for (UserEntity attendee : event.getAttendees()) {
                addNotification(attendee.getUsername(), message);
            }
        }
        
        logger.info("Sent notifications for event status change: {} -> {}", 
                event.getTitle(), event.getStatus());
    }

    @Override
    public void notifyEventCreated(EventEntity event) {
        String message = String.format("Your event '%s' has been created and is pending approval",
                event.getTitle());
        
        addNotification(event.getOrganizer().getUsername(), message);
        logger.info("Sent notification for event creation: {}", event.getTitle());
    }

    @Override
    public void notifyEventCancelled(EventEntity event) {
        String message = String.format("Event '%s' scheduled for %s has been cancelled",
                event.getTitle(), event.getEventDate().format(formatter));
        
        // Notify all attendees
        for (UserEntity attendee : event.getAttendees()) {
            addNotification(attendee.getUsername(), message);
        }
        
        logger.info("Sent notifications for event cancellation: {}", event.getTitle());
    }

    @Override
    public void notifyUserJoinedEvent(UserEntity user, EventEntity event) {
        // Notify event organizer
        String organizerMessage = String.format("User %s has joined your event '%s'",
                user.getUsername(), event.getTitle());
        addNotification(event.getOrganizer().getUsername(), organizerMessage);
        
        // Notify user
        String userMessage = String.format("You have successfully joined the event '%s'",
                event.getTitle());
        addNotification(user.getUsername(), userMessage);
        
        logger.info("Sent notifications for user joining event: {} -> {}", 
                user.getUsername(), event.getTitle());
    }

    @Override
    public List<String> getRecentNotifications(String username) {
        Deque<String> notifications = userNotifications.getOrDefault(username, new LinkedList<>());
        return new ArrayList<>(notifications);
    }
    
    private void addNotification(String username, String message) {
        userNotifications.computeIfAbsent(username, k -> new LinkedList<>());
        Deque<String> userDeque = userNotifications.get(username);
        
        // Add notification with timestamp
        LocalDateTime now = LocalDateTime.now();
        String timeStamp = now.format(formatter);
        userDeque.addFirst(timeStamp + " - " + message);
        
        // Keep only the most recent notifications
        while (userDeque.size() > MAX_NOTIFICATIONS) {
            userDeque.removeLast();
        }
    }

    @Override
    public NotificationEntity createNotification(String message, NotificationType type, UserEntity user, String link) {
        NotificationEntity notification = new NotificationEntity(message, type, user, link);
        return notificationRepository.save(notification);
    }

    @Override
    public void createEventStatusNotification(EventEntity event, NotificationType type) {
        UserEntity creator = event.getOrganizer();
        String message = "";
        String link = "/events/details/" + event.getId();
        
        switch (type) {
            case EVENT_APPROVED -> message = "Your event \"" + event.getTitle() + "\" has been approved.";
            case EVENT_REJECTED -> message = "Your event \"" + event.getTitle() + "\" has been rejected.";
            case EVENT_CANCELED -> message = "Your event \"" + event.getTitle() + "\" has been canceled.";
            default -> message = "There's an update to your event \"" + event.getTitle() + "\".";
        }
        
        createNotification(message, type, creator, link);
    }

    @Override
    public void createPendingEventNotification(EventEntity event) {
        List<UserEntity> admins = userService.getAllAdmins();
        String message = "New event \"" + event.getTitle() + "\" by " + event.getOrganizer().getUsername() + " is waiting for approval.";
        String link = "/users/admin/users";

        for (UserEntity admin : admins) {
            createNotification(message, NotificationType.NEW_EVENT_PENDING, admin, link);
        }
    }

    @Override
    public List<NotificationEntity> getNotificationsForUser(UserEntity user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<NotificationEntity> getUnreadNotificationsForUser(UserEntity user) {
        return notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user);
    }

    @Override
    public long getUnreadNotificationCount(UserEntity user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }

    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    @Override
    public void markAllAsRead(UserEntity user) {
        List<NotificationEntity> unreadNotifications = getUnreadNotificationsForUser(user);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
} 