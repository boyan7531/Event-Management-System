package com.softuni.event.service;

import com.softuni.event.model.entity.EventEntity;
import com.softuni.event.model.entity.UserEntity;

import java.util.List;

public interface NotificationService {
    void notifyUpcomingEvents();
    void notifyEventStatusChange(EventEntity event);
    void notifyEventCreated(EventEntity event);
    void notifyEventCancelled(EventEntity event);
    void notifyUserJoinedEvent(UserEntity user, EventEntity event);
    List<String> getRecentNotifications(String username);
} 