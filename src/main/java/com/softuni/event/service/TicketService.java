package com.softuni.event.service;

import com.softuni.event.model.dto.TicketDTO;

import java.util.List;

public interface TicketService {
    TicketDTO createTicket(Long eventId, String username);
    TicketDTO getTicketById(Long id);
    TicketDTO getTicketByNumber(String ticketNumber);
    List<TicketDTO> getTicketsByEvent(Long eventId);
    List<TicketDTO> getTicketsByUser(String username);
    boolean useTicket(String ticketNumber);
    void deleteTicket(Long id);
} 