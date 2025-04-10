package com.softuni.event.service;

import com.softuni.event.model.dto.EventCreateDTO;
import com.softuni.event.model.dto.EventDetailDTO;
import com.softuni.event.model.enums.EventStatus;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface EventService {
    List<EventDetailDTO> getAllEvents();
    List<EventDetailDTO> getEventsByStatus(EventStatus status);
    List<EventDetailDTO> getUpcomingEvents();
    List<EventDetailDTO> getPastEvents();
    List<EventDetailDTO> searchEvents(String keyword);
    List<EventDetailDTO> getEventsByOrganizer(String username);
    List<EventDetailDTO> getJoinedEvents(String username);
    
    // Calendar feature method
    Map<Integer, List<EventDetailDTO>> getEventsByMonth(int year, Month month);
    
    // Location availability check
    boolean isLocationAvailable(Long locationId, LocalDateTime eventTime, Long excludeEventId);
    
    EventDetailDTO getEventById(Long id);
    EventDetailDTO getEventById(Long id, String username);
    Long createEvent(EventCreateDTO eventCreateDTO, String organizerUsername);
    void updateEvent(Long id, EventCreateDTO eventCreateDTO);
    void deleteEvent(Long id);
    
    void permanentlyDeleteEvent(Long id);
    
    void changeEventStatus(Long id, EventStatus status);
    boolean joinEvent(Long eventId, String username);
    boolean leaveEvent(Long eventId, String username);
    
    // Added method to get the event entity directly
    com.softuni.event.model.entity.EventEntity getEvent(Long id);
} 