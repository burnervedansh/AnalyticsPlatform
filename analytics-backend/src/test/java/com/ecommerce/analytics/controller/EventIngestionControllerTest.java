package com.ecommerce.analytics.controller;

import com.ecommerce.analytics.model.UserEvent;
import com.ecommerce.analytics.service.EventIngestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for EventIngestionController
 */
@WebMvcTest(EventIngestionController.class)
class EventIngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventIngestionService eventIngestionService;

    @MockBean
    private Bucket eventIngestionBucket;

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

        // Mock bucket to always allow requests
        when(eventIngestionBucket.tryConsume(1)).thenReturn(true);
    }

    @Test
    void testIngestEvent_Success() throws Exception {
        // Arrange
        UserEvent savedEvent = UserEvent.builder()
                .id("event_12345")
                .timestamp(testEvent.getTimestamp())
                .userId(testEvent.getUserId())
                .eventType(testEvent.getEventType())
                .pageUrl(testEvent.getPageUrl())
                .sessionId(testEvent.getSessionId())
                .build();

        when(eventIngestionService.ingestEvent(any(UserEvent.class)))
                .thenReturn(savedEvent);

        // Act & Assert
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEvent)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.eventId").value("event_12345"));
    }

    @Test
    void testIngestEvent_RateLimitExceeded() throws Exception {
        // Arrange
        when(eventIngestionBucket.tryConsume(1)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEvent)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Rate limit exceeded. Please try again later."));
    }

    @Test
    void testIngestEvent_MissingRequiredField() throws Exception {
        // Arrange - create event with missing userId
        UserEvent invalidEvent = UserEvent.builder()
                .timestamp("2024-03-15T14:30:00Z")
                .eventType("page_view")
                .pageUrl("/home")
                .sessionId("sess_456")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEvent)))
                .andExpect(status().isBadRequest());
    }
}
