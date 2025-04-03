package com.softuni.event.controller.rest;

import com.softuni.event.service.EventReminderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for manually triggering event reminders (for testing)
 */
@RestController
@RequestMapping("/api/reminders")
public class EventReminderController {

    private static final Logger logger = LoggerFactory.getLogger(EventReminderController.class);
    private final EventReminderService reminderService;

    @Autowired
    public EventReminderController(EventReminderService reminderService) {
        this.reminderService = reminderService;
    }

    /**
     * Trigger reminders for a specific event (for testing)
     * Requires admin role
     */
    @PostMapping("/events/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> triggerEventReminder(@PathVariable(required = false) Long eventId) {
        logger.info("REST API: Received request to send reminder for event ID: {}", eventId);
        Map<String, Object> response = new HashMap<>();
        
        // Handle null or undefined ID
        if (eventId == null) {
            String errorMsg = "Invalid event ID: null or undefined";
            logger.error(errorMsg);
            response.put("success", false);
            response.put("message", errorMsg);
            return ResponseEntity.ok(response);
        }
        
        try {
            boolean success = reminderService.sendReminderForEvent(eventId);
            
            response.put("success", success);
            
            if (success) {
                String successMsg = "Reminders sent successfully for event ID: " + eventId;
                logger.info(successMsg);
                response.put("message", successMsg);
            } else {
                String errorMsg = "Event not found or not eligible for reminders";
                logger.error(errorMsg + ": {}", eventId);
                response.put("message", errorMsg);
            }
        } catch (Exception e) {
            String errorMsg = "Error: " + e.getMessage();
            logger.error("Failed to send reminder for event ID: {}", eventId, e);
            response.put("success", false);
            response.put("message", errorMsg);
        }
        
        // Always return 200 OK for testing purposes
        logger.info("REST API: Returning response for event ID {}: {}", eventId, response);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Manually trigger the scheduled event reminder job
     * Requires admin role
     */
    @PostMapping("/run-scheduled-job")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> runScheduledJob() {
        logger.info("REST API: Received request to run scheduled reminder job");
        Map<String, Object> response = new HashMap<>();
        
        try {
            int remindersSent = reminderService.processReminderSchedule();
            
            String successMsg = "Scheduled reminder job executed successfully. Sent " + remindersSent + " reminders.";
            logger.info(successMsg);
            response.put("success", true);
            response.put("message", successMsg);
            response.put("remindersSent", remindersSent);
            
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            logger.error("Failed to run scheduled reminder job", e);
            response.put("success", false);
            response.put("message", errorMsg);
        }
        
        // Always return 200 OK for testing purposes
        logger.info("REST API: Returning response for scheduled job: {}", response);
        return ResponseEntity.ok(response);
    }
} 