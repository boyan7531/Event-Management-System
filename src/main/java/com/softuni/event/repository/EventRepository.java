package com.softuni.event.repository;

import com.softuni.event.model.entity.EventEntity;
import com.softuni.event.model.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {
    List<EventEntity> findByStatus(EventStatus status);
    List<EventEntity> findByOrganizerId(Long organizerId);
    
    @Query("SELECT e FROM EventEntity e WHERE e.eventDate > ?1 AND e.status = ?2")
    List<EventEntity> findUpcomingEvents(LocalDateTime now, EventStatus status);
    
    @Query("SELECT e FROM EventEntity e WHERE e.eventDate < ?1 AND e.status = ?2")
    List<EventEntity> findPastEvents(LocalDateTime now, EventStatus status);
    
    @Query("SELECT e FROM EventEntity e WHERE e.eventDate BETWEEN ?1 AND ?2 AND e.status = ?3")
    List<EventEntity> findEventsBetweenDates(LocalDateTime start, LocalDateTime end, EventStatus status);
    
    @Query("SELECT e FROM EventEntity e WHERE e.title LIKE %?1% OR e.description LIKE %?1%")
    List<EventEntity> searchByTitleOrDescription(String keyword);
} 