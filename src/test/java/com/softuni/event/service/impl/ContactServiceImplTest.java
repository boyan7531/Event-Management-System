package com.softuni.event.service.impl;

import com.softuni.event.model.dto.ContactMessageDTO;
import com.softuni.event.model.entity.ContactMessageEntity;
import com.softuni.event.repository.ContactMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    @Mock
    private ContactMessageRepository contactMessageRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ContactServiceImpl contactService;

    private ContactMessageEntity testMessage1;
    private ContactMessageEntity testMessage2;
    private ContactMessageDTO testMessageDTO1;
    private ContactMessageDTO testMessageDTO2;

    @BeforeEach
    void setUp() {
        // Set up test data
        testMessage1 = new ContactMessageEntity(
                "John Doe",
                "john@example.com",
                "Test Subject 1",
                "Test Message 1"
        );
        testMessage1.setId(1L);
        testMessage1.setCreatedAt(LocalDateTime.now().minusDays(1));
        testMessage1.setRead(false);

        testMessage2 = new ContactMessageEntity(
                "Jane Smith",
                "jane@example.com",
                "Test Subject 2",
                "Test Message 2"
        );
        testMessage2.setId(2L);
        testMessage2.setCreatedAt(LocalDateTime.now());
        testMessage2.setRead(true);

        testMessageDTO1 = new ContactMessageDTO();
        testMessageDTO1.setId(1L);
        testMessageDTO1.setName("John Doe");
        testMessageDTO1.setEmail("john@example.com");
        testMessageDTO1.setSubject("Test Subject 1");
        testMessageDTO1.setMessage("Test Message 1");
        testMessageDTO1.setCreatedAt(testMessage1.getCreatedAt());
        testMessageDTO1.setRead(false);

        testMessageDTO2 = new ContactMessageDTO();
        testMessageDTO2.setId(2L);
        testMessageDTO2.setName("Jane Smith");
        testMessageDTO2.setEmail("jane@example.com");
        testMessageDTO2.setSubject("Test Subject 2");
        testMessageDTO2.setMessage("Test Message 2");
        testMessageDTO2.setCreatedAt(testMessage2.getCreatedAt());
        testMessageDTO2.setRead(true);

        // Configure mapper with lenient stubbing to avoid UnnecessaryStubbingException
        lenient().when(modelMapper.map(testMessage1, ContactMessageDTO.class)).thenReturn(testMessageDTO1);
        lenient().when(modelMapper.map(testMessage2, ContactMessageDTO.class)).thenReturn(testMessageDTO2);
    }

    @Test
    void saveContactMessage_ShouldSaveMessage() {
        // Arrange
        String name = "Test Name";
        String email = "test@example.com";
        String subject = "Test Subject";
        String message = "Test Message";

        when(contactMessageRepository.save(any(ContactMessageEntity.class))).thenAnswer(invocation -> {
            ContactMessageEntity savedMessage = invocation.getArgument(0);
            savedMessage.setId(3L);
            return savedMessage;
        });

        // Act
        contactService.saveContactMessage(name, email, subject, message);

        // Assert
        verify(contactMessageRepository).save(argThat(entity -> 
            entity.getName().equals(name) &&
            entity.getEmail().equals(email) &&
            entity.getSubject().equals(subject) &&
            entity.getMessage().equals(message) &&
            !entity.isRead()
        ));
    }

    @Test
    void getAllMessages_ShouldReturnAllMessagesOrderedByCreatedAtDesc() {
        // Arrange
        List<ContactMessageEntity> messages = Arrays.asList(testMessage2, testMessage1);
        when(contactMessageRepository.findAllByOrderByCreatedAtDesc()).thenReturn(messages);

        // Act
        List<ContactMessageDTO> result = contactService.getAllMessages();

        // Assert
        assertEquals(2, result.size());
        assertEquals(testMessageDTO2, result.get(0));
        assertEquals(testMessageDTO1, result.get(1));
        verify(contactMessageRepository).findAllByOrderByCreatedAtDesc();
        verify(modelMapper, times(2)).map(any(ContactMessageEntity.class), eq(ContactMessageDTO.class));
    }

    @Test
    void getUnreadMessages_ShouldReturnUnreadMessagesOrderedByCreatedAtDesc() {
        // Arrange
        List<ContactMessageEntity> unreadMessages = Arrays.asList(testMessage1);
        when(contactMessageRepository.findByReadOrderByCreatedAtDesc(false)).thenReturn(unreadMessages);

        // Act
        List<ContactMessageDTO> result = contactService.getUnreadMessages();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testMessageDTO1, result.get(0));
        verify(contactMessageRepository).findByReadOrderByCreatedAtDesc(false);
        verify(modelMapper).map(any(ContactMessageEntity.class), eq(ContactMessageDTO.class));
    }

    @Test
    void getMessageById_ShouldReturnMessageWhenFound() {
        // Arrange
        when(contactMessageRepository.findById(1L)).thenReturn(Optional.of(testMessage1));

        // Act
        ContactMessageDTO result = contactService.getMessageById(1L);

        // Assert
        assertEquals(testMessageDTO1, result);
        verify(contactMessageRepository).findById(1L);
        verify(modelMapper).map(testMessage1, ContactMessageDTO.class);
    }

    @Test
    void getMessageById_ShouldThrowExceptionWhenNotFound() {
        // Arrange
        when(contactMessageRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            contactService.getMessageById(999L);
        });

        assertEquals("Message not found with id: 999", exception.getMessage());
        verify(contactMessageRepository).findById(999L);
    }

    @Test
    void markAsRead_ShouldMarkMessageAsReadWhenFound() {
        // Arrange
        when(contactMessageRepository.findById(1L)).thenReturn(Optional.of(testMessage1));
        when(contactMessageRepository.save(testMessage1)).thenReturn(testMessage1);

        // Act
        contactService.markAsRead(1L);

        // Assert
        assertTrue(testMessage1.isRead());
        verify(contactMessageRepository).findById(1L);
        verify(contactMessageRepository).save(testMessage1);
    }

    @Test
    void markAsRead_ShouldThrowExceptionWhenNotFound() {
        // Arrange
        when(contactMessageRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            contactService.markAsRead(999L);
        });

        assertEquals("Message not found with id: 999", exception.getMessage());
        verify(contactMessageRepository).findById(999L);
        verify(contactMessageRepository, never()).save(any());
    }

    @Test
    void deleteMessage_ShouldDeleteMessage() {
        // Arrange
        doNothing().when(contactMessageRepository).deleteById(1L);

        // Act
        contactService.deleteMessage(1L);

        // Assert
        verify(contactMessageRepository).deleteById(1L);
    }

    @Test
    void countUnreadMessages_ShouldReturnCountOfUnreadMessages() {
        // Arrange
        when(contactMessageRepository.countByRead(false)).thenReturn(5L);

        // Act
        long result = contactService.countUnreadMessages();

        // Assert
        assertEquals(5L, result);
        verify(contactMessageRepository).countByRead(false);
    }
} 