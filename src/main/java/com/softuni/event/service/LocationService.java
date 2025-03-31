package com.softuni.event.service;

import com.softuni.event.model.dto.LocationBasicDTO;

import java.util.List;

public interface LocationService {
    List<LocationBasicDTO> getAllLocations();
    LocationBasicDTO getLocationById(Long id);
    LocationBasicDTO createLocation(LocationBasicDTO locationDTO);
    void updateLocation(Long id, LocationBasicDTO locationDTO);
    void deleteLocation(Long id);
    List<LocationBasicDTO> searchLocations(String keyword);
    List<LocationBasicDTO> getLocationsByCity(String city);
    List<LocationBasicDTO> getLocationsByCapacity(Integer capacity);
} 