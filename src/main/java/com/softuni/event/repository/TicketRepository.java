package com.softuni.event.repository;

import com.softuni.event.model.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity, Long> {
    List<TicketEntity> findByEventId(Long eventId);
    List<TicketEntity> findByUserId(Long userId);
    Optional<TicketEntity> findByTicketNumber(String ticketNumber);
    
    @Query("SELECT COUNT(t) FROM TicketEntity t WHERE t.event.id = ?1")
    Long countTicketsByEventId(Long eventId);
    
    @Query("SELECT t FROM TicketEntity t WHERE t.event.id = ?1 AND t.used = ?2")
    List<TicketEntity> findByEventIdAndUsed(Long eventId, boolean used);
} 