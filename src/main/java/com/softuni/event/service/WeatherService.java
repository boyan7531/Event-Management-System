package com.softuni.event.service;

import java.time.LocalDateTime;

public interface WeatherService {
    String getWeatherForecast(String city, LocalDateTime date);
    String getWeatherForecast(double latitude, double longitude, LocalDateTime date);
} 