package com.softuni.event.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorViewController {

    @GetMapping("/error-page")
    public String error() {
        return "error/error";
    }
} 