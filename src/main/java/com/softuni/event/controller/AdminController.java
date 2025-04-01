package com.softuni.event.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    public String adminDashboard() {
        // Redirect to the users admin page
        return "redirect:/users/admin/users";
    }
} 