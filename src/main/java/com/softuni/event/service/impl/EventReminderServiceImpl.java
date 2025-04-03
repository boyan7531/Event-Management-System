package com.softuni.event.service.impl;

import com.softuni.event.model.entity.EventEntity;
import com.softuni.event.model.entity.UserEntity;
import com.softuni.event.model.enums.EventStatus;
import com.softuni.event.model.enums.NotificationType;
import com.softuni.event.repository.EventRepository;
import com.softuni.event.service.EmailService;
import com.softuni.event.service.EventReminderService;
import com.softuni.event.service.NotificationService;
import com.softuni.event.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementation of EventReminderService for sending reminders about upcoming events
 */
@Service
public class EventReminderServiceImpl implements EventReminderService {

    private static final Logger logger = LoggerFactory.getLogger(EventReminderServiceImpl.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private final EventRepository eventRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final UserService userService;
    
    @Autowired
    public EventReminderServiceImpl(EventRepository eventRepository, 
                               NotificationService notificationService,
                               EmailService emailService,
                               UserService userService) {
        this.eventRepository = eventRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.userService = userService;
        logger.info("EventReminderService initialized");
    }
    
    /**
     * Send reminders 24 hours before events start
     * Runs every hour to check for events happening in the next 24 hours
     * @return Number of reminders sent
     */
    @Override
    public int processReminderSchedule() {
        logger.info("Running scheduled event reminder check");
        
        LocalDateTime now = LocalDateTime.now();
        
        // Find events that are happening in the next 1 hour window that are exactly 24 hours away
        LocalDateTime windowStart = now.plusHours(24).minusMinutes(30);
        LocalDateTime windowEnd = now.plusHours(24).plusMinutes(30);
        
        logger.debug("Looking for events between {} and {}", windowStart.format(formatter), windowEnd.format(formatter));
        
        List<EventEntity> upcomingEvents = eventRepository.findByEventDateBetweenAndStatus(
                windowStart, windowEnd, EventStatus.APPROVED);
        
        logger.info("Found {} events to send reminders for", upcomingEvents.size());
        
        int remindersSent = 0;
        for (EventEntity event : upcomingEvents) {
            if (sendRemindersForEvent(event)) {
                remindersSent++;
            }
        }
        
        logger.info("Successfully sent reminders for {} events", remindersSent);
        return remindersSent;
    }
    
    /**
     * Send reminders for a specific event
     * @return true if reminders were sent successfully
     */
    private boolean sendRemindersForEvent(EventEntity event) {
        try {
            if (event == null) {
                logger.error("Cannot send reminders for null event");
                return false;
            }
            
            logger.info("Sending reminders for event: {} (ID: {}, Status: {})", 
                    event.getTitle(), event.getId(), event.getStatus());
            
            // Notify organizer
            UserEntity organizer = event.getOrganizer();
            if (organizer == null) {
                logger.error("Event has no organizer: {}", event.getId());
                return false;
            }
            
            logger.debug("Sending reminder to organizer: {} ({})", organizer.getUsername(), organizer.getEmail());
            
            String organizerLink = "/events/details/" + event.getId();
            String organizerMessage = String.format(
                    "Reminder: Your event '%s' is happening in 24 hours (on %s).",
                    event.getTitle(), 
                    event.getEventDate().format(formatter)
            );
            
            try {
                // Send in-app notification
                notificationService.createNotification(
                        organizerMessage,
                        NotificationType.EVENT_REMINDER,
                        organizer,
                        organizerLink
                );
                logger.debug("In-app notification sent to organizer successfully");
            } catch (Exception e) {
                logger.error("Failed to send in-app notification to organizer", e);
                // Continue execution - we don't want to fail the entire process if just notifications fail
            }
            
            try {
                // Send email
                emailService.sendEventReminder(organizer, event, true);
                logger.debug("Email sent to organizer successfully");
            } catch (Exception e) {
                logger.error("Failed to send email to organizer", e);
                // Continue execution - we don't want to fail the entire process if just email fails
            }
            
            // Notify attendees
            if (event.getAttendees() != null && !event.getAttendees().isEmpty()) {
                logger.debug("Sending reminders to {} attendees", event.getAttendees().size());
                
                for (UserEntity attendee : event.getAttendees()) {
                    if (attendee == null) {
                        logger.warn("Null attendee found in event {}", event.getId());
                        continue;
                    }
                    
                    logger.debug("Sending reminder to attendee: {} ({})", 
                            attendee.getUsername(), attendee.getEmail());
                    
                    String attendeeLink = "/events/details/" + event.getId();
                    String attendeeMessage = String.format(
                            "Reminder: Event '%s' that you're attending is happening in 24 hours (on %s).",
                            event.getTitle(),
                            event.getEventDate().format(formatter)
                    );
                    
                    try {
                        // Send in-app notification
                        notificationService.createNotification(
                                attendeeMessage,
                                NotificationType.EVENT_REMINDER,
                                attendee,
                                attendeeLink
                        );
                    } catch (Exception e) {
                        logger.error("Failed to send in-app notification to attendee: {}", attendee.getUsername(), e);
                        // Continue with next attendee
                    }
                    
                    try {
                        // Send email
                        emailService.sendEventReminder(attendee, event, false);
                    } catch (Exception e) {
                        logger.error("Failed to send email to attendee: {}", attendee.getUsername(), e);
                        // Continue with next attendee
                    }
                }
            } else {
                logger.info("Event has no attendees: {}", event.getId());
            }
            
            logger.info("Successfully sent reminders for event ID: {}", event.getId());
            return true;
        } catch (Exception e) {
            logger.error("Error sending reminders for event ID: {}", event.getId(), e);
            return false;
        }
    }
    
    /**
     * Manually trigger reminders for an event
     * @param eventId The ID of the event to send reminders for
     * @return true if reminders were sent successfully, false if event was not found or not eligible
     */
    @Override
    public boolean sendReminderForEvent(Long eventId) {
        try {
            logger.info("Starting sendReminderForEvent for eventId: {}", eventId);
            
            // First check if the event exists at all
            boolean eventExists = eventRepository.existsById(eventId);
            if (!eventExists) {
                logger.error("Event with ID {} does not exist in the database", eventId);
                return false;
            }
            
            // Use the new method that eagerly loads all associations
            EventEntity event = eventRepository.findByIdWithAssociations(eventId);
            if (event == null) {
                logger.error("Event with ID {} could not be loaded with associations", eventId);
                return false;
            }
            
            logger.info("Retrieved event: ID={}, Title={}, Status={}, Date={}, Organizer={}",
                    event.getId(), 
                    event.getTitle(), 
                    event.getStatus(),
                    event.getEventDate(),
                    event.getOrganizer() != null ? event.getOrganizer().getUsername() : "null");
            
            // Force load attendees to ensure they're initialized
            if (event.getAttendees() != null) {
                logger.info("Event has {} attendees", event.getAttendees().size());
            } else {
                logger.warn("Event attendees collection is null");
            }
            
            // Remove status check to allow sending reminders for any event status
            logger.info("Proceeding to send reminder for event ID: {} with status: {}", eventId, event.getStatus());
            
            return sendRemindersForEvent(event);
        } catch (Exception e) {
            logger.error("Failed to send reminder for event ID: {}", eventId, e);
            return false;
        }
    }
} 