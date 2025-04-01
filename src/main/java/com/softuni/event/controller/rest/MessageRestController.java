package com.softuni.event.controller.rest;

import com.softuni.event.service.ContactService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageRestController {

    private final ContactService contactService;

    public MessageRestController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        long count = contactService.countUnreadMessages();
        return ResponseEntity.ok(Map.of("count", count));
    }
} 