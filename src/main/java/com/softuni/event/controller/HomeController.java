package com.softuni.event.controller;

import com.softuni.event.model.enums.EventStatus;
import com.softuni.event.service.EventService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final EventService eventService;

    public HomeController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("upcomingEvents", eventService.getUpcomingEvents());
        model.addAttribute("events", eventService.getEventsByStatus(EventStatus.APPROVED).stream().limit(3).toList());
        return "index";
    }

    @GetMapping("/events")
    public String allEvents(Model model) {
        model.addAttribute("events", eventService.getAllEvents());
        return "events";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/faq")
    public String faq() {
        return "faq";
    }

    @GetMapping("/help")
    public String help() {
        return "help";
    }

    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }
} 