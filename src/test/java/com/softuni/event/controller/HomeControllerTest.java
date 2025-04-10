package com.softuni.event.controller;

import com.softuni.event.model.dto.EventDetailDTO;
import com.softuni.event.model.enums.EventStatus;
import com.softuni.event.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class) // Use Mockito Extension
//@WebMvcTest(HomeController.class) // Keep this to load web context but manage mocks manually
class HomeControllerTest {

    // Autowire MockMvc if using @WebMvcTest or build manually
    // @Autowired
    private MockMvc mockMvc;

    @Mock // Use Mockito's @Mock
    private EventService eventService;

    @InjectMocks // Inject mocks into the controller
    private HomeController homeController;

    @BeforeEach
    void setUp() {
        // Configure a simple ViewResolver for standalone setup
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/"); // Adjust if your templates are elsewhere
        viewResolver.setSuffix(".html");     // Adjust if you use a different suffix

        // Initialize mocks and setup MockMvc manually with the view resolver
        mockMvc = MockMvcBuilders.standaloneSetup(homeController)
                                 .setViewResolvers(viewResolver)
                                 .build();
    }

    @Test
    // @WithMockUser // Manual setup might require different security configuration if needed
    void testHome() throws Exception {
        List<EventDetailDTO> upcomingEvents = Collections.emptyList();
        List<EventDetailDTO> approvedEvents = Collections.emptyList();

        when(eventService.getUpcomingEvents()).thenReturn(upcomingEvents);
        when(eventService.getEventsByStatus(EventStatus.APPROVED)).thenReturn(approvedEvents);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("upcomingEvents", upcomingEvents))
                .andExpect(model().attributeExists("events"));
    }

    @Test
    // @WithMockUser
    void testAllEvents() throws Exception {
        List<EventDetailDTO> allEvents = Collections.emptyList();
        when(eventService.getAllEvents()).thenReturn(allEvents);

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("events"))
                .andExpect(model().attribute("events", allEvents));
    }

    @Test
    // @WithMockUser
    void testAbout() throws Exception {
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"));
    }

    @Test
    // @WithMockUser
    void testFaq() throws Exception {
        mockMvc.perform(get("/faq"))
                .andExpect(status().isOk())
                .andExpect(view().name("faq"));
    }

    @Test
    // @WithMockUser
    void testHelp() throws Exception {
        mockMvc.perform(get("/help"))
                .andExpect(status().isOk())
                .andExpect(view().name("help"));
    }

    @Test
    // @WithMockUser
    void testTerms() throws Exception {
        mockMvc.perform(get("/terms"))
                .andExpect(status().isOk())
                .andExpect(view().name("terms"));
    }
} 