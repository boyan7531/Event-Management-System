package com.softuni.event.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ErrorViewController.class)
class ErrorViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser // Include mock user if security might intercept the path
    void testErrorPageView() throws Exception {
        mockMvc.perform(get("/error-page"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/error"));
    }
} 