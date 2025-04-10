package com.softuni.event.controller.rest;

import com.softuni.event.service.EventReminderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EventReminderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventReminderService reminderService;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void triggerEventReminder_Success() throws Exception {
        when(reminderService.sendReminderForEvent(anyLong())).thenReturn(true);

        mockMvc.perform(post("/api/reminders/events/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reminders sent successfully for event ID: 1"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void triggerEventReminder_EventNotFound() throws Exception {
        when(reminderService.sendReminderForEvent(anyLong())).thenReturn(false);

        mockMvc.perform(post("/api/reminders/events/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Event not found or not eligible for reminders"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void triggerEventReminder_ServiceException() throws Exception {
        when(reminderService.sendReminderForEvent(anyLong())).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/reminders/events/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error: Service error"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void triggerEventReminder_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/reminders/events/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void runScheduledJob_Success() throws Exception {
        when(reminderService.processReminderSchedule()).thenReturn(5);

        mockMvc.perform(post("/api/reminders/run-scheduled-job")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.remindersSent").value(5))
                .andExpect(jsonPath("$.message").value("Scheduled reminder job executed successfully. Sent 5 reminders."));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void runScheduledJob_ServiceException() throws Exception {
        when(reminderService.processReminderSchedule()).thenThrow(new RuntimeException("Job failed"));

        mockMvc.perform(post("/api/reminders/run-scheduled-job")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Job failed"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void runScheduledJob_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/reminders/run-scheduled-job")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
} 