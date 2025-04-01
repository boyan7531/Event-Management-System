package com.softuni.event.service;

import com.softuni.event.model.entity.EventEntity;
import com.softuni.event.model.entity.NotificationEntity;
import com.softuni.event.model.entity.UserEntity;
import com.softuni.event.model.enums.NotificationType;

import java.util.List;

public interface NotificationService {
    
    NotificationEntity createNotification(String message, NotificationType type, UserEntity user, String link);
    
    void createEventStatusNotification(EventEntity event, NotificationType type);
    
    void createPendingEventNotification(EventEntity event);
    
    List<NotificationEntity> getNotificationsForUser(UserEntity user);
    
    List<NotificationEntity> getUnreadNotificationsForUser(UserEntity user);
    
    long getUnreadNotificationCount(UserEntity user);
    
    void markAsRead(Long notificationId);
    
    void markAllAsRead(UserEntity user);
    
    void deleteNotification(Long notificationId);
    
    void notifyUpcomingEvents();
    
    void notifyEventStatusChange(EventEntity event);
    
    void notifyEventCreated(EventEntity event);
    
    void notifyEventCancelled(EventEntity event);
    
    void notifyUserJoinedEvent(UserEntity user, EventEntity event);
    
    List<String> getRecentNotifications(String username);
} 