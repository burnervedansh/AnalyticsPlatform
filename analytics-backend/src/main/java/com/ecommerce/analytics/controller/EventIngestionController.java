package com.ecommerce.analytics.controller;

import com.ecommerce.analytics.model.AnalyticsResponse;
import com.ecommerce.analytics.model.UserEvent;
import com.ecommerce.analytics.service.EventIngestionService;
import io.github.bucket4j.Bucket;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for event ingestion.
 * Handles incoming user events with rate limiting.
 */
@RestController
@RequestMapping("/api/events")
@Slf4j
@RequiredArgsConstructor
public class EventIngestionController {

    private final EventIngestionService eventIngestionService;
    private final Bucket eventIngestionBucket;

    /**
     * Ingest a single user event
     * POST /api/events
     */
    @PostMapping
    public ResponseEntity<AnalyticsResponse.EventIngestionResponse> ingestEvent(
            @Valid @RequestBody UserEvent event) {

        // Check rate limit
        if (!eventIngestionBucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for event ingestion");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(AnalyticsResponse.EventIngestionResponse.builder()
                            .status("error")
                            .message("Rate limit exceeded. Please try again later.")
                            .build());
        }

        try {
            
            UserEvent savedEvent = eventIngestionService.ingestEvent(event);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(AnalyticsResponse.EventIngestionResponse.builder()
                            .status("success")
                            .eventId(savedEvent.getId())
                            .message("Event ingested successfully")
                            .build());

        } catch (IllegalArgumentException e) {
            log.warn("Invalid event data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AnalyticsResponse.EventIngestionResponse.builder()
                            .status("error")
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("Failed to ingest event: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AnalyticsResponse.EventIngestionResponse.builder()
                            .status("error")
                            .message("Internal server error")
                            .build());
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Event ingestion service is running");
    }
}
