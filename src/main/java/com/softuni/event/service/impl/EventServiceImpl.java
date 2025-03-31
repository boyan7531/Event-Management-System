package com.softuni.event.service.impl;

import com.softuni.event.exception.ResourceNotFoundException;
import com.softuni.event.exception.UnauthorizedAccessException;
import com.softuni.event.model.dto.EventCreateDTO;
import com.softuni.event.model.dto.EventDetailDTO;
import com.softuni.event.model.dto.LocationBasicDTO;
import com.softuni.event.model.dto.UserBasicDTO;
import com.softuni.event.model.entity.EventEntity;
import com.softuni.event.model.entity.LocationEntity;
import com.softuni.event.model.entity.UserEntity;
import com.softuni.event.model.enums.EventStatus;
import com.softuni.event.repository.EventRepository;
import com.softuni.event.repository.LocationRepository;
import com.softuni.event.repository.UserRepository;
import com.softuni.event.service.EventService;
import com.softuni.event.service.NotificationService;
import com.softuni.event.service.WeatherService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;
    private final WeatherService weatherService;

    public EventServiceImpl(EventRepository eventRepository, 
                           UserRepository userRepository, 
                           LocationRepository locationRepository, 
                           ModelMapper modelMapper, 
                           NotificationService notificationService, 
                           WeatherService weatherService) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.modelMapper = modelMapper;
        this.notificationService = notificationService;
        this.weatherService = weatherService;
    }

    @Override
    public List<EventDetailDTO> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::mapToDetailDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventDetailDTO> getEventsByStatus(EventStatus status) {
        return eventRepository.findByStatus(status).stream()
                .map(this::mapToDetailDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventDetailDTO> getUpcomingEvents() {
        return eventRepository.findUpcomingEvents(LocalDateTime.now(), EventStatus.APPROVED).stream()
                .map(this::mapToDetailDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventDetailDTO> getPastEvents() {
        return eventRepository.findPastEvents(LocalDateTime.now(), EventStatus.APPROVED).stream()
                .map(this::mapToDetailDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventDetailDTO> searchEvents(String keyword) {
        return eventRepository.searchByTitleOrDescription(keyword).stream()
                .map(this::mapToDetailDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventDetailDTO> getEventsByOrganizer(String username) {
        UserEntity organizer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return eventRepository.findByOrganizerId(organizer.getId()).stream()
                .map(this::mapToDetailDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventDetailDTO> getJoinedEvents(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return user.getAttendedEvents().stream()
                .map(this::mapToDetailDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EventDetailDTO getEventById(Long id) {
        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
        
        return mapToDetailDTO(event);
    }

    @Override
    @Transactional
    public Long createEvent(EventCreateDTO eventCreateDTO, String organizerUsername) {
        UserEntity organizer = userRepository.findByUsername(organizerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        LocationEntity location = locationRepository.findById(eventCreateDTO.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", eventCreateDTO.getLocationId()));
        
        EventEntity eventEntity = new EventEntity();
        eventEntity.setTitle(eventCreateDTO.getTitle());
        eventEntity.setDescription(eventCreateDTO.getDescription());
        eventEntity.setEventDate(eventCreateDTO.getEventDate());
        eventEntity.setRegistrationDeadline(eventCreateDTO.getRegistrationDeadline());
        eventEntity.setPaid(eventCreateDTO.isPaid());
        eventEntity.setTicketPrice(eventCreateDTO.getTicketPrice());
        eventEntity.setAvailableTickets(eventCreateDTO.getAvailableTickets());
        
        // Handle image upload if needed
        // if (eventCreateDTO.getImage() != null && !eventCreateDTO.getImage().isEmpty()) {
        //     String imageUrl = uploadImage(eventCreateDTO.getImage());
        //     eventEntity.setImageUrl(imageUrl);
        // }
        
        eventEntity.setStatus(EventStatus.PENDING); // New events are pending by default
        eventEntity.setOrganizer(organizer);
        eventEntity.setLocation(location);
        eventEntity.setCreatedAt(LocalDateTime.now());
        eventEntity.setAttendees(new HashSet<>());
        
        EventEntity savedEvent = eventRepository.save(eventEntity);
        
        // Notify about the event creation
        notificationService.notifyEventCreated(savedEvent);
        
        return savedEvent.getId();
    }

    @Override
    @Transactional
    public void updateEvent(Long id, EventCreateDTO eventCreateDTO) {
        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
        
        LocationEntity location = locationRepository.findById(eventCreateDTO.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", eventCreateDTO.getLocationId()));
        
        event.setTitle(eventCreateDTO.getTitle());
        event.setDescription(eventCreateDTO.getDescription());
        event.setEventDate(eventCreateDTO.getEventDate());
        event.setRegistrationDeadline(eventCreateDTO.getRegistrationDeadline());
        event.setPaid(eventCreateDTO.isPaid());
        event.setTicketPrice(eventCreateDTO.getTicketPrice());
        event.setAvailableTickets(eventCreateDTO.getAvailableTickets());
        event.setLocation(location);
        event.setUpdatedAt(LocalDateTime.now());
        
        // Handle image upload if needed
        // if (eventCreateDTO.getImage() != null && !eventCreateDTO.getImage().isEmpty()) {
        //     String imageUrl = uploadImage(eventCreateDTO.getImage());
        //     event.setImageUrl(imageUrl);
        // }
        
        eventRepository.save(event);
    }

    @Override
    @Transactional
    public void deleteEvent(Long id) {
        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
        
        // Cancel the event instead of deleting it
        event.setStatus(EventStatus.CANCELED);
        event.setUpdatedAt(LocalDateTime.now());
        
        eventRepository.save(event);
        
        // Notify attendees about cancellation
        notificationService.notifyEventCancelled(event);
    }

    @Override
    @Transactional
    public void changeEventStatus(Long id, EventStatus status) {
        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
        
        event.setStatus(status);
        event.setUpdatedAt(LocalDateTime.now());
        
        eventRepository.save(event);
        
        // Notify about status change
        notificationService.notifyEventStatusChange(event);
    }

    @Override
    @Transactional
    public boolean joinEvent(Long eventId, String username) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
        
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Check if event is approved
        if (event.getStatus() != EventStatus.APPROVED) {
            return false;
        }
        
        // Check if registration deadline has passed
        if (event.getRegistrationDeadline() != null && 
            event.getRegistrationDeadline().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        // Check if event has available tickets
        if (event.getAvailableTickets() != null && event.getAvailableTickets() <= 0) {
            return false;
        }
        
        // Check if user is already attending
        if (event.getAttendees().contains(user)) {
            return false;
        }
        
        // Add user to event attendees
        event.getAttendees().add(user);
        
        // Reduce available tickets if applicable
        if (event.getAvailableTickets() != null) {
            event.setAvailableTickets(event.getAvailableTickets() - 1);
        }
        
        eventRepository.save(event);
        
        // Notify about user joining event
        notificationService.notifyUserJoinedEvent(user, event);
        
        return true;
    }

    @Override
    @Transactional
    public boolean leaveEvent(Long eventId, String username) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
        
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Check if user is attending the event
        if (!event.getAttendees().contains(user)) {
            return false;
        }
        
        // Check if event date has already passed
        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        // Remove user from event attendees
        event.getAttendees().remove(user);
        
        // Increase available tickets if applicable
        if (event.getAvailableTickets() != null) {
            event.setAvailableTickets(event.getAvailableTickets() + 1);
        }
        
        eventRepository.save(event);
        
        return true;
    }
    
    private EventDetailDTO mapToDetailDTO(EventEntity event) {
        EventDetailDTO dto = modelMapper.map(event, EventDetailDTO.class);
        
        // Map organizer
        UserBasicDTO organizer = modelMapper.map(event.getOrganizer(), UserBasicDTO.class);
        organizer.setFullName(event.getOrganizer().getFirstName() + " " + event.getOrganizer().getLastName());
        dto.setOrganizer(organizer);
        
        // Map location
        LocationBasicDTO location = modelMapper.map(event.getLocation(), LocationBasicDTO.class);
        dto.setLocation(location);
        
        // Map attendees
        Set<UserBasicDTO> attendees = event.getAttendees().stream()
                .map(user -> {
                    UserBasicDTO userDTO = modelMapper.map(user, UserBasicDTO.class);
                    userDTO.setFullName(user.getFirstName() + " " + user.getLastName());
                    return userDTO;
                })
                .collect(Collectors.toSet());
        dto.setAttendees(attendees);
        dto.setTotalAttendees(attendees.size());
        
        // Get weather forecast if event is upcoming
        if (event.getEventDate().isAfter(LocalDateTime.now()) && 
            event.getLocation().getCity() != null) {
            String forecast = weatherService.getWeatherForecast(
                    event.getLocation().getCity(), 
                    event.getEventDate());
            dto.setWeatherForecast(forecast);
        }
        
        return dto;
    }
} 