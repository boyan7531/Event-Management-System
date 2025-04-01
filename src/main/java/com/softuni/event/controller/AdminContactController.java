package com.softuni.event.controller;

import com.softuni.event.model.dto.ContactMessageDTO;
import com.softuni.event.service.ContactService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/messages")
@PreAuthorize("hasRole('ADMIN')")
public class AdminContactController {

    private final ContactService contactService;

    public AdminContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public String showMessages(Model model) {
        model.addAttribute("messages", contactService.getAllMessages());
        model.addAttribute("unreadCount", contactService.countUnreadMessages());
        model.addAttribute("unreadOnly", false);
        return "admin-messages";
    }

    @GetMapping("/unread")
    public String showUnreadMessages(Model model) {
        model.addAttribute("messages", contactService.getUnreadMessages());
        model.addAttribute("unreadCount", contactService.countUnreadMessages());
        model.addAttribute("unreadOnly", true);
        return "admin-messages";
    }

    @GetMapping("/view/{id}")
    public String viewMessage(@PathVariable Long id, Model model) {
        ContactMessageDTO message = contactService.getMessageById(id);
        
        // Mark as read when viewed
        if (!message.isRead()) {
            contactService.markAsRead(id);
        }
        
        model.addAttribute("message", message);
        return "admin-message-detail";
    }

    @PostMapping("/mark-read/{id}")
    public String markAsRead(@PathVariable Long id, @RequestParam(required = false) String returnUrl) {
        contactService.markAsRead(id);
        
        if (returnUrl != null && !returnUrl.isEmpty()) {
            return "redirect:" + returnUrl;
        }
        return "redirect:/admin/messages";
    }

    @PostMapping("/delete/{id}")
    public String deleteMessage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        contactService.deleteMessage(id);
        redirectAttributes.addFlashAttribute("successMessage", "Message deleted successfully");
        return "redirect:/admin/messages";
    }
} 