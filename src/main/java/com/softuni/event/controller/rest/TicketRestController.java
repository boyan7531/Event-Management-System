package com.softuni.event.controller.rest;

import com.softuni.event.model.dto.TicketDTO;
import com.softuni.event.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
public class TicketRestController {

    private final TicketService ticketService;

    public TicketRestController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<TicketDTO>> getTicketsByEvent(@PathVariable Long eventId) {
        List<TicketDTO> tickets = ticketService.getTicketsByEvent(eventId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{ticketNumber}")
    public ResponseEntity<TicketDTO> getTicketByNumber(@PathVariable String ticketNumber) {
        TicketDTO ticket = ticketService.getTicketByNumber(ticketNumber);
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/validate/{ticketNumber}")
    public ResponseEntity<Map<String, Object>> validateTicket(@PathVariable String ticketNumber) {
        boolean isValid = ticketService.useTicket(ticketNumber);
        
        Map<String, Object> response = Map.of(
                "ticketNumber", ticketNumber,
                "valid", isValid,
                "message", isValid ? "Ticket validated successfully" : "Invalid ticket or already used"
        );
        
        return ResponseEntity.ok(response);
    }
} 