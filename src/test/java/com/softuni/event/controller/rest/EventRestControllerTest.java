package com.softuni.event.controller.rest;

import com.softuni.event.model.dto.EventDetailDTO;
import com.softuni.event.model.enums.EventStatus;
import com.softuni.event.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventRestController.class)
@WithMockUser
class EventRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Test
    void getAllEvents_ReturnsEventsList() throws Exception {
        // Create mock events
        EventDetailDTO event1 = new EventDetailDTO();
        event1.setId(1L);
        event1.setTitle("Test Event 1");
        
        EventDetailDTO event2 = new EventDetailDTO();
        event2.setId(2L);
        event2.setTitle("Test Event 2");
        
        when(eventService.getAllEvents()).thenReturn(List.of(event1, event2));

        mockMvc.perform(get("/api/events")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Event 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Test Event 2"));
    }

    @Test
    void checkLocationAvailability_Available() throws Exception {
        when(eventService.isLocationAvailable(eq(1L), any(LocalDateTime.class), isNull()))
                .thenReturn(true);

        mockMvc.perform(get("/api/events/check-location-availability")
                .param("locationId", "1")
                .param("eventDateTime", "2025-10-15T14:00:00")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.locationId").value(1))
                .andExpect(jsonPath("$.eventDateTime").value("2025-10-15T14:00:00"));
    }

    @Test
    void checkLocationAvailability_NotAvailable() throws Exception {
        when(eventService.isLocationAvailable(eq(1L), any(LocalDateTime.class), eq(2L)))
                .thenReturn(false);

        mockMvc.perform(get("/api/events/check-location-availability")
                .param("locationId", "1")
                .param("eventDateTime", "2025-10-15T14:00:00")
                .param("excludeEventId", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.locationId").value(1))
                .andExpect(jsonPath("$.eventDateTime").value("2025-10-15T14:00:00"));
    }

    @Test
    void checkLocationAvailability_InvalidData() throws Exception {
        when(eventService.isLocationAvailable(anyLong(), any(LocalDateTime.class), any()))
                .thenThrow(new RuntimeException("Invalid location"));

        mockMvc.perform(get("/api/events/check-location-availability")
                .param("locationId", "999")
                .param("eventDateTime", "2025-10-15T14:00:00")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid location"));
    }

    @Test
    void getUpcomingEvents_ReturnsEventsList() throws Exception {
        EventDetailDTO event1 = new EventDetailDTO();
        event1.setId(1L);
        event1.setTitle("Upcoming Event 1");
        
        EventDetailDTO event2 = new EventDetailDTO();
        event2.setId(2L);
        event2.setTitle("Upcoming Event 2");
        
        when(eventService.getUpcomingEvents()).thenReturn(List.of(event1, event2));

        mockMvc.perform(get("/api/events/upcoming")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Upcoming Event 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Upcoming Event 2"));
    }

    @Test
    void searchEvents_ReturnsMatchingEvents() throws Exception {
        EventDetailDTO event1 = new EventDetailDTO();
        event1.setId(1L);
        event1.setTitle("Conference");
        
        EventDetailDTO event2 = new EventDetailDTO();
        event2.setId(2L);
        event2.setTitle("Workshop");
        
        when(eventService.searchEvents("work")).thenReturn(List.of(event2));

        mockMvc.perform(get("/api/events/search")
                .param("keyword", "work")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].title").value("Workshop"));
    }

    @Test
    void getEventsByStatus_ReturnsFilteredEvents() throws Exception {
        EventDetailDTO event1 = new EventDetailDTO();
        event1.setId(1L);
        event1.setTitle("Active Event");
        event1.setStatus(EventStatus.APPROVED);
        
        when(eventService.getEventsByStatus(EventStatus.APPROVED)).thenReturn(List.of(event1));

        mockMvc.perform(get("/api/events/by-status")
                .param("status", "APPROVED")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Active Event"))
                .andExpect(jsonPath("$[0].status").value("APPROVED"));
    }

    @Test
    void getEventById_ReturnsEvent() throws Exception {
        EventDetailDTO event = new EventDetailDTO();
        event.setId(1L);
        event.setTitle("Specific Event");
        event.setDescription("Event description");
        
        when(eventService.getEventById(1L)).thenReturn(event);

        mockMvc.perform(get("/api/events/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Specific Event"))
                .andExpect(jsonPath("$.description").value("Event description"));
    }
} 