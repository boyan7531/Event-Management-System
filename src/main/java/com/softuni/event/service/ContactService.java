package com.softuni.event.service;

import com.softuni.event.model.dto.ContactMessageDTO;

import java.util.List;

public interface ContactService {
    // Save a new contact message
    void saveContactMessage(String name, String email, String subject, String message);
    
    // Get all contact messages
    List<ContactMessageDTO> getAllMessages();
    
    // Get unread contact messages
    List<ContactMessageDTO> getUnreadMessages();
    
    // Get a single message by ID
    ContactMessageDTO getMessageById(Long id);
    
    // Mark a message as read
    void markAsRead(Long id);
    
    // Delete a message
    void deleteMessage(Long id);
    
    // Count unread messages
    long countUnreadMessages();
} 