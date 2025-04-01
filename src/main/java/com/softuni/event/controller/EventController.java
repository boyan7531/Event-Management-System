package com.softuni.event.controller;

import com.softuni.event.model.dto.EventCreateDTO;
import com.softuni.event.model.dto.EventDetailDTO;
import com.softuni.event.model.entity.EventEntity;
import com.softuni.event.model.enums.EventStatus;
import com.softuni.event.model.enums.NotificationType;
import com.softuni.event.service.EventService;
import com.softuni.event.service.LocationService;
import com.softuni.event.service.NotificationService;
import com.softuni.event.service.TicketService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final LocationService locationService;
    private final TicketService ticketService;
    private final NotificationService notificationService;

    public EventController(EventService eventService, LocationService locationService, TicketService ticketService, NotificationService notificationService) {
        this.eventService = eventService;
        this.locationService = locationService;
        this.ticketService = ticketService;
        this.notificationService = notificationService;
    }

    @GetMapping("/details/{id}")
    public String eventDetails(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        EventDetailDTO event;
        if (userDetails != null) {
            event = eventService.getEventById(id, userDetails.getUsername());
        } else {
            event = eventService.getEventById(id);
        }
        model.addAttribute("event", event);
        return "event-details";
    }

    @GetMapping("/create")
    public String createEventForm(Model model) {
        model.addAttribute("eventForm", new EventCreateDTO());
        model.addAttribute("locations", locationService.getAllLocations());
        return "event-create";
    }

    @PostMapping("/create")
    public String createEvent(@Valid @ModelAttribute("eventForm") EventCreateDTO eventForm,
                              BindingResult bindingResult,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        if (userDetails == null) {
            // User is not authenticated
            return "redirect:/users/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("locations", locationService.getAllLocations());
            return "event-create";
        }

        try {
            Long eventId = eventService.createEvent(eventForm, userDetails.getUsername());
            
            // Create notification for admin about the new pending event
            EventEntity event = eventService.getEvent(eventId);
            notificationService.createPendingEventNotification(event);
            
            return "redirect:/events/details/" + eventId;
        } catch (IllegalStateException e) {
            // Handle location availability error
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("locations", locationService.getAllLocations());
            return "event-create";
        } catch (Exception e) {
            // Handle other errors
            model.addAttribute("errorMessage", "Error creating event: " + e.getMessage());
            model.addAttribute("locations", locationService.getAllLocations());
            return "event-create";
        }
    }

    @GetMapping("/my-events")
    public String myEvents(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("organizedEvents", eventService.getEventsByOrganizer(userDetails.getUsername()));
        model.addAttribute("joinedEvents", eventService.getJoinedEvents(userDetails.getUsername()));
        return "my-events";
    }

    @PostMapping("/{id}/join")
    public String joinEvent(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails) {
        eventService.joinEvent(id, userDetails.getUsername());
        return "redirect:/events/details/" + id;
    }

    @PostMapping("/{id}/leave")
    public String leaveEvent(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails userDetails) {
        eventService.leaveEvent(id, userDetails.getUsername());
        return "redirect:/events/details/" + id;
    }

    @PostMapping("/{id}/approve")
    public String approveEvent(@PathVariable Long id, @RequestParam(required = false) String redirectUrl) {
        EventEntity event = eventService.getEvent(id);
        eventService.changeEventStatus(id, EventStatus.APPROVED);
        
        // Create notification for the event creator
        notificationService.createEventStatusNotification(event, NotificationType.EVENT_APPROVED);
        
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            return "redirect:" + redirectUrl;
        }
        
        return "redirect:/events/details/" + id;
    }

    @PostMapping("/{id}/reject")
    public String rejectEvent(@PathVariable Long id, @RequestParam(required = false) String redirectUrl) {
        EventEntity event = eventService.getEvent(id);
        eventService.changeEventStatus(id, EventStatus.REJECTED);
        
        // Create notification for the event creator
        notificationService.createEventStatusNotification(event, NotificationType.EVENT_REJECTED);
        
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            return "redirect:" + redirectUrl;
        }
        
        return "redirect:/events/details/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancelEvent(@PathVariable Long id, @RequestParam(required = false) String redirectUrl) {
        EventEntity event = eventService.getEvent(id);
        eventService.deleteEvent(id); // This will set status to CANCELED
        
        // Create notification for the event creator
        notificationService.createEventStatusNotification(event, NotificationType.EVENT_CANCELED);
        
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            return "redirect:" + redirectUrl;
        }
        
        return "redirect:/events/my-events";
    }

    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable Long id, 
                             @AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam(required = false) String redirectUrl) {
        EventDetailDTO event = eventService.getEventById(id);
        
        // Check if user is admin or the event organizer
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        // Only allow organizer or admin to delete the event
        if (!isAdmin && !event.getOrganizer().getUsername().equals(userDetails.getUsername())) {
            throw new RuntimeException("You are not authorized to delete this event");
        }
        
        eventService.permanentlyDeleteEvent(id);
        
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/events/my-events";
    }

    @GetMapping("/search")
    public String searchEvents(@RequestParam String keyword, Model model) {
        model.addAttribute("events", eventService.searchEvents(keyword));
        model.addAttribute("keyword", keyword);
        return "search-results";
    }

    @GetMapping("/edit/{id}")
    public String editEventForm(@PathVariable Long id, Model model) {
        EventDetailDTO event = eventService.getEventById(id);
        EventCreateDTO eventForm = new EventCreateDTO();
        
        // Map fields from detail DTO to create DTO for editing
        eventForm.setTitle(event.getTitle());
        eventForm.setDescription(event.getDescription());
        eventForm.setEventDate(event.getEventDate());
        eventForm.setRegistrationDeadline(event.getRegistrationDeadline());
        eventForm.setPaid(event.isPaid());
        eventForm.setTicketPrice(event.getTicketPrice());
        eventForm.setAvailableTickets(event.getAvailableTickets());
        eventForm.setLocationId(event.getLocation().getId());
        
        model.addAttribute("eventForm", eventForm);
        model.addAttribute("eventId", id);
        model.addAttribute("locations", locationService.getAllLocations());
        
        return "event-edit";
    }
    
    @PostMapping("/edit/{id}")
    public String updateEvent(@PathVariable Long id,
                             @Valid @ModelAttribute("eventForm") EventCreateDTO eventForm,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("eventId", id);
            model.addAttribute("locations", locationService.getAllLocations());
            return "event-edit";
        }
        
        try {
            eventService.updateEvent(id, eventForm);
            return "redirect:/events/details/" + id;
        } catch (IllegalStateException e) {
            // Handle location availability error
            model.addAttribute("eventId", id);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("locations", locationService.getAllLocations());
            return "event-edit";
        } catch (Exception e) {
            // Handle other errors
            model.addAttribute("eventId", id);
            model.addAttribute("errorMessage", "Error updating event: " + e.getMessage());
            model.addAttribute("locations", locationService.getAllLocations());
            return "event-edit";
        }
    }

    @GetMapping("/calendar")
    public String showCalendar(
            @RequestParam(required = false, defaultValue = "#{T(java.time.LocalDate).now().getYear()}") Integer year,
            @RequestParam(required = false) Integer month,
            Model model) {
        
        // If month is not provided, use current month
        if (month == null) {
            month = java.time.LocalDate.now().getMonthValue();
        }
        
        // Validate month range
        if (month < 1 || month > 12) {
            month = java.time.LocalDate.now().getMonthValue();
        }
        
        // Get Month enum from month number
        Month currentMonth = Month.of(month);
        
        // Get year/month for calculations
        YearMonth yearMonth = YearMonth.of(year, month);
        
        // Get events for the month
        Map<Integer, List<EventDetailDTO>> eventsByDay = eventService.getEventsByMonth(year, currentMonth);
        
        // Add all necessary data to the model
        model.addAttribute("currentYear", year);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("daysInMonth", yearMonth.lengthOfMonth());
        model.addAttribute("eventsByDay", eventsByDay);
        model.addAttribute("firstDayOfMonth", yearMonth.atDay(1).getDayOfWeek().getValue());
        
        // For navigation
        model.addAttribute("prevMonth", month > 1 ? month - 1 : 12);
        model.addAttribute("prevYear", month > 1 ? year : year - 1);
        model.addAttribute("nextMonth", month < 12 ? month + 1 : 1);
        model.addAttribute("nextYear", month < 12 ? year : year + 1);
        
        return "event-calendar";
    }
} 