package com.softuni.event.controller;

import com.softuni.event.service.ContactService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/contact")
public class ContactController {
    
    private final ContactService contactService;
    
    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public String showContactForm() {
        return "contact";
    }
    
    @PostMapping
    public String handleContactForm(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String message,
            RedirectAttributes redirectAttributes) {
        
        // Save the contact message to the database
        contactService.saveContactMessage(name, email, subject, message);
        
        // Add a success message
        redirectAttributes.addFlashAttribute("successMessage", 
            "Thank you for your message! We will get back to you as soon as possible.");
            
        return "redirect:/contact";
    }
} 