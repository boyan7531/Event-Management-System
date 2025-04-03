package com.softuni.event.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.softuni.event.model.entity.EventEntity;
import com.softuni.event.model.enums.EventStatus;
import com.softuni.event.repository.EventRepository;
import com.softuni.event.service.EventReminderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventReminderService eventReminderService;

    @GetMapping
    public String adminDashboard() {
        // Redirect to the users admin page
        return "redirect:/users/admin/users";
    }

    /**
     * Displays the admin reminders dashboard with upcoming events
     */
    @GetMapping("/reminders")
    public String reminderDashboard(Model model) {
        // Get upcoming events for the next 7 days
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextWeek = now.plusDays(7);
        
        List<EventEntity> upcomingEvents = eventRepository.findByEventDateBetweenAndStatus(
                now, nextWeek, EventStatus.APPROVED);
        
        model.addAttribute("upcomingEvents", upcomingEvents);
        return "admin-reminders";
    }

    /**
     * Triggers sending reminders for a specific event
     */
    @PostMapping("/reminders/event/{id}")
    public String sendReminderForEvent(@PathVariable(required = false) Long id, RedirectAttributes redirectAttributes) {
        logger.info("Admin dashboard: trying to send reminder for event #{}", id);
        
        // Handle null or undefined ID
        if (id == null) {
            String errorMsg = "Invalid event ID: null or undefined";
            logger.error(errorMsg);
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/admin/reminders";
        }
        
        try {
            boolean success = eventReminderService.sendReminderForEvent(id);
            if (success) {
                String successMsg = "Reminder emails successfully sent for event #" + id;
                logger.info(successMsg);
                redirectAttributes.addFlashAttribute("successMessage", successMsg);
            } else {
                String errorMsg = "Event #" + id + " not found or not eligible for reminders";
                logger.error(errorMsg);
                redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            }
        } catch (Exception e) {
            String errorMsg = "Error sending reminder: " + e.getMessage();
            logger.error(errorMsg, e);
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
        }
        
        return "redirect:/admin/reminders";
    }

    /**
     * Triggers the scheduled reminder job manually
     */
    @PostMapping("/reminders/run-job")
    public String runReminderJob(RedirectAttributes redirectAttributes) {
        logger.info("Admin dashboard: trying to run reminder job");
        try {
            int remindersSent = eventReminderService.processReminderSchedule();
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Reminder job executed successfully. Sent " + remindersSent + " reminders.");
        } catch (Exception e) {
            String errorMsg = "Error running reminder job: " + e.getMessage();
            logger.error(errorMsg, e);
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
        }
        
        return "redirect:/admin/reminders";
    }
} 