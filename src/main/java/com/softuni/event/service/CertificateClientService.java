package com.softuni.event.service;

import com.softuni.event.model.entity.EventEntity;

import java.util.Map;

/**
 * Client service for communicating with the certificate microservice
 */
public interface CertificateClientService {
    
    /**
     * Requests generation of a certificate for an approved event
     * @param event The approved event
     * @return The certificate ID or null if generation failed
     */
    String generateEventCertificate(EventEntity event);
    
    /**
     * Gets the URL to download a certificate by event ID
     * @param eventId The event ID
     * @return The download URL or null if not found
     */
    String getCertificateUrl(Long eventId);
    
    /**
     * Gets certificate information by event ID
     * @param eventId The event ID
     * @return Map containing certificate details or null if not found
     */
    Map<String, Object> getCertificateInfo(Long eventId);
    
    /**
     * Gets the base URL of the certificate service
     * @return The certificate service URL
     */
    String getServiceUrl();
    
    /**
     * Generate a certificate if none exists or get the existing one
     * @param eventId The event ID
     * @param event The event entity
     * @return The URL for the certificate
     */
    default String getOrGenerateCertificate(Long eventId, EventEntity event) {
        String certificateUrl = getCertificateUrl(eventId);
        if (certificateUrl == null) {
            generateEventCertificate(event);
            // Try to get URL again after generation
            certificateUrl = getCertificateUrl(eventId);
        }
        return certificateUrl;
    }
} 