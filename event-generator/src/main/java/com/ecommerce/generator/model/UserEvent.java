package com.ecommerce.generator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Represents a user event in the e-commerce platform.
 * Matches the event schema expected by the analytics backend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {

    /**
     * ISO 8601 formatted timestamp of when the event occurred
     */
    @JsonProperty("timestamp")
    private String timestamp;

    /**
     * Unique identifier for the user
     */
    @JsonProperty("user_id")
    private String userId;

    /**
     * Type of event (e.g., page_view, click, purchase)
     */
    @JsonProperty("event_type")
    private String eventType;

    /**
     * URL of the page where the event occurred
     */
    @JsonProperty("page_url")
    private String pageUrl;

    /**
     * Session identifier for the user's browsing session
     */
    @JsonProperty("session_id")
    private String sessionId;

    /**
     * Creates a new event with the current timestamp
     */
    public static UserEvent create(String userId, String eventType, String pageUrl, String sessionId) {
        return UserEvent.builder()
                .timestamp(Instant.now().toString())
                .userId(userId)
                .eventType(eventType)
                .pageUrl(pageUrl)
                .sessionId(sessionId)
                .build();
    }
}
