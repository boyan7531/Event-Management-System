package com.softuni.event.service.impl;

import com.softuni.event.exception.ResourceNotFoundException;
import com.softuni.event.model.dto.LocationBasicDTO;
import com.softuni.event.model.entity.LocationEntity;
import com.softuni.event.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private LocationServiceImpl locationService;

    @Captor
    private ArgumentCaptor<LocationEntity> locationCaptor;

    private LocationEntity testLocation1;
    private LocationEntity testLocation2;
    private LocationBasicDTO testLocationDTO1;
    private LocationBasicDTO testLocationDTO2;

    @BeforeEach
    void setUp() {
        // Set up test data
        testLocation1 = new LocationEntity();
        testLocation1.setId(1L);
        testLocation1.setName("Test Venue 1");
        testLocation1.setAddress("123 Test St");
        testLocation1.setCity("Test City");
        testLocation1.setCountry("Test Country");
        testLocation1.setCapacity(100);
        testLocation1.setCreatedAt(LocalDateTime.now().minusDays(5));

        testLocation2 = new LocationEntity();
        testLocation2.setId(2L);
        testLocation2.setName("Test Venue 2");
        testLocation2.setAddress("456 Sample Ave");
        testLocation2.setCity("Sample City");
        testLocation2.setCountry("Sample Country");
        testLocation2.setCapacity(200);
        testLocation2.setCreatedAt(LocalDateTime.now().minusDays(2));

        testLocationDTO1 = new LocationBasicDTO();
        testLocationDTO1.setId(1L);
        testLocationDTO1.setName("Test Venue 1");
        testLocationDTO1.setAddress("123 Test St");
        testLocationDTO1.setCity("Test City");
        testLocationDTO1.setCountry("Test Country");
        testLocationDTO1.setCapacity(100);

        testLocationDTO2 = new LocationBasicDTO();
        testLocationDTO2.setId(2L);
        testLocationDTO2.setName("Test Venue 2");
        testLocationDTO2.setAddress("456 Sample Ave");
        testLocationDTO2.setCity("Sample City");
        testLocationDTO2.setCountry("Sample Country");
        testLocationDTO2.setCapacity(200);
    }

    @Test
    void getAllLocations_ShouldReturnAllLocations() {
        // Arrange
        when(locationRepository.findAll()).thenReturn(Arrays.asList(testLocation1, testLocation2));
        when(modelMapper.map(testLocation1, LocationBasicDTO.class)).thenReturn(testLocationDTO1);
        when(modelMapper.map(testLocation2, LocationBasicDTO.class)).thenReturn(testLocationDTO2);

        // Act
        List<LocationBasicDTO> result = locationService.getAllLocations();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Test Venue 1", result.get(0).getName());
        assertEquals("Test Venue 2", result.get(1).getName());
        verify(locationRepository).findAll();
        verify(modelMapper, times(2)).map(any(LocationEntity.class), eq(LocationBasicDTO.class));
    }

    @Test
    void getLocationById_ShouldReturnLocation_WhenExists() {
        // Arrange
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation1));
        when(modelMapper.map(testLocation1, LocationBasicDTO.class)).thenReturn(testLocationDTO1);

        // Act
        LocationBasicDTO result = locationService.getLocationById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Venue 1", result.getName());
        verify(locationRepository).findById(1L);
        verify(modelMapper).map(testLocation1, LocationBasicDTO.class);
    }

    @Test
    void getLocationById_ShouldThrowException_WhenNotExists() {
        // Arrange
        when(locationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            locationService.getLocationById(999L);
        });
        
        assertEquals("Location not found with id: '999'", exception.getMessage());
        verify(locationRepository).findById(999L);
        verifyNoInteractions(modelMapper);
    }

    @Test
    void createLocation_ShouldCreateAndReturnLocation() {
        // Arrange
        LocationBasicDTO inputDTO = new LocationBasicDTO();
        inputDTO.setName("New Venue");
        inputDTO.setAddress("789 New St");
        inputDTO.setCity("New City");
        inputDTO.setCountry("New Country");
        inputDTO.setCapacity(300);

        LocationEntity newLocation = new LocationEntity();
        newLocation.setName("New Venue");
        newLocation.setAddress("789 New St");
        newLocation.setCity("New City");
        newLocation.setCountry("New Country");
        newLocation.setCapacity(300);

        LocationEntity savedLocation = new LocationEntity();
        savedLocation.setId(3L);
        savedLocation.setName("New Venue");
        savedLocation.setAddress("789 New St");
        savedLocation.setCity("New City");
        savedLocation.setCountry("New Country");
        savedLocation.setCapacity(300);
        savedLocation.setCreatedAt(LocalDateTime.now());

        LocationBasicDTO savedDTO = new LocationBasicDTO();
        savedDTO.setId(3L);
        savedDTO.setName("New Venue");
        savedDTO.setAddress("789 New St");
        savedDTO.setCity("New City");
        savedDTO.setCountry("New Country");
        savedDTO.setCapacity(300);

        when(modelMapper.map(inputDTO, LocationEntity.class)).thenReturn(newLocation);
        when(locationRepository.save(any(LocationEntity.class))).thenReturn(savedLocation);
        when(modelMapper.map(savedLocation, LocationBasicDTO.class)).thenReturn(savedDTO);

        // Act
        LocationBasicDTO result = locationService.createLocation(inputDTO);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("New Venue", result.getName());
        
        verify(modelMapper).map(inputDTO, LocationEntity.class);
        verify(locationRepository).save(locationCaptor.capture());
        verify(modelMapper).map(savedLocation, LocationBasicDTO.class);
        
        LocationEntity capturedLocation = locationCaptor.getValue();
        assertNotNull(capturedLocation.getCreatedAt());
    }

    @Test
    void updateLocation_ShouldUpdateExistingLocation() {
        // Arrange
        Long locationId = 1L;
        LocationBasicDTO updateDTO = new LocationBasicDTO();
        updateDTO.setName("Updated Venue");
        updateDTO.setAddress("Updated Address");
        updateDTO.setCity("Updated City");
        updateDTO.setCountry("Updated Country");
        updateDTO.setCapacity(150);

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation1));
        when(locationRepository.save(any(LocationEntity.class))).thenReturn(testLocation1);

        // Act
        locationService.updateLocation(locationId, updateDTO);

        // Assert
        verify(locationRepository).findById(locationId);
        verify(locationRepository).save(locationCaptor.capture());
        
        LocationEntity updatedLocation = locationCaptor.getValue();
        assertEquals("Updated Venue", updatedLocation.getName());
        assertEquals("Updated Address", updatedLocation.getAddress());
        assertEquals("Updated City", updatedLocation.getCity());
        assertEquals("Updated Country", updatedLocation.getCountry());
        assertEquals(150, updatedLocation.getCapacity());
        assertNotNull(updatedLocation.getUpdatedAt());
    }

    @Test
    void updateLocation_ShouldThrowException_WhenNotExists() {
        // Arrange
        Long locationId = 999L;
        LocationBasicDTO updateDTO = new LocationBasicDTO();
        updateDTO.setName("Updated Venue");

        when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            locationService.updateLocation(locationId, updateDTO);
        });
        
        assertEquals("Location not found with id: '999'", exception.getMessage());
        verify(locationRepository).findById(locationId);
        verify(locationRepository, never()).save(any());
    }

    @Test
    void deleteLocation_ShouldDeleteExistingLocation() {
        // Arrange
        Long locationId = 1L;
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation1));
        doNothing().when(locationRepository).delete(testLocation1);

        // Act
        locationService.deleteLocation(locationId);

        // Assert
        verify(locationRepository).findById(locationId);
        verify(locationRepository).delete(testLocation1);
    }

    @Test
    void deleteLocation_ShouldThrowException_WhenNotExists() {
        // Arrange
        Long locationId = 999L;
        when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            locationService.deleteLocation(locationId);
        });
        
        assertEquals("Location not found with id: '999'", exception.getMessage());
        verify(locationRepository).findById(locationId);
        verify(locationRepository, never()).delete(any());
    }

    @Test
    void searchLocations_ShouldReturnMatchingLocations() {
        // Arrange
        String keyword = "Venue";
        when(locationRepository.findByNameContaining(keyword)).thenReturn(Arrays.asList(testLocation1, testLocation2));
        when(modelMapper.map(testLocation1, LocationBasicDTO.class)).thenReturn(testLocationDTO1);
        when(modelMapper.map(testLocation2, LocationBasicDTO.class)).thenReturn(testLocationDTO2);

        // Act
        List<LocationBasicDTO> result = locationService.searchLocations(keyword);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Test Venue 1", result.get(0).getName());
        assertEquals("Test Venue 2", result.get(1).getName());
        verify(locationRepository).findByNameContaining(keyword);
        verify(modelMapper, times(2)).map(any(LocationEntity.class), eq(LocationBasicDTO.class));
    }

    @Test
    void getLocationsByCity_ShouldReturnLocationsInCity() {
        // Arrange
        String city = "Test City";
        when(locationRepository.findByCity(city)).thenReturn(List.of(testLocation1));
        when(modelMapper.map(testLocation1, LocationBasicDTO.class)).thenReturn(testLocationDTO1);

        // Act
        List<LocationBasicDTO> result = locationService.getLocationsByCity(city);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test City", result.get(0).getCity());
        verify(locationRepository).findByCity(city);
        verify(modelMapper).map(any(LocationEntity.class), eq(LocationBasicDTO.class));
    }

    @Test
    void getLocationsByCapacity_ShouldReturnLocationsWithMinimumCapacity() {
        // Arrange
        int capacity = 150;
        when(locationRepository.findByCapacityGreaterThanEqual(capacity)).thenReturn(List.of(testLocation2));
        when(modelMapper.map(testLocation2, LocationBasicDTO.class)).thenReturn(testLocationDTO2);

        // Act
        List<LocationBasicDTO> result = locationService.getLocationsByCapacity(capacity);

        // Assert
        assertEquals(1, result.size());
        assertEquals(200, result.get(0).getCapacity());
        verify(locationRepository).findByCapacityGreaterThanEqual(capacity);
        verify(modelMapper).map(any(LocationEntity.class), eq(LocationBasicDTO.class));
    }
} 