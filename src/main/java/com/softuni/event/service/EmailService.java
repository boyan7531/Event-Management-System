package com.softuni.event.service;

import com.softuni.event.model.entity.EventEntity;
import com.softuni.event.model.entity.UserEntity;

/**
 * Service for sending emails to users
 */
public interface EmailService {
    
    /**
     * Send a simple email message
     * @param to The recipient's email address
     * @param subject The email subject
     * @param text The email body text
     */
    void sendSimpleMessage(String to, String subject, String text);
    
    /**
     * Send an HTML email message
     * @param to The recipient's email address
     * @param subject The email subject
     * @param htmlContent The email HTML content
     */
    void sendHtmlMessage(String to, String subject, String htmlContent);
    
    /**
     * Send a reminder email for an event
     * @param user The user to send the reminder to
     * @param event The event to remind about
     * @param isOrganizer Whether the user is the event organizer
     */
    void sendEventReminder(UserEntity user, EventEntity event, boolean isOrganizer);
} 