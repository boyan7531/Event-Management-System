package com.softuni.event.controller;

import com.softuni.event.model.entity.NotificationEntity;
import com.softuni.event.model.entity.UserEntity;
import com.softuni.event.service.NotificationService;
import com.softuni.event.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public String viewAllNotifications(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        UserEntity user = userService.getUserByUsername(userDetails.getUsername());
        List<NotificationEntity> notifications = notificationService.getNotificationsForUser(user);
        model.addAttribute("notifications", notifications);
        
        // Mark all as read when viewing all notifications
        notificationService.markAllAsRead(user);
        
        return "notifications";
    }

    @GetMapping("/unread-count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        UserEntity user = userService.getUserByUsername(userDetails.getUsername());
        long count = notificationService.getUnreadNotificationCount(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/unread")
    @ResponseBody
    public ResponseEntity<List<NotificationEntity>> getUnreadNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        UserEntity user = userService.getUserByUsername(userDetails.getUsername());
        List<NotificationEntity> notifications = notificationService.getUnreadNotificationsForUser(user);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/mark-read/{id}")
    @ResponseBody
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mark-all-read")
    @ResponseBody
    public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        UserEntity user = userService.getUserByUsername(userDetails.getUsername());
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }
} 