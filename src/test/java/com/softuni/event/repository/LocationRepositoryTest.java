package com.softuni.event.repository;

import com.softuni.event.model.entity.LocationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class LocationRepositoryTest {

    @Autowired
    private LocationRepository locationRepository;
    
    private LocationEntity location1; // Large venue in Sofia
    private LocationEntity location2; // Medium venue in Sofia
    private LocationEntity location3; // Small venue in Varna
    
    @BeforeEach
    void setUp() {
        // Create test locations
        location1 = new LocationEntity();
        location1.setName("National Palace of Culture");
        location1.setAddress("1 Bulgaria Square");
        location1.setCity("Sofia");
        location1.setCountry("Bulgaria");
        location1.setCapacity(5000);
        location1.setDescription("Largest conference center in Sofia");
        
        location2 = new LocationEntity();
        location2.setName("Sofia Tech Park");
        location2.setAddress("111 Tsarigradsko Shose Blvd");
        location2.setCity("Sofia");
        location2.setCountry("Bulgaria");
        location2.setCapacity(500);
        location2.setDescription("Modern tech venue");
        
        location3 = new LocationEntity();
        location3.setName("Festival Hall");
        location3.setAddress("2 Slivnitsa Blvd");
        location3.setCity("Varna");
        location3.setCountry("Bulgaria");
        location3.setCapacity(200);
        location3.setDescription("Beach city venue");
        
        // Save locations
        location1 = locationRepository.save(location1);
        location2 = locationRepository.save(location2);
        location3 = locationRepository.save(location3);
    }
    
    @Test
    void testFindByCity() {
        // Act
        List<LocationEntity> sofiaLocations = locationRepository.findByCity("Sofia");
        List<LocationEntity> varnaLocations = locationRepository.findByCity("Varna");
        List<LocationEntity> plovdivLocations = locationRepository.findByCity("Plovdiv");
        
        // Assert
        assertEquals(2, sofiaLocations.size());
        assertTrue(sofiaLocations.contains(location1));
        assertTrue(sofiaLocations.contains(location2));
        
        assertEquals(1, varnaLocations.size());
        assertTrue(varnaLocations.contains(location3));
        
        assertEquals(0, plovdivLocations.size());
    }
    
    @Test
    void testFindByCountry() {
        // Act
        List<LocationEntity> bulgariaLocations = locationRepository.findByCountry("Bulgaria");
        List<LocationEntity> greeceLocations = locationRepository.findByCountry("Greece");
        
        // Assert
        assertEquals(3, bulgariaLocations.size());
        assertTrue(bulgariaLocations.contains(location1));
        assertTrue(bulgariaLocations.contains(location2));
        assertTrue(bulgariaLocations.contains(location3));
        
        assertEquals(0, greeceLocations.size());
    }
    
    @Test
    void testFindByCapacityGreaterThanEqual() {
        // Act
        List<LocationEntity> largeLocations = locationRepository.findByCapacityGreaterThanEqual(1000);
        List<LocationEntity> mediumLocations = locationRepository.findByCapacityGreaterThanEqual(300);
        List<LocationEntity> smallLocations = locationRepository.findByCapacityGreaterThanEqual(100);
        
        // Assert
        assertEquals(1, largeLocations.size());
        assertTrue(largeLocations.contains(location1));
        
        assertEquals(2, mediumLocations.size());
        assertTrue(mediumLocations.contains(location1));
        assertTrue(mediumLocations.contains(location2));
        
        assertEquals(3, smallLocations.size());
        assertTrue(smallLocations.contains(location1));
        assertTrue(smallLocations.contains(location2));
        assertTrue(smallLocations.contains(location3));
    }
    
    @Test
    void testFindByNameContaining() {
        // Act
        List<LocationEntity> palaceLocations = locationRepository.findByNameContaining("Palace");
        List<LocationEntity> hallLocations = locationRepository.findByNameContaining("Hall");
        List<LocationEntity> parkLocations = locationRepository.findByNameContaining("Park");
        List<LocationEntity> nonExistentLocations = locationRepository.findByNameContaining("Stadium");
        
        // Assert
        assertEquals(1, palaceLocations.size());
        assertTrue(palaceLocations.contains(location1));
        
        assertEquals(1, hallLocations.size());
        assertTrue(hallLocations.contains(location3));
        
        assertEquals(1, parkLocations.size());
        assertTrue(parkLocations.contains(location2));
        
        assertEquals(0, nonExistentLocations.size());
    }
    
    @Test
    void testBasicJpaRepositoryMethods() {
        // Test findById
        Optional<LocationEntity> foundLocation = locationRepository.findById(location1.getId());
        assertTrue(foundLocation.isPresent());
        assertEquals("National Palace of Culture", foundLocation.get().getName());
        
        // Test findAll
        List<LocationEntity> allLocations = locationRepository.findAll();
        assertEquals(3, allLocations.size());
        
        // Test update
        LocationEntity locationToUpdate = foundLocation.get();
        locationToUpdate.setName("Updated Palace of Culture");
        locationRepository.save(locationToUpdate);
        
        Optional<LocationEntity> updatedLocation = locationRepository.findById(location1.getId());
        assertTrue(updatedLocation.isPresent());
        assertEquals("Updated Palace of Culture", updatedLocation.get().getName());
        
        // Test delete
        locationRepository.delete(locationToUpdate);
        assertEquals(2, locationRepository.count());
        assertFalse(locationRepository.findById(location1.getId()).isPresent());
    }
} 