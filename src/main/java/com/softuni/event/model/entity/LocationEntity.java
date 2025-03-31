package com.softuni.event.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor
public class LocationEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    private String city;
    
    private String country;
    
    private String zipCode;
    
    private Double latitude;
    
    private Double longitude;
    
    private Integer capacity;
    
    private String description;

    @OneToMany(mappedBy = "location")
    private Set<EventEntity> events = new HashSet<>();
} 