package com.softuni.event.repository;

import com.softuni.event.model.entity.EventEntity;
import com.softuni.event.model.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {
    List<EventEntity> findByStatus(EventStatus status);
    List<EventEntity> findByOrganizerId(Long organizerId);
    
    @Query("SELECT e FROM EventEntity e WHERE e.eventDate > ?1 AND e.status = ?2 ORDER BY e.eventDate")
    List<EventEntity> findUpcomingEvents(LocalDateTime now, EventStatus status);
    
    @Query("SELECT e FROM EventEntity e WHERE e.eventDate < ?1 AND e.status = ?2 ORDER BY e.eventDate DESC")
    List<EventEntity> findPastEvents(LocalDateTime now, EventStatus status);
    
    @Query("SELECT e FROM EventEntity e WHERE e.eventDate BETWEEN ?1 AND ?2 AND e.status = ?3")
    List<EventEntity> findEventsBetweenDates(LocalDateTime start, LocalDateTime end, EventStatus status);
    
    @Query("SELECT e FROM EventEntity e WHERE (LOWER(e.title) LIKE LOWER(CONCAT('%', ?1, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', ?1, '%'))) AND e.status = com.softuni.event.model.enums.EventStatus.APPROVED")
    List<EventEntity> searchByTitleOrDescription(String keyword);
    
    // Calendar feature query
    List<EventEntity> findByEventDateBetweenAndStatus(LocalDateTime start, LocalDateTime end, EventStatus status);
    
    // Find events at the same location with overlapping time
    @Query("SELECT e FROM EventEntity e WHERE e.location.id = :locationId AND e.status IN :statuses AND (e.id != :excludeEventId OR :excludeEventId IS NULL) " +
           "AND (e.eventDate BETWEEN :eventStart AND :eventEnd " +
           "OR (e.eventDate < :eventStart AND FUNCTION('DATEADD', HOUR, 3, e.eventDate) > :eventStart))")
    List<EventEntity> findOverlappingEvents(
            @Param("locationId") Long locationId,
            @Param("eventStart") LocalDateTime eventStart,
            @Param("eventEnd") LocalDateTime eventEnd,
            @Param("excludeEventId") Long excludeEventId,
            @Param("statuses") List<EventStatus> statuses);

    /**
     * Find an event by ID with all necessary associations eagerly loaded
     */
    @Query("SELECT e FROM EventEntity e LEFT JOIN FETCH e.organizer LEFT JOIN FETCH e.location LEFT JOIN FETCH e.attendees WHERE e.id = :eventId")
    EventEntity findByIdWithAssociations(@Param("eventId") Long eventId);
} 