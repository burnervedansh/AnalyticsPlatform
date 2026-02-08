package com.ecommerce.generator.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserEvent model
 */
class UserEventTest {

    @Test
    void testBuilder_shouldCreateEventWithAllFields() {
        UserEvent event = UserEvent.builder()
                .timestamp("2024-03-15T14:30:00Z")
                .userId("usr_123")
                .eventType("page_view")
                .pageUrl("/home")
                .sessionId("sess_456")
                .build();

        assertEquals("2024-03-15T14:30:00Z", event.getTimestamp());
        assertEquals("usr_123", event.getUserId());
        assertEquals("page_view", event.getEventType());
        assertEquals("/home", event.getPageUrl());
        assertEquals("sess_456", event.getSessionId());
    }

    @Test
    void testCreate_shouldGenerateTimestampAutomatically() {
        UserEvent event = UserEvent.create("usr_123", "click", "/products", "sess_456");

        assertNotNull(event.getTimestamp());
        assertEquals("usr_123", event.getUserId());
        assertEquals("click", event.getEventType());
        assertEquals("/products", event.getPageUrl());
        assertEquals("sess_456", event.getSessionId());
    }

    @Test
    void testEquals_shouldCompareTwoEvents() {
        UserEvent event1 = UserEvent.builder()
                .timestamp("2024-03-15T14:30:00Z")
                .userId("usr_123")
                .eventType("page_view")
                .pageUrl("/home")
                .sessionId("sess_456")
                .build();

        UserEvent event2 = UserEvent.builder()
                .timestamp("2024-03-15T14:30:00Z")
                .userId("usr_123")
                .eventType("page_view")
                .pageUrl("/home")
                .sessionId("sess_456")
                .build();

        assertEquals(event1, event2);
    }

    @Test
    void testToString_shouldContainAllFields() {
        UserEvent event = UserEvent.create("usr_123", "page_view", "/home", "sess_456");
        String toString = event.toString();

        assertTrue(toString.contains("usr_123"));
        assertTrue(toString.contains("page_view"));
        assertTrue(toString.contains("/home"));
        assertTrue(toString.contains("sess_456"));
    }
}
