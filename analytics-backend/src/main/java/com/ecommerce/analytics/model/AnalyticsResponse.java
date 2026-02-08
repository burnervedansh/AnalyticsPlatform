package com.ecommerce.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response models for analytics queries
 */
public class AnalyticsResponse {

    /**
     * Response for active users count
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveUsersResponse {
        private long activeUsers;
        private String timestamp;
    }

    /**
     * Response for top pages
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPagesResponse {
        private List<PageViewCount> pages;
        private String timestamp;
    }

    /**
     * Individual page view count
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageViewCount {
        private String url;
        private long views;
    }

    /**
     * Response for active sessions
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveSessionsResponse {
        private String userId;
        private long activeSessions;
        private List<String> sessions;
        private String timestamp;
    }

    /**
     * Response for recent users with active sessions
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentSessionsResponse {
        private List<UserSessionInfo> users;
        private String timestamp;
    }

    /**
     * User session information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSessionInfo {
        private String userId;
        private long activeSessions;
        private List<String> sessions;
    }

    /**
     * Generic event ingestion response
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventIngestionResponse {
        private String status;
        private String eventId;
        private String message;
    }

    /**
     * Error response
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        private String error;
        private String message;
        private int status;
        private String timestamp;
        private Map<String, String> validationErrors;
    }
}
