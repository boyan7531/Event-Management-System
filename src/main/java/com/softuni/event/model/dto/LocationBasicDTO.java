package com.softuni.event.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "Location name cannot be blank")
    private String name;

    @NotBlank(message = "Address cannot be blank")
    private String address;

    @NotBlank(message = "City cannot be blank")
    private String city;

    @NotBlank(message = "Country cannot be blank")
    private String country;

    @NotNull(message = "Capacity cannot be null")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
} 