package com.softuni.event.controller;

import com.softuni.event.model.dto.LocationBasicDTO;
import com.softuni.event.service.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal; // Ensure this import if needed for other tests, though not directly here
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(LocationController.class)
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocationService locationService;

    private LocationBasicDTO testLocationDTO;

    @BeforeEach
    void setUp() {
        testLocationDTO = new LocationBasicDTO();
        testLocationDTO.setId(1L);
        testLocationDTO.setName("Test Location");
        testLocationDTO.setAddress("123 Test St");
        testLocationDTO.setCity("Test City");
        testLocationDTO.setCountry("Test Country");
        testLocationDTO.setCapacity(100);
    }

    @Test
    @WithMockUser // Accessing /locations requires authentication as per SecurityConfig
    void testGetAllLocations() throws Exception {
        when(locationService.getAllLocations()).thenReturn(List.of(testLocationDTO));

        mockMvc.perform(get("/locations"))
                .andExpect(status().isOk())
                .andExpect(view().name("locations"))
                .andExpect(model().attributeExists("locations"))
                .andExpect(model().attribute("locations", hasSize(1)))
                .andExpect(model().attribute("locations", contains(
                        hasProperty("name", is("Test Location"))
                )));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Requires ADMIN role as per SecurityConfig
    void testShowCreateForm() throws Exception {
        mockMvc.perform(get("/locations/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("location-create"))
                .andExpect(model().attributeExists("location"))
                .andExpect(model().attribute("location", instanceOf(LocationBasicDTO.class)));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Requires ADMIN role
    void testCreateLocation_Success() throws Exception {
        when(locationService.createLocation(any(LocationBasicDTO.class))).thenReturn(testLocationDTO);

        mockMvc.perform(post("/locations/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "New Location")
                        .param("address", "456 New Ave")
                        .param("city", "New City")
                        .param("country", "New Country")
                        .param("capacity", "50")
                        .with(csrf())) // Add CSRF token for POST requests
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/locations"));

        verify(locationService, times(1)).createLocation(any(LocationBasicDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Requires ADMIN role
    void testCreateLocation_ValidationError() throws Exception {
        mockMvc.perform(post("/locations/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "") // Invalid: name is required
                        .param("address", "456 New Ave")
                        .param("city", "New City")
                        .param("country", "New Country")
                        .param("capacity", "50")
                        .with(csrf())) // Add CSRF token
                .andExpect(status().isOk()) // Expect OK status as the form is re-displayed
                .andExpect(view().name("location-create"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("location", "name"));

        verify(locationService, never()).createLocation(any(LocationBasicDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Requires ADMIN role
    void testShowEditForm() throws Exception {
        when(locationService.getLocationById(1L)).thenReturn(testLocationDTO);

        mockMvc.perform(get("/locations/edit/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("location-edit"))
                .andExpect(model().attributeExists("location"))
                .andExpect(model().attribute("location", hasProperty("id", is(1L))))
                .andExpect(model().attribute("location", hasProperty("name", is("Test Location"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Requires ADMIN role
    void testUpdateLocation_Success() throws Exception {
        doNothing().when(locationService).updateLocation(eq(1L), any(LocationBasicDTO.class));

        mockMvc.perform(post("/locations/edit/{id}", 1L)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Updated Location")
                        .param("address", "123 Updated St")
                        .param("city", "Updated City")
                        .param("country", "Updated Country")
                        .param("capacity", "110")
                        .with(csrf())) // Add CSRF token
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/locations"));

        verify(locationService, times(1)).updateLocation(eq(1L), any(LocationBasicDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Requires ADMIN role
    void testUpdateLocation_ValidationError() throws Exception {
        // No need to mock service as it shouldn't be called on validation error

        mockMvc.perform(post("/locations/edit/{id}", 1L)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Updated Location")
                        .param("address", "") // Invalid: address is required
                        .param("city", "Updated City")
                        .param("country", "Updated Country")
                        .param("capacity", "110")
                        .with(csrf())) // Add CSRF token
                .andExpect(status().isOk()) // Expect OK status as the form is re-displayed
                .andExpect(view().name("location-edit"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("location", "address"));

        verify(locationService, never()).updateLocation(anyLong(), any(LocationBasicDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Requires ADMIN role
    void testDeleteLocation() throws Exception {
        doNothing().when(locationService).deleteLocation(1L);

        mockMvc.perform(post("/locations/delete/{id}", 1L)
                        .with(csrf())) // Add CSRF token
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/locations"));

        verify(locationService, times(1)).deleteLocation(1L);
    }
}