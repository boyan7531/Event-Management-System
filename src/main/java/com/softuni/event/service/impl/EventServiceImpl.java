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
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

    public EventServiceImpl(EventRepository eventRepository, 
                           UserRepository userRepository, 
                           LocationRepository locationRepository, 
                           ModelMapper modelMapper, 
                           NotificationService notificationService) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.modelMapper = modelMapper;
        this.notificationService = notificationService;
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
    public EventDetailDTO getEventById(Long id, String username) {
        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
        
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        EventDetailDTO dto = mapToDetailDTO(event);
        
        // Check if the user has joined this event
        dto.setJoinedByCurrentUser(event.getAttendees().contains(user));
        
        return dto;
    }

    @Override
    @Transactional
    public Long createEvent(EventCreateDTO eventCreateDTO, String organizerUsername) {
        UserEntity organizer = userRepository.findByUsername(organizerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        LocationEntity location = locationRepository.findById(eventCreateDTO.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", eventCreateDTO.getLocationId()));
        
        // Check if location is available at the specified time
        if (!isLocationAvailable(eventCreateDTO.getLocationId(), eventCreateDTO.getEventDate(), null)) {
            throw new IllegalStateException("The location is already booked at this time. Please choose a different time or location.");
        }
        
        // Check if registration deadline is after event date
        if (eventCreateDTO.getRegistrationDeadline() != null && 
            eventCreateDTO.getRegistrationDeadline().isAfter(eventCreateDTO.getEventDate())) {
            throw new IllegalStateException("Registration deadline cannot be after the event date.");
        }
        
        // Validate that available tickets doesn't exceed location capacity
        if (eventCreateDTO.getAvailableTickets() != null && location.getCapacity() != null && 
            eventCreateDTO.getAvailableTickets() > location.getCapacity()) {
            throw new IllegalStateException("Available tickets cannot exceed the location capacity of " + location.getCapacity() + ".");
        }
        
        EventEntity eventEntity = new EventEntity();
        eventEntity.setTitle(eventCreateDTO.getTitle());
        eventEntity.setDescription(eventCreateDTO.getDescription());
        eventEntity.setEventDate(eventCreateDTO.getEventDate());
        eventEntity.setRegistrationDeadline(eventCreateDTO.getRegistrationDeadline());
        eventEntity.setPaid(eventCreateDTO.isPaid());
        eventEntity.setTicketPrice(eventCreateDTO.getTicketPrice());
        eventEntity.setAvailableTickets(eventCreateDTO.getAvailableTickets());
        
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
        
        // Check if location is available at the specified time
        // Only check if date or location has changed
        boolean dateChanged = !event.getEventDate().equals(eventCreateDTO.getEventDate());
        boolean locationChanged = !event.getLocation().getId().equals(eventCreateDTO.getLocationId());
        
        if ((dateChanged || locationChanged) && 
            !isLocationAvailable(eventCreateDTO.getLocationId(), eventCreateDTO.getEventDate(), id)) {
            throw new IllegalStateException("The location is already booked at this time. Please choose a different time or location.");
        }
        
        // Check if registration deadline is after event date
        if (eventCreateDTO.getRegistrationDeadline() != null && 
            eventCreateDTO.getRegistrationDeadline().isAfter(eventCreateDTO.getEventDate())) {
            throw new IllegalStateException("Registration deadline cannot be after the event date.");
        }
        
        // Validate that available tickets doesn't exceed location capacity
        if (eventCreateDTO.getAvailableTickets() != null && location.getCapacity() != null && 
            eventCreateDTO.getAvailableTickets() > location.getCapacity()) {
            throw new IllegalStateException("Available tickets cannot exceed the location capacity of " + location.getCapacity() + ".");
        }
        
        event.setTitle(eventCreateDTO.getTitle());
        event.setDescription(eventCreateDTO.getDescription());
        event.setEventDate(eventCreateDTO.getEventDate());
        event.setRegistrationDeadline(eventCreateDTO.getRegistrationDeadline());
        event.setPaid(eventCreateDTO.isPaid());
        event.setTicketPrice(eventCreateDTO.getTicketPrice());
        event.setAvailableTickets(eventCreateDTO.getAvailableTickets());
        event.setLocation(location);
        event.setUpdatedAt(LocalDateTime.now());
        
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
    public void permanentlyDeleteEvent(Long id) {
        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
        
        // First, notify attendees about deletion
        notificationService.notifyEventCancelled(event);
        
        // Remove all attendees (clear the many-to-many relationship)
        event.getAttendees().clear();
        
        // Delete the event
        eventRepository.delete(event);
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
        
        // Check if user is the organizer
        if (event.getOrganizer().getId().equals(user.getId())) {
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

    @Override
    public Map<Integer, List<EventDetailDTO>> getEventsByMonth(int year, Month month) {
        // Get the start and end date for the given month
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        
        // Fetch all approved events for the month
        List<EventEntity> monthEvents = eventRepository.findByEventDateBetweenAndStatus(
                startOfMonth, 
                endOfMonth, 
                EventStatus.APPROVED
        );
        
        // Group events by day of month
        return monthEvents.stream()
                .map(this::mapToDetailDTO)
                .collect(Collectors.groupingBy(
                        event -> event.getEventDate().getDayOfMonth()
                ));
    }

    @Override
    public boolean isLocationAvailable(Long locationId, LocalDateTime eventTime, Long excludeEventId) {
        // Calculate event end time (assume events last 3 hours)
        LocalDateTime eventEndTime = eventTime.plusHours(3);
        
        // Statuses to check for conflicts - we only care about APPROVED and PENDING events
        List<EventStatus> conflictStatuses = List.of(EventStatus.APPROVED, EventStatus.PENDING);
        
        // Check for overlapping events
        List<EventEntity> overlappingEvents = eventRepository.findOverlappingEvents(
            locationId,
            eventTime,
            eventEndTime,
            excludeEventId != null ? excludeEventId : -1L,
            conflictStatuses
        );
        
        // Location is available if there are no overlapping events
        return overlappingEvents.isEmpty();
    }
    
    @Override
    public EventEntity getEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
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
        
        return dto;
    }
} 