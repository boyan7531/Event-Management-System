package com.softuni.event.service.impl;

import com.softuni.event.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class WeatherServiceImpl implements WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final RestTemplate restTemplate;
    
    @Value("${weather.api.url:http://localhost:8081/api/weather}")
    private String weatherApiUrl;

    public WeatherServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getWeatherForecast(String city, LocalDateTime date) {
        try {
            String dateStr = date.format(DATE_FORMATTER);
            String url = String.format("%s?city=%s&date=%s", weatherApiUrl, city, dateStr);
            
            return restTemplate.getForObject(url, String.class);
        } catch (RestClientException e) {
            logger.error("Error fetching weather forecast for city: {}, date: {}", city, date, e);
            return "Weather forecast unavailable";
        }
    }

    @Override
    public String getWeatherForecast(double latitude, double longitude, LocalDateTime date) {
        try {
            String dateStr = date.format(DATE_FORMATTER);
            String url = String.format("%s?lat=%s&lon=%s&date=%s", weatherApiUrl, latitude, longitude, dateStr);
            
            return restTemplate.getForObject(url, String.class);
        } catch (RestClientException e) {
            logger.error("Error fetching weather forecast for lat: {}, lon: {}, date: {}", 
                        latitude, longitude, date, e);
            return "Weather forecast unavailable";
        }
    }
} 