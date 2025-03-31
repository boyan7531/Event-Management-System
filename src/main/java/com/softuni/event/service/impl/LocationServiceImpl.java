package com.softuni.event.service.impl;

import com.softuni.event.exception.ResourceNotFoundException;
import com.softuni.event.model.dto.LocationBasicDTO;
import com.softuni.event.model.entity.LocationEntity;
import com.softuni.event.repository.LocationRepository;
import com.softuni.event.service.LocationService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final ModelMapper modelMapper;

    public LocationServiceImpl(LocationRepository locationRepository, ModelMapper modelMapper) {
        this.locationRepository = locationRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<LocationBasicDTO> getAllLocations() {
        return locationRepository.findAll().stream()
                .map(location -> modelMapper.map(location, LocationBasicDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public LocationBasicDTO getLocationById(Long id) {
        LocationEntity location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", id));
        
        return modelMapper.map(location, LocationBasicDTO.class);
    }

    @Override
    @Transactional
    public LocationBasicDTO createLocation(LocationBasicDTO locationDTO) {
        LocationEntity location = modelMapper.map(locationDTO, LocationEntity.class);
        location.setCreatedAt(LocalDateTime.now());
        
        LocationEntity savedLocation = locationRepository.save(location);
        
        return modelMapper.map(savedLocation, LocationBasicDTO.class);
    }

    @Override
    @Transactional
    public void updateLocation(Long id, LocationBasicDTO locationDTO) {
        LocationEntity location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", id));
        
        location.setName(locationDTO.getName());
        location.setAddress(locationDTO.getAddress());
        location.setCity(locationDTO.getCity());
        location.setCountry(locationDTO.getCountry());
        location.setCapacity(locationDTO.getCapacity());
        location.setUpdatedAt(LocalDateTime.now());
        
        locationRepository.save(location);
    }

    @Override
    @Transactional
    public void deleteLocation(Long id) {
        LocationEntity location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", id));
        
        locationRepository.delete(location);
    }

    @Override
    public List<LocationBasicDTO> searchLocations(String keyword) {
        return locationRepository.findByNameContaining(keyword).stream()
                .map(location -> modelMapper.map(location, LocationBasicDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<LocationBasicDTO> getLocationsByCity(String city) {
        return locationRepository.findByCity(city).stream()
                .map(location -> modelMapper.map(location, LocationBasicDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<LocationBasicDTO> getLocationsByCapacity(Integer capacity) {
        return locationRepository.findByCapacityGreaterThanEqual(capacity).stream()
                .map(location -> modelMapper.map(location, LocationBasicDTO.class))
                .collect(Collectors.toList());
    }
} 