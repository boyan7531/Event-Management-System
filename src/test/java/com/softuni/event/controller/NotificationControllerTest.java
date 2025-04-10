package com.softuni.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softuni.event.model.entity.NotificationEntity;
import com.softuni.event.model.entity.UserEntity;
import com.softuni.event.model.enums.NotificationType;
import com.softuni.event.service.NotificationService;
import com.softuni.event.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // For JSON assertions

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private UserService userService;

    private UserEntity testUser;
    private NotificationEntity testNotification;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testNotification = new NotificationEntity();
        testNotification.setId(1L);
        testNotification.setUser(testUser);
        testNotification.setMessage("Test Notification Message");
        testNotification.setRead(false);
        testNotification.setCreatedAt(LocalDateTime.now());
        testNotification.setType(NotificationType.SYSTEM);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testViewAllNotifications() throws Exception {
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        testNotification.setType(NotificationType.EVENT_APPROVED);
        when(notificationService.getNotificationsForUser(testUser)).thenReturn(List.of(testNotification));
        doNothing().when(notificationService).markAllAsRead(testUser);

        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(view().name("notifications"))
                .andExpect(model().attributeExists("notifications"))
                .andExpect(model().attribute("notifications", hasSize(1)));

        verify(notificationService, times(1)).markAllAsRead(testUser);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetUnreadCount() throws Exception {
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(notificationService.getUnreadNotificationCount(testUser)).thenReturn(5L);

        mockMvc.perform(get("/notifications/unread-count")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count", is(5)));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetUnreadNotifications() throws Exception {
        testNotification.setRead(false);
        testNotification.setType(NotificationType.EVENT_REMINDER);
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(notificationService.getUnreadNotificationsForUser(testUser)).thenReturn(List.of(testNotification));

        mockMvc.perform(get("/notifications/unread")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].message", is("Test Notification Message")))
                .andExpect(jsonPath("$[0].read", is(false)));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetUnreadNotifications_Empty() throws Exception {
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(notificationService.getUnreadNotificationsForUser(testUser)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/notifications/unread")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser // No specific user needed as ID is path variable
    void testMarkAsRead() throws Exception {
        doNothing().when(notificationService).markAsRead(1L);

        mockMvc.perform(post("/notifications/mark-read/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).markAsRead(1L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testMarkAllAsRead() throws Exception {
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        doNothing().when(notificationService).markAllAsRead(testUser);

        mockMvc.perform(post("/notifications/mark-all-read")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).markAllAsRead(testUser);
    }

    @Test
    @WithMockUser // No specific user needed as ID is path variable
    void testDeleteNotification() throws Exception {
        doNothing().when(notificationService).deleteNotification(1L);

        mockMvc.perform(post("/notifications/delete/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).deleteNotification(1L);
    }
}