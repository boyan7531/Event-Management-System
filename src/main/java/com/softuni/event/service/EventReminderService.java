package com.softuni.event.service;

import org.springframework.scheduling.annotation.Scheduled;

/**
 * Service responsible for sending event reminders
 */
public interface EventReminderService {
    
    /**
     * Scheduled method to process reminders for upcoming events
     * @return Number of reminders sent
     */
    @Scheduled(cron = "${event.reminder.schedule:0 0 * * * *}")
    int processReminderSchedule();
    
    /**
     * Send a reminder for a specific event
     * @param eventId ID of the event
     * @return true if reminder was sent successfully, false if event was not found or not eligible
     */
    boolean sendReminderForEvent(Long eventId);
} 