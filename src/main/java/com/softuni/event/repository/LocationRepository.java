package com.softuni.event.repository;

import com.softuni.event.model.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
    List<LocationEntity> findByCity(String city);
    List<LocationEntity> findByCountry(String country);
    List<LocationEntity> findByCapacityGreaterThanEqual(Integer capacity);
    List<LocationEntity> findByNameContaining(String keyword);
} 