package com.softuni.event.controller.rest;

import com.softuni.event.model.dto.EventDetailDTO;
import com.softuni.event.model.enums.EventStatus;
import com.softuni.event.service.EventService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventRestController {

    private final EventService eventService;

    public EventRestController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<EventDetailDTO>> getAllEvents() {
        List<EventDetailDTO> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }
    
    @GetMapping(value = "/check-location-availability", 
                produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<Map<String, Object>> checkLocationAvailability(
            @RequestParam Long locationId,
            @RequestParam String eventDateTime,
            @RequestParam(required = false) Long excludeEventId) {
        
        try {
            // Parse the date time string
            LocalDateTime eventTime = LocalDateTime.parse(eventDateTime);
            
            // Check availability
            boolean isAvailable = eventService.isLocationAvailable(locationId, eventTime, excludeEventId);
            
            // Create a simple map response instead of complex object
            Map<String, Object> response = new HashMap<>();
            response.put("available", isAvailable);
            response.put("locationId", locationId);
            response.put("eventDateTime", eventDateTime);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/upcoming")
    public ResponseEntity<List<EventDetailDTO>> getUpcomingEvents() {
        List<EventDetailDTO> events = eventService.getUpcomingEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/search")
    public ResponseEntity<List<EventDetailDTO>> searchEvents(@RequestParam String keyword) {
        List<EventDetailDTO> events = eventService.searchEvents(keyword);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/by-status")
    public ResponseEntity<List<EventDetailDTO>> getEventsByStatus(@RequestParam EventStatus status) {
        List<EventDetailDTO> events = eventService.getEventsByStatus(status);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDetailDTO> getEventById(@PathVariable Long id) {
        EventDetailDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }
} 