package com.softuni.event.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedAccessException extends RuntimeException {
    
    public UnauthorizedAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedAccessException(String username, String resourceType, Object resourceId) {
        super(String.format("User '%s' is not authorized to access %s with ID: %s", 
                username, resourceType, resourceId));
    }
} 