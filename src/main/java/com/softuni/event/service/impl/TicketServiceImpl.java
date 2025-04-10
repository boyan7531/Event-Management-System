package com.softuni.event.service.impl;

import com.softuni.event.exception.ResourceNotFoundException;
import com.softuni.event.model.dto.EventBasicDTO;
import com.softuni.event.model.dto.TicketDTO;
import com.softuni.event.model.dto.UserBasicDTO;
import com.softuni.event.model.entity.EventEntity;
import com.softuni.event.model.entity.TicketEntity;
import com.softuni.event.model.entity.UserEntity;
import com.softuni.event.repository.EventRepository;
import com.softuni.event.repository.TicketRepository;
import com.softuni.event.repository.UserRepository;
import com.softuni.event.service.TicketService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;

    public TicketServiceImpl(TicketRepository ticketRepository,
                            UserRepository userRepository,
                            EventRepository eventRepository,
                            ModelMapper modelMapper) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public TicketDTO createTicket(Long eventId, String username) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
        
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        
        // Check if the event location has capacity
        Integer locationCapacity = event.getLocation().getCapacity();
        if (locationCapacity != null) {
            Long currentTicketCount = ticketRepository.countTicketsByEventId(eventId);
            if (currentTicketCount >= locationCapacity) {
                throw new IllegalStateException("Cannot create ticket: Event has reached location capacity of " + locationCapacity);
            }
        }
        
        TicketEntity ticket = new TicketEntity();
        ticket.setTicketNumber(UUID.randomUUID().toString());
        ticket.setIssueDate(LocalDateTime.now());
        ticket.setUsed(false);
        ticket.setEvent(event);
        ticket.setUser(user);
        ticket.setCreatedAt(LocalDateTime.now());
        
        TicketEntity savedTicket = ticketRepository.save(ticket);
        
        return mapToDTO(savedTicket);
    }

    @Override
    public TicketDTO getTicketById(Long id) {
        TicketEntity ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));
        
        return mapToDTO(ticket);
    }

    @Override
    public TicketDTO getTicketByNumber(String ticketNumber) {
        TicketEntity ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "number", ticketNumber));
        
        return mapToDTO(ticket);
    }

    @Override
    public List<TicketDTO> getTicketsByEvent(Long eventId) {
        return ticketRepository.findByEventId(eventId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TicketDTO> getTicketsByUser(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        
        return ticketRepository.findByUserId(user.getId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean useTicket(String ticketNumber) {
        TicketEntity ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "number", ticketNumber));
        
        // Check if ticket is already used
        if (ticket.isUsed()) {
            return false;
        }
        
        // Check if event date has passed
        if (ticket.getEvent().getEventDate().isAfter(LocalDateTime.now())) {
            return false; // Can't use ticket before event
        }
        
        ticket.setUsed(true);
        ticket.setUpdatedAt(LocalDateTime.now());
        
        ticketRepository.save(ticket);
        
        return true;
    }

    @Override
    @Transactional
    public void deleteTicket(Long id) {
        TicketEntity ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));
        
        ticketRepository.delete(ticket);
    }
    
    private TicketDTO mapToDTO(TicketEntity ticket) {
        TicketDTO dto = modelMapper.map(ticket, TicketDTO.class);
        
        // Map event
        EventBasicDTO eventDTO = modelMapper.map(ticket.getEvent(), EventBasicDTO.class);
        dto.setEvent(eventDTO);
        
        // Map user
        UserBasicDTO userDTO = modelMapper.map(ticket.getUser(), UserBasicDTO.class);
        userDTO.setFullName(ticket.getUser().getFirstName() + " " + ticket.getUser().getLastName());
        dto.setUser(userDTO);
        
        return dto;
    }
} 