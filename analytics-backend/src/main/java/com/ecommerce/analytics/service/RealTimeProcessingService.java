package com.ecommerce.analytics.service;

import com.ecommerce.analytics.model.UserEvent;
import com.ecommerce.analytics.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service for real-time processing of events and updating metrics.
 * Runs periodically to calculate time-windowed metrics.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RealTimeProcessingService {

    private final EventRepository eventRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // Redis key constants
    private static final String ACTIVE_USERS_KEY = "metrics:active_users";
    private static final String PAGE_VIEWS_KEY = "metrics:page_views";
    private static final String USER_SESSIONS_PREFIX = "metrics:sessions:";

    // Time windows
    private static final Duration ACTIVE_USERS_WINDOW = Duration.ofMinutes(5);
    private static final Duration PAGE_VIEWS_WINDOW = Duration.ofMinutes(15);
    private static final Duration ACTIVE_SESSIONS_WINDOW = Duration.ofMinutes(5);

    /**
     * Process events and update metrics every 10 seconds
     */
    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
    public void processMetrics() {
        try {
            log.debug("Starting metrics processing...");

            updateActiveUsers();
            updatePageViews();
            updateActiveSessions();

            log.debug("Metrics processing completed");

        } catch (Exception e) {
            log.error("Error processing metrics: {}", e.getMessage(), e);
        }
    }

    /**
     * Update active users count (last 5 minutes)
     */
    private void updateActiveUsers() {
        Instant cutoffTime = Instant.now().minus(ACTIVE_USERS_WINDOW);

        List<UserEvent> recentEvents = eventRepository.findEventsBetween(
                cutoffTime, Instant.now());

        long activeUsers = recentEvents.stream()
                .map(UserEvent::getUserId)
                .distinct()
                .count();

        redisTemplate.opsForValue().set(ACTIVE_USERS_KEY, activeUsers);
        redisTemplate.expire(ACTIVE_USERS_KEY, ACTIVE_USERS_WINDOW.getSeconds(), TimeUnit.SECONDS);

        log.debug("Active users: {}", activeUsers);
    }

    /**
     * Update page views by URL (last 15 minutes)
     */
    private void updatePageViews() {
        Instant cutoffTime = Instant.now().minus(PAGE_VIEWS_WINDOW);

        List<UserEvent> recentEvents = eventRepository.findEventsBetween(
                cutoffTime, Instant.now());

        Map<String, Long> pageViewCounts = recentEvents.stream()
                .filter(event -> "page_view".equals(event.getEventType()))
                .collect(Collectors.groupingBy(
                        UserEvent::getPageUrl,
                        Collectors.counting()));

        // Clear old data
        redisTemplate.delete(PAGE_VIEWS_KEY);

        if (!pageViewCounts.isEmpty()) {
            redisTemplate.opsForHash().putAll(PAGE_VIEWS_KEY, pageViewCounts);
            redisTemplate.expire(PAGE_VIEWS_KEY, PAGE_VIEWS_WINDOW.getSeconds(), TimeUnit.SECONDS);
        }

        log.debug("Page views updated: {} pages tracked", pageViewCounts.size());
    }

    /**
     * Update active sessions for all users (last 5 minutes)
     */
    private void updateActiveSessions() {
        Instant cutoffTime = Instant.now().minus(ACTIVE_SESSIONS_WINDOW);

        List<UserEvent> recentEvents = eventRepository.findEventsBetween(
                cutoffTime, Instant.now());

        Map<String, Set<String>> userSessions = new HashMap<>();

        for (UserEvent event : recentEvents) {
            userSessions.computeIfAbsent(event.getUserId(), k -> new HashSet<>())
                    .add(event.getSessionId());
        }

        for (Map.Entry<String, Set<String>> entry : userSessions.entrySet()) {
            String userId = entry.getKey();
            Set<String> sessions = entry.getValue();

            String key = USER_SESSIONS_PREFIX + userId;

            // Clear old sessions
            redisTemplate.delete(key);

            for (String sessionId : sessions) {
                redisTemplate.opsForSet().add(key, sessionId);
            }

            // Set TTL
            redisTemplate.expire(key, ACTIVE_SESSIONS_WINDOW.getSeconds(), TimeUnit.SECONDS);
        }

        log.debug("Active sessions updated for {} users", userSessions.size());
    }

    /**
     * Cleanup old events - runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldEvents() {
        try {
            Instant retentionPeriod = Instant.now().minus(Duration.ofHours(24));

            long countBefore = eventRepository.count();
            eventRepository.deleteByCreatedAtBefore(retentionPeriod);
            long countAfter = eventRepository.count();

            log.info("Cleaned up {} old events. Remaining: {}",
                    countBefore - countAfter, countAfter);

        } catch (Exception e) {
            log.error("Failed to cleanup old events: {}", e.getMessage(), e);
        }
    }
}
