package com.ecommerce.analytics.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * User event entity stored in MongoDB.
 * Represents a single user action on the e-commerce platform.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "events")
public class UserEvent {

    @Id
    private String id;

    /**
     * ISO 8601 formatted timestamp of when the event occurred
     */
    @NotBlank(message = "Timestamp is required")
    @JsonProperty("timestamp")
    private String timestamp;

    /**
     * Unique identifier for the user
     */
    @NotBlank(message = "User ID is required")
    @JsonProperty("user_id")
    @Indexed
    private String userId;

    /**
     * Type of event (e.g., page_view, click, purchase)
     */
    @NotBlank(message = "Event type is required")
    @JsonProperty("event_type")
    private String eventType;

    /**
     * URL of the page where the event occurred
     */
    @NotBlank(message = "Page URL is required")
    @JsonProperty("page_url")
    @Indexed
    private String pageUrl;

    /**
     * Session identifier for the user's browsing session
     */
    @NotBlank(message = "Session ID is required")
    @JsonProperty("session_id")
    @Indexed
    private String sessionId;

    /**
     * Timestamp when this record was created in the database
     */
    @CreatedDate
    @Indexed
    private Instant createdAt;

    /**
     * Convert timestamp string to Instant for time-based queries
     */
    public Instant getTimestampAsInstant() {
        try {
            return Instant.parse(timestamp);
        } catch (Exception e) {
            return Instant.now();
        }
    }
}
