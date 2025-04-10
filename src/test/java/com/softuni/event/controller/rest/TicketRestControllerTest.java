package com.softuni.event.controller.rest;

import com.softuni.event.model.dto.EventBasicDTO;
import com.softuni.event.model.dto.TicketDTO;
import com.softuni.event.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketRestController.class)
@EnableMethodSecurity
class TicketRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @Test
    @WithMockUser
    void getTicketsByEvent_ReturnsTickets() throws Exception {
        // Arrange
        EventBasicDTO event = new EventBasicDTO();
        event.setId(1L);
        
        TicketDTO ticket1 = new TicketDTO();
        ticket1.setTicketNumber("ABC123");
        ticket1.setEvent(event);
        
        TicketDTO ticket2 = new TicketDTO();
        ticket2.setTicketNumber("DEF456");
        ticket2.setEvent(event);
        
        when(ticketService.getTicketsByEvent(1L)).thenReturn(List.of(ticket1, ticket2));

        // Act & Assert
        mockMvc.perform(get("/api/tickets/event/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].ticketNumber").value("ABC123"))
                .andExpect(jsonPath("$[1].ticketNumber").value("DEF456"));
    }
    
    @Test
    @WithMockUser
    void getTicketsByEvent_WhenNoTickets_ReturnsEmptyList() throws Exception {
        // Arrange
        when(ticketService.getTicketsByEvent(999L)).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/tickets/event/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser
    void getTicketByNumber_ReturnsTicket() throws Exception {
        // Arrange
        EventBasicDTO event = new EventBasicDTO();
        event.setId(2L);
        
        TicketDTO ticket = new TicketDTO();
        ticket.setTicketNumber("XYZ789");
        ticket.setEvent(event);
        ticket.setUsed(false);
        
        when(ticketService.getTicketByNumber("XYZ789")).thenReturn(ticket);

        // Act & Assert
        mockMvc.perform(get("/api/tickets/XYZ789")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketNumber").value("XYZ789"))
                .andExpect(jsonPath("$.event.id").value(2))
                .andExpect(jsonPath("$.used").value(false));
    }
    
    @Test
    void getTicketByNumber_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/tickets/ABC123")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void validateTicket_ValidTicket_ReturnsSuccess() throws Exception {
        // Arrange
        when(ticketService.useTicket(eq("XYZ789"))).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/tickets/validate/XYZ789")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketNumber").value("XYZ789"))
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.message").value("Ticket validated successfully"));
    }
    
    @Test
    @WithMockUser
    void validateTicket_InvalidTicket_ReturnsInvalid() throws Exception {
        // Arrange
        when(ticketService.useTicket(anyString())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/tickets/validate/INVALID")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketNumber").value("INVALID"))
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("Invalid ticket or already used"));
    }
    
    @Test
    void validateTicket_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/tickets/validate/XYZ789")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
} 