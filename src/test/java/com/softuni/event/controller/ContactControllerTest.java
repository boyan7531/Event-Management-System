package com.softuni.event.controller;

import com.softuni.event.service.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactController.class)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @Test
    @WithMockUser // Even public pages often require some user context if security is enabled
    void testShowContactForm() throws Exception {
        mockMvc.perform(get("/contact"))
                .andExpect(status().isOk())
                .andExpect(view().name("contact"));
    }

    @Test
    @WithMockUser
    void testHandleContactForm() throws Exception {
        String testName = "Test User";
        String testEmail = "test@example.com";
        String testSubject = "Test Subject";
        String testMessage = "This is a test message.";

        // Mock the service call - it returns void
        doNothing().when(contactService).saveContactMessage(testName, testEmail, testSubject, testMessage);

        mockMvc.perform(post("/contact")
                        .param("name", testName)
                        .param("email", testEmail)
                        .param("subject", testSubject)
                        .param("message", testMessage)
                        .with(csrf())) // Include CSRF token
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contact"))
                .andExpect(flash().attributeExists("successMessage"));

        // Verify that the service method was called with the correct arguments
        verify(contactService).saveContactMessage(testName, testEmail, testSubject, testMessage);
    }
} 