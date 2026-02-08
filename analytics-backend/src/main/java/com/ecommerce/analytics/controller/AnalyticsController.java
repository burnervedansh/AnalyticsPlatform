package com.ecommerce.analytics.controller;

import com.ecommerce.analytics.model.AnalyticsResponse;
import com.ecommerce.analytics.service.AnalyticsQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for analytics queries.
 * Provides endpoints for retrieving real-time metrics.
 */
@RestController
@RequestMapping("/api/analytics")
@Slf4j
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsQueryService analyticsQueryService;

    /**
     * Get active users count (last 5 minutes)
     * GET /api/analytics/active-users
     */
    @GetMapping("/active-users")
    public ResponseEntity<AnalyticsResponse.ActiveUsersResponse> getActiveUsers() {
        log.debug("Fetching active users");
        AnalyticsResponse.ActiveUsersResponse response = analyticsQueryService.getActiveUsers();
        return ResponseEntity.ok(response);
    }

    /**
     * Get top pages by view count (last 15 minutes)
     * GET /api/analytics/top-pages?limit=5
     */
    @GetMapping("/top-pages")
    public ResponseEntity<AnalyticsResponse.TopPagesResponse> getTopPages(
            @RequestParam(defaultValue = "5") int limit) {

        log.debug("Fetching top {} pages", limit);

        // Validate limit
        if (limit < 1 || limit > 100) {
            limit = 5;
        }

        AnalyticsResponse.TopPagesResponse response = analyticsQueryService.getTopPages(limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Get active sessions for a specific user (last 5 minutes)
     * GET /api/analytics/active-sessions?userId=usr_123
     */
    @GetMapping("/active-sessions")
    public ResponseEntity<AnalyticsResponse.ActiveSessionsResponse> getActiveSessions(
            @RequestParam String userId) {

        log.debug("Fetching active sessions for user: {}", userId);

        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        AnalyticsResponse.ActiveSessionsResponse response = analyticsQueryService.getActiveSessions(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get recent users with active sessions (last 5 minutes)
     * GET /api/analytics/recent-sessions?limit=5
     */
    @GetMapping("/recent-sessions")
    public ResponseEntity<AnalyticsResponse.RecentSessionsResponse> getRecentSessions(
            @RequestParam(defaultValue = "5") int limit) {

        log.debug("Fetching recent {} users with active sessions", limit);

        // Validate limit
        if (limit < 1 || limit > 50) {
            limit = 5;
        }

        AnalyticsResponse.RecentSessionsResponse response = analyticsQueryService.getRecentActiveSessions(limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Analytics service is running");
    }
}
