package com.softuni.event.service.impl;

import com.softuni.event.model.entity.EventEntity;
import com.softuni.event.model.entity.LocationEntity;
import com.softuni.event.model.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender emailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> simpleMailMessageCaptor;

    private UserEntity testUser;
    private EventEntity testEvent;
    private String fromAddress = "test@example.com";

    @BeforeEach
    void setUp() {
        // Set up test data
        ReflectionTestUtils.setField(emailService, "fromAddress", fromAddress);
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);

        testUser = new UserEntity();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("user@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        UserEntity organizer = new UserEntity();
        organizer.setId(2L);
        organizer.setUsername("organizer");
        organizer.setEmail("organizer@example.com");
        organizer.setFirstName("Event");
        organizer.setLastName("Organizer");

        LocationEntity location = new LocationEntity();
        location.setId(1L);
        location.setName("Test Location");
        location.setAddress("123 Test St");
        location.setCity("Test City");

        testEvent = new EventEntity();
        testEvent.setId(1L);
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setEventDate(LocalDateTime.now().plusDays(1));
        testEvent.setOrganizer(organizer);
        testEvent.setLocation(location);
        testEvent.setAttendees(new HashSet<>());
    }

    @Test
    void sendSimpleMessage_ShouldSendEmail() {
        // Arrange
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String text = "Test Message";

        doNothing().when(emailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendSimpleMessage(to, subject, text);

        // Assert
        verify(emailSender).send(simpleMailMessageCaptor.capture());
        SimpleMailMessage capturedMessage = simpleMailMessageCaptor.getValue();
        
        assertEquals(fromAddress, capturedMessage.getFrom());
        assertEquals(to, capturedMessage.getTo()[0]);
        assertEquals(subject, capturedMessage.getSubject());
        assertEquals(text, capturedMessage.getText());
    }

    @Test
    void sendSimpleMessage_WhenDisabled_ShouldNotSendEmail() {
        // Arrange
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);
        
        // Act
        emailService.sendSimpleMessage("test@example.com", "Subject", "Message");
        
        // Assert
        verify(emailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendHtmlMessage_ShouldSendHtmlEmail() throws MessagingException {
        // Arrange
        String to = "recipient@example.com";
        String subject = "Test HTML Subject";
        String htmlContent = "<html><body><h1>Test</h1></body></html>";
        
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(emailSender).send(any(MimeMessage.class));
        
        // Act
        emailService.sendHtmlMessage(to, subject, htmlContent);
        
        // Assert
        verify(emailSender).createMimeMessage();
        verify(emailSender).send(mimeMessage);
    }

    @Test
    void sendHtmlMessage_WhenDisabled_ShouldNotSendEmail() {
        // Arrange
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);
        
        // Act
        emailService.sendHtmlMessage("test@example.com", "Subject", "<html></html>");
        
        // Assert
        verify(emailSender, never()).createMimeMessage();
        verify(emailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendEventReminder_ToAttendee_ShouldSendEmail() throws MessagingException {
        // Arrange
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(emailSender).send(any(MimeMessage.class));
        
        // Act
        emailService.sendEventReminder(testUser, testEvent, false);
        
        // Assert
        verify(emailSender).createMimeMessage();
        verify(emailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEventReminder_ToOrganizer_ShouldSendEmail() throws MessagingException {
        // Arrange
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(emailSender).send(any(MimeMessage.class));
        
        // Act
        emailService.sendEventReminder(testUser, testEvent, true);
        
        // Assert
        verify(emailSender).createMimeMessage();
        verify(emailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEventReminder_WithNullUser_ShouldNotSendEmail() {
        // Act
        emailService.sendEventReminder(null, testEvent, false);
        
        // Assert
        verify(emailSender, never()).createMimeMessage();
        verify(emailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendEventReminder_WithNullEvent_ShouldNotSendEmail() {
        // Act
        emailService.sendEventReminder(testUser, null, false);
        
        // Assert
        verify(emailSender, never()).createMimeMessage();
        verify(emailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendEventReminder_WithUserWithoutEmail_ShouldNotSendEmail() {
        // Arrange
        UserEntity userWithoutEmail = new UserEntity();
        userWithoutEmail.setId(3L);
        userWithoutEmail.setUsername("noEmail");
        
        // Act
        emailService.sendEventReminder(userWithoutEmail, testEvent, false);
        
        // Assert
        verify(emailSender, never()).createMimeMessage();
        verify(emailSender, never()).send(any(MimeMessage.class));
    }
} 