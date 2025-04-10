package com.softuni.event.controller;

import com.softuni.event.model.entity.EventEntity;
import com.softuni.event.model.enums.EventStatus;
import com.softuni.event.repository.EventRepository;
import com.softuni.event.service.EventReminderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN") // Apply admin role for all tests in this class
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private EventReminderService eventReminderService;

    @Test
    void testAdminDashboardRedirect() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/admin/users"));
    }

    @Test
    void testReminderDashboard() throws Exception {
        List<EventEntity> mockEvents = Collections.emptyList();
        when(eventRepository.findByEventDateBetweenAndStatus(any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.APPROVED)))
                .thenReturn(mockEvents);

        mockMvc.perform(get("/admin/reminders"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-reminders"))
                .andExpect(model().attributeExists("upcomingEvents"))
                .andExpect(model().attribute("upcomingEvents", hasSize(0)));

        verify(eventRepository, times(1)).findByEventDateBetweenAndStatus(any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.APPROVED));
    }

    @Test
    void testSendReminderForEvent_Success() throws Exception {
        Long eventId = 1L;
        when(eventReminderService.sendReminderForEvent(eventId)).thenReturn(true);

        mockMvc.perform(post("/admin/reminders/event/{id}", eventId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reminders"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(eventReminderService, times(1)).sendReminderForEvent(eventId);
    }

    @Test
    void testSendReminderForEvent_NotFoundOrNotEligible() throws Exception {
        Long eventId = 2L;
        when(eventReminderService.sendReminderForEvent(eventId)).thenReturn(false);

        mockMvc.perform(post("/admin/reminders/event/{id}", eventId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reminders"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(eventReminderService, times(1)).sendReminderForEvent(eventId);
    }
    
    @Test
    void testSendReminderForEvent_NullId() throws Exception {
        // Path variable cannot technically be null here, but testing handling if path pattern allowed it
        // Spring MVC usually handles this with a 404 before controller logic if ID is required path part
        // This test case might be less relevant depending on strict path matching
         mockMvc.perform(post("/admin/reminders/event/") // Hypothetical endpoint without ID
                         .with(csrf()))
                 .andExpect(status().isNotFound()); // Expecting 404 if ID is mandatory path part

        // If the endpoint pattern allows null/optional ID (e.g., @PathVariable(required=false))
        // and the controller has specific null check logic:
        /*
        mockMvc.perform(post("/admin/reminders/event/{id}", (Object) null) // PathVariable might not allow null directly
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reminders"))
                .andExpect(flash().attributeExists("errorMessage"));
        verify(eventReminderService, never()).sendReminderForEvent(any());
        */
    }


    @Test
    void testSendReminderForEvent_ServiceException() throws Exception {
        Long eventId = 3L;
        String exceptionMessage = "Database connection failed";
        when(eventReminderService.sendReminderForEvent(eventId)).thenThrow(new RuntimeException(exceptionMessage));

        mockMvc.perform(post("/admin/reminders/event/{id}", eventId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reminders"))
                .andExpect(flash().attribute("errorMessage", "Error sending reminder: " + exceptionMessage));

        verify(eventReminderService, times(1)).sendReminderForEvent(eventId);
    }

    @Test
    void testRunReminderJob_Success() throws Exception {
        int remindersSent = 5;
        when(eventReminderService.processReminderSchedule()).thenReturn(remindersSent);

        mockMvc.perform(post("/admin/reminders/run-job")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reminders"))
                .andExpect(flash().attribute("successMessage", "Reminder job executed successfully. Sent " + remindersSent + " reminders."));

        verify(eventReminderService, times(1)).processReminderSchedule();
    }

    @Test
    void testRunReminderJob_ServiceException() throws Exception {
        String exceptionMessage = "Scheduler lock error";
        when(eventReminderService.processReminderSchedule()).thenThrow(new RuntimeException(exceptionMessage));

        mockMvc.perform(post("/admin/reminders/run-job")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reminders"))
                .andExpect(flash().attribute("errorMessage", "Error running reminder job: " + exceptionMessage));

        verify(eventReminderService, times(1)).processReminderSchedule();
    }
} 