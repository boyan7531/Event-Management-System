package com.softuni.event.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.security.access.AccessDeniedException;

import java.util.Date;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoHandlerFoundException ex, Model model, HttpServletRequest request, HttpServletResponse response) {
        logger.error("Page not found: {}", request.getRequestURI());
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        model.addAttribute("status", 404);
        model.addAttribute("error", "Page Not Found");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("timestamp", new Date());
        return "error/404";
    }
    
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFound(NoResourceFoundException ex, Model model, HttpServletRequest request, HttpServletResponse response) {
        logger.error("Static resource not found: {}", request.getRequestURI());
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        model.addAttribute("status", 404);
        model.addAttribute("error", "Resource Not Found");
        model.addAttribute("message", "The requested resource was not found: " + ex.getResourcePath());
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("timestamp", new Date());
        return "error/404";
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model, HttpServletRequest request, HttpServletResponse response) {
        logger.error("Resource not found: {}", ex.getMessage());
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        model.addAttribute("status", 404);
        model.addAttribute("error", "Resource Not Found");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("timestamp", new Date());
        return "error/404";
    }
    
    @ExceptionHandler(UnauthorizedAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbidden(UnauthorizedAccessException ex, Model model, HttpServletRequest request, HttpServletResponse response) {
        logger.error("Unauthorized access: {}", ex.getMessage());
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        model.addAttribute("status", 403);
        model.addAttribute("error", "Access Denied");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("timestamp", new Date());
        return "error/403";
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException ex, Model model, HttpServletRequest request, HttpServletResponse response) {
        logger.warn("Access Denied: {} for user {} accessing {}", ex.getMessage(), request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous", request.getRequestURI());
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        model.addAttribute("status", 403);
        model.addAttribute("error", "Forbidden");
        model.addAttribute("message", "You do not have permission to access this resource.");
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("timestamp", new Date());
        return "error/403";
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralError(Exception ex, Model model, HttpServletRequest request, HttpServletResponse response) {
        logger.error("Internal server error", ex);
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        model.addAttribute("status", 500);
        model.addAttribute("error", "Internal Server Error");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("timestamp", new Date());
        return "error/error";
    }
} 