package com.softuni.event.service.impl;

import com.softuni.event.model.entity.EventEntity;
import com.softuni.event.model.entity.LocationEntity;
import com.softuni.event.model.entity.UserEntity;
import com.softuni.event.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateClientServiceImplTest {

    @Mock
    private EventService eventService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CertificateClientServiceImpl certificateClientService;

    private EventEntity testEvent;
    private final String certificateServiceUrl = "https://certificate-service.example.com";
    private final String username = "testuser";
    private final String password = "testpass";

    @BeforeEach
    void setUp() {
        // Set up test data
        UserEntity organizer = new UserEntity();
        organizer.setId(1L);
        organizer.setFirstName("John");
        organizer.setLastName("Doe");

        LocationEntity location = new LocationEntity();
        location.setId(1L);
        location.setName("Test Location");

        testEvent = new EventEntity();
        testEvent.setId(1L);
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setEventDate(LocalDateTime.now().plusDays(10));
        testEvent.setOrganizer(organizer);
        testEvent.setLocation(location);

        // Replace the RestTemplate with our mock
        ReflectionTestUtils.setField(certificateClientService, "restTemplate", restTemplate);
        
        // Set configuration properties
        ReflectionTestUtils.setField(certificateClientService, "certificateServiceUrl", certificateServiceUrl);
        ReflectionTestUtils.setField(certificateClientService, "username", username);
        ReflectionTestUtils.setField(certificateClientService, "password", password);
    }

    @Test
    void generateEventCertificate_Success() {
        // Arrange
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", "cert-123");
        responseBody.put("eventId", 1L);
        responseBody.put("status", "generated");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        
        when(restTemplate.postForEntity(
                anyString(),
                any(),
                eq(Map.class)
        )).thenReturn(responseEntity);

        // Act
        String certificateId = certificateClientService.generateEventCertificate(testEvent);

        // Assert
        assertEquals("cert-123", certificateId);
        verify(restTemplate).postForEntity(
                eq(certificateServiceUrl + "/api/certificates/generate"),
                any(),
                eq(Map.class)
        );
    }

    @Test
    void generateEventCertificate_RestClientException() {
        // Arrange
        when(restTemplate.postForEntity(
                anyString(),
                any(),
                eq(Map.class)
        )).thenThrow(new RestClientException("Connection error"));

        // Act
        String certificateId = certificateClientService.generateEventCertificate(testEvent);

        // Assert
        assertNull(certificateId);
        verify(restTemplate).postForEntity(
                eq(certificateServiceUrl + "/api/certificates/generate"),
                any(),
                eq(Map.class)
        );
    }

    @Test
    void generateEventCertificate_HttpClientErrorException() {
        // Arrange
        when(restTemplate.postForEntity(
                anyString(),
                any(),
                eq(Map.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad request"));

        // Act
        String certificateId = certificateClientService.generateEventCertificate(testEvent);

        // Assert
        assertNull(certificateId);
        verify(restTemplate).postForEntity(
                eq(certificateServiceUrl + "/api/certificates/generate"),
                any(),
                eq(Map.class)
        );
    }

    @Test
    void generateEventCertificate_NullResponse() {
        // Arrange
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        
        when(restTemplate.postForEntity(
                anyString(),
                any(),
                eq(Map.class)
        )).thenReturn(responseEntity);

        // Act
        String certificateId = certificateClientService.generateEventCertificate(testEvent);

        // Assert
        assertNull(certificateId);
        verify(restTemplate).postForEntity(
                eq(certificateServiceUrl + "/api/certificates/generate"),
                any(),
                eq(Map.class)
        );
    }

    @Test
    void getCertificateUrl_Success() {
        // Arrange
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", "cert-123");
        responseBody.put("eventId", 1L);
        responseBody.put("downloadUrl", "https://certificate-service.example.com/download/cert-123");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(Map.class)
        )).thenReturn(responseEntity);

        // Act
        String certificateUrl = certificateClientService.getCertificateUrl(1L);

        // Assert
        assertEquals("https://certificate-service.example.com/download/cert-123", certificateUrl);
        verify(restTemplate).exchange(
                eq(certificateServiceUrl + "/api/certificates/event/1"),
                eq(HttpMethod.GET),
                any(),
                eq(Map.class)
        );
    }

    @Test
    void getCertificateUrl_NoDownloadUrl() {
        // Arrange
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", "cert-123");
        responseBody.put("eventId", 1L);
        // No downloadUrl field

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(Map.class)
        )).thenReturn(responseEntity);

        // Act
        String certificateUrl = certificateClientService.getCertificateUrl(1L);

        // Assert
        assertNull(certificateUrl);
        verify(restTemplate).exchange(
                eq(certificateServiceUrl + "/api/certificates/event/1"),
                eq(HttpMethod.GET),
                any(),
                eq(Map.class)
        );
    }

    @Test
    void getCertificateUrl_NotFound() {
        // Arrange
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(Map.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Certificate not found"));

        // Act
        String certificateUrl = certificateClientService.getCertificateUrl(1L);

        // Assert
        assertNull(certificateUrl);
        verify(restTemplate).exchange(
                eq(certificateServiceUrl + "/api/certificates/event/1"),
                eq(HttpMethod.GET),
                any(),
                eq(Map.class)
        );
    }

    @Test
    void getCertificateInfo_Success() {
        // Arrange
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", "cert-123");
        responseBody.put("eventId", 1L);
        responseBody.put("status", "generated");
        responseBody.put("downloadUrl", "https://certificate-service.example.com/download/cert-123");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(Map.class)
        )).thenReturn(responseEntity);

        // Act
        Map<String, Object> certificateInfo = certificateClientService.getCertificateInfo(1L);

        // Assert
        assertNotNull(certificateInfo);
        assertEquals("cert-123", certificateInfo.get("id"));
        assertEquals(1L, certificateInfo.get("eventId"));
        assertEquals("generated", certificateInfo.get("status"));
        assertEquals("https://certificate-service.example.com/download/cert-123", certificateInfo.get("downloadUrl"));
        
        verify(restTemplate).exchange(
                eq(certificateServiceUrl + "/api/certificates/event/1"),
                eq(HttpMethod.GET),
                any(),
                eq(Map.class)
        );
    }

    @Test
    void getCertificateInfo_NotFound() {
        // Arrange
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(Map.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Certificate not found"));

        // Act
        Map<String, Object> certificateInfo = certificateClientService.getCertificateInfo(1L);

        // Assert
        assertNull(certificateInfo);
        verify(restTemplate).exchange(
                eq(certificateServiceUrl + "/api/certificates/event/1"),
                eq(HttpMethod.GET),
                any(),
                eq(Map.class)
        );
    }

    @Test
    void getServiceUrl_ReturnsConfiguredUrl() {
        // Act
        String serviceUrl = certificateClientService.getServiceUrl();

        // Assert
        assertEquals(certificateServiceUrl, serviceUrl);
    }
} 