package com.softuni.event.repository;

import com.softuni.event.model.entity.ContactMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessageEntity, Long> {
    // Find all messages sorted by creation date (newest first)
    List<ContactMessageEntity> findAllByOrderByCreatedAtDesc();
    
    // Find all unread messages
    List<ContactMessageEntity> findByReadOrderByCreatedAtDesc(boolean read);
    
    // Count unread messages
    long countByRead(boolean read);
} 