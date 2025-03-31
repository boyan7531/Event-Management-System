package com.softuni.event.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationBasicDTO {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String country;
    private Integer capacity;
} 