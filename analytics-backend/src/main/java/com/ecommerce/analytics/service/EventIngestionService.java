package com.ecommerce.analytics.service;

import com.ecommerce.analytics.model.UserEvent;
import com.ecommerce.analytics.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Service for handling event ingestion.
 * Validates and stores incoming user events.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventIngestionService {

    private final EventRepository eventRepository;

    /**
     * Ingest a new user event
     * 
     * @param event the event to ingest
     * @return the saved event with generated ID
     */
    @Transactional
    public UserEvent ingestEvent(UserEvent event) {
        try {
            event.setCreatedAt(Instant.now());
            // Validate timestamp format
            validateTimestamp(event.getTimestamp());

            UserEvent savedEvent = eventRepository.save(event);
            log.debug("Event ingested: {} from user: {}",
                    savedEvent.getEventType(), savedEvent.getUserId());
            return savedEvent;

        } catch (Exception e) {
            log.error("Failed to ingest event: {}", e.getMessage());
            throw new RuntimeException("Failed to ingest event: " + e.getMessage(), e);
        }
    }

    /**
     * Validate timestamp format (ISO 8601)
     */
    private void validateTimestamp(String timestamp) {
        try {
            Instant.parse(timestamp);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid timestamp format. Expected ISO 8601 format.");
        }
    }

    /**
     * Get total event count
     */
    public long getTotalEventCount() {
        return eventRepository.count();
    }

    /**
     * Clean up old events (older than retention period)
     * 
     * @param retentionPeriod how far back to keep events
     */
    @Transactional
    public void cleanupOldEvents(Instant retentionPeriod) {
        try {
            eventRepository.deleteByCreatedAtBefore(retentionPeriod);
            log.info("Cleaned up events older than {}", retentionPeriod);
        } catch (Exception e) {
            log.error("Failed to cleanup old events: {}", e.getMessage());
        }
    }
}
