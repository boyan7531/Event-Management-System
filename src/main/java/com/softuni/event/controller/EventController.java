package com.softuni.event.controller;

import com.softuni.event.model.dto.EventCreateDTO;
import com.softuni.event.model.dto.EventDetailDTO;
import com.softuni.event.model.enums.EventStatus;
import com.softuni.event.service.EventService;
import com.softuni.event.service.LocationService;
import com.softuni.event.service.TicketService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final LocationService locationService;
    private final TicketService ticketService;

    public EventController(EventService eventService, LocationService locationService, TicketService ticketService) {
        this.eventService = eventService;
        this.locationService = locationService;
        this.ticketService = ticketService;
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
            return "redirect:/events/details/" + eventId;
        } catch (Exception e) {
            // Handle the case when user is not found in the database or other errors
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
    public String approveEvent(@PathVariable Long id) {
        eventService.changeEventStatus(id, EventStatus.APPROVED);
        return "redirect:/events/details/" + id;
    }

    @PostMapping("/{id}/reject")
    public String rejectEvent(@PathVariable Long id) {
        eventService.changeEventStatus(id, EventStatus.REJECTED);
        return "redirect:/events/details/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancelEvent(@PathVariable Long id) {
        eventService.deleteEvent(id); // This will set status to CANCELED
        return "redirect:/events/my-events";
    }

    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        EventDetailDTO event = eventService.getEventById(id);
        
        // Only allow organizer to delete their own event
        if (!event.getOrganizer().getUsername().equals(userDetails.getUsername())) {
            throw new RuntimeException("You are not authorized to delete this event");
        }
        
        eventService.permanentlyDeleteEvent(id);
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
        
        eventService.updateEvent(id, eventForm);
        return "redirect:/events/details/" + id;
    }
} 