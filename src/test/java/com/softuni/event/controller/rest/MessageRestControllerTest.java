package com.softuni.event.controller.rest;

import com.softuni.event.service.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageRestController.class)
@EnableMethodSecurity
class MessageRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUnreadCount_AsAdmin_ReturnsCount() throws Exception {
        // Arrange
        when(contactService.countUnreadMessages()).thenReturn(5L);

        // Act & Assert
        mockMvc.perform(get("/api/messages/unread-count")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUnreadCount_AsUser_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/messages/unread-count")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUnreadCount_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/messages/unread-count")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUnreadCount_ZeroMessages_ReturnsZero() throws Exception {
        // Arrange
        when(contactService.countUnreadMessages()).thenReturn(0L);

        // Act & Assert
        mockMvc.perform(get("/api/messages/unread-count")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }
} 