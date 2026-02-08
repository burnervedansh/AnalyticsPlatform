package com.ecommerce.analytics.service;

import com.ecommerce.analytics.model.UserEvent;
import com.ecommerce.analytics.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EventIngestionService
 */
@ExtendWith(MockitoExtension.class)
class EventIngestionServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventIngestionService eventIngestionService;

    private UserEvent testEvent;

    @BeforeEach
    void setUp() {
        testEvent = UserEvent.builder()
                .timestamp("2024-03-15T14:30:00Z")
                .userId("usr_123")
                .eventType("page_view")
                .pageUrl("/home")
                .sessionId("sess_456")
                .build();
    }

    @Test
    void testIngestEvent_Success() {
        // Arrange
        UserEvent savedEvent = UserEvent.builder()
                .id("event_12345")
                .timestamp(testEvent.getTimestamp())
                .userId(testEvent.getUserId())
                .eventType(testEvent.getEventType())
                .pageUrl(testEvent.getPageUrl())
                .sessionId(testEvent.getSessionId())
                .createdAt(Instant.now())
                .build();

        when(eventRepository.save(any(UserEvent.class))).thenReturn(savedEvent);

        // Act
        UserEvent result = eventIngestionService.ingestEvent(testEvent);

        // Assert
        assertNotNull(result);
        assertEquals("event_12345", result.getId());
        assertEquals("usr_123", result.getUserId());
        verify(eventRepository, times(1)).save(any(UserEvent.class));
    }

    @Test
    void testIngestEvent_InvalidTimestamp() {
        // Arrange
        testEvent.setTimestamp("invalid-timestamp");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            eventIngestionService.ingestEvent(testEvent);
        });
    }

    @Test
    void testGetTotalEventCount() {
        // Arrange
        when(eventRepository.count()).thenReturn(1500L);

        // Act
        long count = eventIngestionService.getTotalEventCount();

        // Assert
        assertEquals(1500L, count);
        verify(eventRepository, times(1)).count();
    }

    @Test
    void testCleanupOldEvents() {
        // Arrange
        Instant cutoff = Instant.now().minusSeconds(86400);

        // Act
        eventIngestionService.cleanupOldEvents(cutoff);

        // Assert
        verify(eventRepository, times(1)).deleteByCreatedAtBefore(cutoff);
    }
}
