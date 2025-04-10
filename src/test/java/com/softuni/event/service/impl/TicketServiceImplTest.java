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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Captor
    private ArgumentCaptor<TicketEntity> ticketCaptor;

    private UserEntity testUser;
    private EventEntity testEvent;
    private TicketEntity testTicket;
    private TicketDTO testTicketDTO;
    private UserBasicDTO testUserDTO;
    private EventBasicDTO testEventDTO;

    @BeforeEach
    void setUp() {
        // Set up test data
        testUser = new UserEntity();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("user@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        testEvent = new EventEntity();
        testEvent.setId(1L);
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setEventDate(LocalDateTime.now().plusDays(1));

        testTicket = new TicketEntity();
        testTicket.setId(1L);
        testTicket.setTicketNumber("TICKET-123");
        testTicket.setIssueDate(LocalDateTime.now().minusDays(1));
        testTicket.setUsed(false);
        testTicket.setEvent(testEvent);
        testTicket.setUser(testUser);
        testTicket.setCreatedAt(LocalDateTime.now().minusDays(1));

        testUserDTO = new UserBasicDTO();
        testUserDTO.setId(1L);
        testUserDTO.setUsername("testuser");
        testUserDTO.setFullName("Test User");

        testEventDTO = new EventBasicDTO();
        testEventDTO.setId(1L);
        testEventDTO.setTitle("Test Event");

        testTicketDTO = new TicketDTO();
        testTicketDTO.setId(1L);
        testTicketDTO.setTicketNumber("TICKET-123");
        testTicketDTO.setIssueDate(testTicket.getIssueDate());
        testTicketDTO.setUsed(false);
        testTicketDTO.setEvent(testEventDTO);
        testTicketDTO.setUser(testUserDTO);
    }

    @Test
    void createTicket_ShouldCreateAndReturnTicket() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(ticketRepository.save(any(TicketEntity.class))).thenReturn(testTicket);
        when(modelMapper.map(any(TicketEntity.class), eq(TicketDTO.class))).thenReturn(testTicketDTO);
        when(modelMapper.map(testEvent, EventBasicDTO.class)).thenReturn(testEventDTO);
        when(modelMapper.map(testUser, UserBasicDTO.class)).thenReturn(testUserDTO);

        // Act
        TicketDTO result = ticketService.createTicket(1L, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals(testTicketDTO, result);
        
        verify(eventRepository).findById(1L);
        verify(userRepository).findByUsername("testuser");
        verify(ticketRepository).save(ticketCaptor.capture());
        
        TicketEntity capturedTicket = ticketCaptor.getValue();
        assertNotNull(capturedTicket.getTicketNumber());
        assertNotNull(capturedTicket.getIssueDate());
        assertFalse(capturedTicket.isUsed());
        assertEquals(testEvent, capturedTicket.getEvent());
        assertEquals(testUser, capturedTicket.getUser());
    }

    @Test
    void createTicket_ShouldThrowException_WhenEventNotFound() {
        // Arrange
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            ticketService.createTicket(999L, "testuser");
        });
        
        assertEquals("Event not found with id: '999'", exception.getMessage());
        verify(eventRepository).findById(999L);
        verifyNoInteractions(userRepository, ticketRepository, modelMapper);
    }

    @Test
    void createTicket_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            ticketService.createTicket(1L, "unknownuser");
        });
        
        assertEquals("User not found with username: 'unknownuser'", exception.getMessage());
        verify(eventRepository).findById(1L);
        verify(userRepository).findByUsername("unknownuser");
        verifyNoInteractions(ticketRepository, modelMapper);
    }

    @Test
    void createTicket_ShouldThrowException_WhenLocationCapacityReached() {
        // Arrange
        testEvent.getLocation().setCapacity(10); // Set capacity to 10
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(ticketRepository.countTicketsByEventId(1L)).thenReturn(10L); // Current tickets equal to capacity
        
        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            ticketService.createTicket(1L, "testuser");
        });
        
        assertEquals("Cannot create ticket: Event has reached location capacity of 10", exception.getMessage());
        verify(eventRepository).findById(1L);
        verify(userRepository).findByUsername("testuser");
        verify(ticketRepository).countTicketsByEventId(1L);
        verifyNoMoreInteractions(ticketRepository);
    }

    @Test
    void getTicketById_ShouldReturnTicket_WhenFound() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(modelMapper.map(testTicket, TicketDTO.class)).thenReturn(testTicketDTO);
        when(modelMapper.map(testEvent, EventBasicDTO.class)).thenReturn(testEventDTO);
        when(modelMapper.map(testUser, UserBasicDTO.class)).thenReturn(testUserDTO);

        // Act
        TicketDTO result = ticketService.getTicketById(1L);

        // Assert
        assertEquals(testTicketDTO, result);
        verify(ticketRepository).findById(1L);
    }

    @Test
    void getTicketById_ShouldThrowException_WhenNotFound() {
        // Arrange
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            ticketService.getTicketById(999L);
        });
        
        assertEquals("Ticket not found with id: '999'", exception.getMessage());
        verify(ticketRepository).findById(999L);
    }

    @Test
    void getTicketByNumber_ShouldReturnTicket_WhenFound() {
        // Arrange
        when(ticketRepository.findByTicketNumber("TICKET-123")).thenReturn(Optional.of(testTicket));
        when(modelMapper.map(testTicket, TicketDTO.class)).thenReturn(testTicketDTO);
        when(modelMapper.map(testEvent, EventBasicDTO.class)).thenReturn(testEventDTO);
        when(modelMapper.map(testUser, UserBasicDTO.class)).thenReturn(testUserDTO);

        // Act
        TicketDTO result = ticketService.getTicketByNumber("TICKET-123");

        // Assert
        assertEquals(testTicketDTO, result);
        verify(ticketRepository).findByTicketNumber("TICKET-123");
    }

    @Test
    void getTicketByNumber_ShouldThrowException_WhenNotFound() {
        // Arrange
        when(ticketRepository.findByTicketNumber("INVALID-TICKET")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            ticketService.getTicketByNumber("INVALID-TICKET");
        });
        
        assertEquals("Ticket not found with number: 'INVALID-TICKET'", exception.getMessage());
        verify(ticketRepository).findByTicketNumber("INVALID-TICKET");
    }

    @Test
    void getTicketsByEvent_ShouldReturnEventTickets() {
        // Arrange
        List<TicketEntity> tickets = Arrays.asList(testTicket);
        when(ticketRepository.findByEventId(1L)).thenReturn(tickets);
        when(modelMapper.map(testTicket, TicketDTO.class)).thenReturn(testTicketDTO);
        when(modelMapper.map(testEvent, EventBasicDTO.class)).thenReturn(testEventDTO);
        when(modelMapper.map(testUser, UserBasicDTO.class)).thenReturn(testUserDTO);

        // Act
        List<TicketDTO> result = ticketService.getTicketsByEvent(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testTicketDTO, result.get(0));
        verify(ticketRepository).findByEventId(1L);
    }

    @Test
    void getTicketsByUser_ShouldReturnUserTickets() {
        // Arrange
        List<TicketEntity> tickets = Arrays.asList(testTicket);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(ticketRepository.findByUserId(1L)).thenReturn(tickets);
        when(modelMapper.map(testTicket, TicketDTO.class)).thenReturn(testTicketDTO);
        when(modelMapper.map(testEvent, EventBasicDTO.class)).thenReturn(testEventDTO);
        when(modelMapper.map(testUser, UserBasicDTO.class)).thenReturn(testUserDTO);

        // Act
        List<TicketDTO> result = ticketService.getTicketsByUser("testuser");

        // Assert
        assertEquals(1, result.size());
        assertEquals(testTicketDTO, result.get(0));
        verify(userRepository).findByUsername("testuser");
        verify(ticketRepository).findByUserId(1L);
    }

    @Test
    void getTicketsByUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            ticketService.getTicketsByUser("unknownuser");
        });
        
        assertEquals("User not found with username: 'unknownuser'", exception.getMessage());
        verify(userRepository).findByUsername("unknownuser");
        verifyNoInteractions(ticketRepository);
    }

    @Test
    void useTicket_ShouldMarkTicketAsUsed_WhenValid() {
        // Arrange
        // Change event date to be in the past
        testEvent.setEventDate(LocalDateTime.now().minusDays(1));
        testTicket.setEvent(testEvent);
        
        when(ticketRepository.findByTicketNumber("TICKET-123")).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(TicketEntity.class))).thenReturn(testTicket);

        // Act
        boolean result = ticketService.useTicket("TICKET-123");

        // Assert
        assertTrue(result);
        verify(ticketRepository).findByTicketNumber("TICKET-123");
        verify(ticketRepository).save(ticketCaptor.capture());
        
        TicketEntity savedTicket = ticketCaptor.getValue();
        assertTrue(savedTicket.isUsed());
        assertNotNull(savedTicket.getUpdatedAt());
    }

    @Test
    void useTicket_ShouldReturnFalse_WhenTicketAlreadyUsed() {
        // Arrange
        testTicket.setUsed(true);
        when(ticketRepository.findByTicketNumber("TICKET-123")).thenReturn(Optional.of(testTicket));

        // Act
        boolean result = ticketService.useTicket("TICKET-123");

        // Assert
        assertFalse(result);
        verify(ticketRepository).findByTicketNumber("TICKET-123");
        verifyNoMoreInteractions(ticketRepository);
    }

    @Test
    void useTicket_ShouldReturnFalse_WhenEventInFuture() {
        // Arrange
        // Event date already set to future in setUp
        when(ticketRepository.findByTicketNumber("TICKET-123")).thenReturn(Optional.of(testTicket));

        // Act
        boolean result = ticketService.useTicket("TICKET-123");

        // Assert
        assertFalse(result);
        verify(ticketRepository).findByTicketNumber("TICKET-123");
        verifyNoMoreInteractions(ticketRepository);
    }

    @Test
    void useTicket_ShouldThrowException_WhenTicketNotFound() {
        // Arrange
        when(ticketRepository.findByTicketNumber("INVALID-TICKET")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            ticketService.useTicket("INVALID-TICKET");
        });
        
        assertEquals("Ticket not found with number: 'INVALID-TICKET'", exception.getMessage());
        verify(ticketRepository).findByTicketNumber("INVALID-TICKET");
    }

    @Test
    void deleteTicket_ShouldDeleteTicket_WhenExists() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        doNothing().when(ticketRepository).delete(testTicket);

        // Act
        ticketService.deleteTicket(1L);

        // Assert
        verify(ticketRepository).findById(1L);
        verify(ticketRepository).delete(testTicket);
    }

    @Test
    void deleteTicket_ShouldThrowException_WhenTicketNotFound() {
        // Arrange
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            ticketService.deleteTicket(999L);
        });
        
        assertEquals("Ticket not found with id: '999'", exception.getMessage());
        verify(ticketRepository).findById(999L);
        verifyNoMoreInteractions(ticketRepository);
    }
} 