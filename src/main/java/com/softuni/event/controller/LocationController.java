package com.softuni.event.controller;

import com.softuni.event.model.dto.LocationBasicDTO;
import com.softuni.event.service.LocationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public String getAllLocations(Model model) {
        model.addAttribute("locations", locationService.getAllLocations());
        return "locations";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("location", new LocationBasicDTO());
        return "location-create";
    }

    @PostMapping("/create")
    public String createLocation(@Valid @ModelAttribute("location") LocationBasicDTO locationDTO,
                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "location-create";
        }
        
        locationService.createLocation(locationDTO);
        return "redirect:/locations";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        LocationBasicDTO location = locationService.getLocationById(id);
        model.addAttribute("location", location);
        return "location-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateLocation(@PathVariable Long id, 
                                @Valid @ModelAttribute("location") LocationBasicDTO locationDTO,
                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "location-edit";
        }
        
        locationService.updateLocation(id, locationDTO);
        return "redirect:/locations";
    }

    @PostMapping("/delete/{id}")
    public String deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return "redirect:/locations";
    }
} 