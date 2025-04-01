package com.softuni.event.model.entity;

import com.softuni.event.model.enums.NotificationType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type")
    private NotificationType type;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_read")
    private boolean read;

    @Column(name = "link")
    private String link;

    public NotificationEntity() {
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }

    public NotificationEntity(String message, NotificationType type, UserEntity user, String link) {
        this();
        this.message = message;
        this.type = type;
        this.user = user;
        this.link = link;
    }

    public Long getId() {
        return id;
    }

    public NotificationEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public NotificationEntity setMessage(String message) {
        this.message = message;
        return this;
    }

    public NotificationType getType() {
        return type;
    }

    public NotificationEntity setType(NotificationType type) {
        this.type = type;
        return this;
    }

    public UserEntity getUser() {
        return user;
    }

    public NotificationEntity setUser(UserEntity user) {
        this.user = user;
        return this;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public NotificationEntity setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public boolean isRead() {
        return read;
    }

    public NotificationEntity setRead(boolean read) {
        this.read = read;
        return this;
    }

    public String getLink() {
        return link;
    }

    public NotificationEntity setLink(String link) {
        this.link = link;
        return this;
    }
} 