package com.softuni.event.repository;

import com.softuni.event.model.entity.NotificationEntity;
import com.softuni.event.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    
    List<NotificationEntity> findByUserOrderByCreatedAtDesc(UserEntity user);
    
    List<NotificationEntity> findByUserAndReadFalseOrderByCreatedAtDesc(UserEntity user);
    
    long countByUserAndReadFalse(UserEntity user);
} 