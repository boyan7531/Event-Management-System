package com.softuni.event.service.impl;

import com.softuni.event.model.entity.EventEntity;
import com.softuni.event.service.CertificateClientService;
import com.softuni.event.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class CertificateClientServiceImpl implements CertificateClientService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateClientServiceImpl.class);
    
    private final RestTemplate restTemplate;
    private final EventService eventService;
    
    @Value("${certificate.service.url}")
    private String certificateServiceUrl;
    
    @Value("${certificate.service.username:admin}")
    private String username;
    
    @Value("${certificate.service.password:admin}")
    private String password;

    public CertificateClientServiceImpl(EventService eventService) {
        this.restTemplate = new RestTemplate();
        this.eventService = eventService;
        logger.info("CertificateClientServiceImpl initialized");
    }
    
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        String authHeader = "Basic " + encodedAuth;
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);
        logger.debug("Created auth headers with Basic auth, username: {}", username);
        return headers;
    }

    @Override
    public String generateEventCertificate(EventEntity event) {
        logger.info("Generating certificate for event: ID={}, title={}", event.getId(), event.getTitle());
        try {
            String url = certificateServiceUrl + "/api/certificates/generate";
            
            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("id", event.getId());
            requestBody.put("title", event.getTitle());
            requestBody.put("description", event.getDescription());
            requestBody.put("date", event.getEventDate());
            requestBody.put("location", event.getLocation().getName());
            
            Map<String, Object> organizer = new HashMap<>();
            organizer.put("id", event.getOrganizer().getId());
            organizer.put("name", event.getOrganizer().getFirstName() + " " + event.getOrganizer().getLastName());
            requestBody.put("organizer", organizer);
            
            logger.debug("Request body: {}", requestBody);
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            try {
                ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
                logger.debug("Response status: {}", response.getStatusCode());
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    String certificateId = String.valueOf(response.getBody().get("id"));
                    logger.info("Certificate generated successfully with ID: {}", certificateId);
                    return certificateId;
                } else {
                    logger.warn("Failed to generate certificate. Status: {}", response.getStatusCode());
                    return null;
                }
            } catch (RestClientException e) {
                // Don't log stack trace for 4xx errors (likely certificate already exists)
                if (e instanceof HttpClientErrorException && 
                   ((HttpClientErrorException) e).getStatusCode().is4xxClientError()) {
                    logger.warn("Certificate service error: {}", e.getMessage());
                } else {
                    logger.error("Error generating certificate: {}", e.getMessage(), e);
                }
                return null;
            }
        } catch (Exception e) {
            logger.error("Unexpected error generating certificate: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String getCertificateUrl(Long eventId) {
        logger.info("Getting certificate URL for event ID: {}", eventId);
        try {
            String url = certificateServiceUrl + "/api/certificates/event/" + eventId;
            logger.debug("Getting certificate URL from: {}", url);
            
            HttpEntity<String> request = new HttpEntity<>(createAuthHeaders());
            logger.debug("Request headers: {}", request.getHeaders());
            
            try {
                logger.debug("Sending GET request to: {}", url);
                ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, request, Map.class);
                logger.debug("Response received: status={}", response.getStatusCode());
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> responseBody = response.getBody();
                    logger.debug("Response body: {}", responseBody);
                    
                    // Check if downloadUrl exists in the response
                    if (responseBody.containsKey("downloadUrl")) {
                        String downloadUrl = (String) responseBody.get("downloadUrl");
                        logger.info("Certificate found for event ID: {}, download URL: {}", eventId, downloadUrl);
                        return downloadUrl;
                    } else {
                        logger.warn("Response does not contain downloadUrl field: {}", responseBody);
                        return null;
                    }
                } else {
                    logger.warn("Certificate not found for event ID: {}. Status: {}", 
                           eventId, response.getStatusCode());
                    return null;
                }
            } catch (RestClientException e) {
                if (e instanceof HttpClientErrorException && 
                   ((HttpClientErrorException) e).getStatusCode() == HttpStatus.NOT_FOUND) {
                    logger.info("Certificate not found for event ID: {}", eventId);
                } else {
                    logger.error("REST client error when getting certificate URL: {}", e.getMessage(), e);
                }
                return null;
            }
        } catch (Exception e) {
            logger.error("Error retrieving certificate for event ID: {}", eventId, e);
            return null;
        }
    }

    @Override
    public Map<String, Object> getCertificateInfo(Long eventId) {
        logger.info("Getting certificate info for event ID: {}", eventId);
        try {
            String url = certificateServiceUrl + "/api/certificates/event/" + eventId;
            logger.debug("Getting certificate info from: {}", url);
            
            HttpEntity<String> request = new HttpEntity<>(createAuthHeaders());
            
            try {
                logger.debug("Sending GET request to: {}", url);
                ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, request, Map.class);
                logger.debug("Response received: status={}, body={}", response.getStatusCode(), response.getBody());
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> certificateInfo = response.getBody();
                    logger.info("Certificate info found for event ID: {}", eventId);
                    return certificateInfo;
                } else {
                    logger.warn("Certificate info not found for event ID: {}. Status: {}", 
                           eventId, response.getStatusCode());
                    return null;
                }
            } catch (RestClientException e) {
                // This is likely a 404 Not Found, which is expected if no certificate exists
                logger.debug("REST client error when getting certificate info (likely 404): {}", e.getMessage());
                return null;
            }
        } catch (Exception e) {
            logger.error("Error retrieving certificate info for event ID: {}", eventId, e);
            return null;
        }
    }

    @Override
    public String getServiceUrl() {
        logger.debug("Getting certificate service URL: {}", certificateServiceUrl);
        return certificateServiceUrl;
    }
} 