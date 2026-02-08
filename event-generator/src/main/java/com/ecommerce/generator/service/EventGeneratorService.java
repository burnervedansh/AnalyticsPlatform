package com.ecommerce.generator.service;

import com.ecommerce.generator.config.GeneratorConfig;
import com.ecommerce.generator.model.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service responsible for generating and sending mock user events.
 * Generates events at a configurable rate and sends them to the analytics
 * backend.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventGeneratorService {

    private final GeneratorConfig config;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    // Track active sessions per user
    private final Map<String, Set<String>> userSessions = new ConcurrentHashMap<>();

    // Dynamic user pool size (changes every 5 seconds)
    private volatile int currentUserPoolSize = 100;

    // Statistics
    private final AtomicLong totalEventsSent = new AtomicLong(0);
    private final AtomicLong successfulEvents = new AtomicLong(0);
    private final AtomicLong failedEvents = new AtomicLong(0);

    /**
     * Start generating events after application is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (config.isEnabled()) {
            log.info("Event Generator starting...");
            log.info("Configuration:");
            log.info("  - Backend URL: {}", config.getBackendUrl());
            log.info("  - Events per second: {}", config.getEventsPerSecond());
            log.info("  - User pool size: {}", config.getUserPoolSize());
            log.info("  - Startup delay: {}ms", config.getStartupDelayMs());

            try {
                Thread.sleep(config.getStartupDelayMs());
                log.info("Event generation started!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Startup delay interrupted", e);
            }
        } else {
            log.warn("Event generator is disabled");
        }
    }

    /**
     * Generate and send events at approximately 50 events per second.
     * Fixed delay of 20ms = 1000ms / 50 events = 50 events/second
     */
    @Scheduled(fixedDelay = 20)
    public void generateAndSendEvent() {
        if (!config.isEnabled()) {
            return;
        }

        try {
            UserEvent event = generateRandomEvent();
            sendEvent(event);
        } catch (Exception e) {
            log.error("Error generating/sending event", e);
            failedEvents.incrementAndGet();
        }
    }

    /**
     * Generate a random user event with realistic data
     */
    private UserEvent generateRandomEvent() {
        String userId = generateUserId();
        String sessionId = getOrCreateSessionId(userId);
        String eventType = selectRandom(config.getEventTypes());
        String pageUrl = selectRandom(config.getPageUrls());

        return UserEvent.create(userId, eventType, pageUrl, sessionId);
    }

    /**
     * Update user pool size every 5 seconds to create dynamic active user counts.
     * Varies between 800 and 1000 users.
     */
    @Scheduled(fixedRate = 5000)
    public void updateUserPoolSize() {
        currentUserPoolSize = 800 + random.nextInt(201);
        log.debug("User pool size updated to: {}", currentUserPoolSize);
    }

    /**
     * Generate a user ID from the dynamic user pool
     */
    private String generateUserId() {
        int userNumber = random.nextInt(currentUserPoolSize) + 1;
        return String.format("usr_%d", userNumber);
    }

    /**
     * Get an existing session or create a new one for the user.
     * Users can have 1-3 concurrent sessions.
     */
    private String getOrCreateSessionId(String userId) {
        userSessions.putIfAbsent(userId, ConcurrentHashMap.newKeySet());
        Set<String> sessions = userSessions.get(userId);

        // Randomly decide to create a new session or use existing
        if (sessions.isEmpty() || (sessions.size() < config.getMaxSessionsPerUser() && random.nextDouble() < 0.1)) {
            String newSessionId = String.format("sess_%s_%d",
                    userId.replace("usr_", ""),
                    UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
            sessions.add(newSessionId);

            // Occasionally clean up old sessions to keep realistic count
            if (sessions.size() > config.getMaxSessionsPerUser()) {
                String oldSession = sessions.iterator().next();
                sessions.remove(oldSession);
            }

            return newSessionId;
        }

        // Return a random existing session
        List<String> sessionList = new ArrayList<>(sessions);
        return sessionList.get(random.nextInt(sessionList.size()));
    }

    /**
     * Select a random element from a list
     */
    private <T> T selectRandom(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    /**
     * Send event to the backend API
     */
    private void sendEvent(UserEvent event) {
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    config.getBackendUrl(),
                    event,
                    String.class);

            totalEventsSent.incrementAndGet();

            if (response.getStatusCode() == HttpStatus.CREATED ||
                    response.getStatusCode() == HttpStatus.OK) {
                successfulEvents.incrementAndGet();

                if (totalEventsSent.get() % 100 == 0) {
                    log.info("Events sent: {} | Success: {} | Failed: {}",
                            totalEventsSent.get(),
                            successfulEvents.get(),
                            failedEvents.get());
                }
            } else {
                failedEvents.incrementAndGet();
                log.warn("Unexpected response status: {} for event: {}",
                        response.getStatusCode(), event);
            }

        } catch (Exception e) {
            failedEvents.incrementAndGet();

            if (failedEvents.get() % 10 == 1) {
                log.error("Failed to send event (showing every 10th error): {}", e.getMessage());
            }
        }
    }

    /**
     * Print statistics periodically
     */
    @Scheduled(fixedRate = 30000)
    public void printStatistics() {
        if (config.isEnabled() && totalEventsSent.get() > 0) {
            log.info("=== Event Generator Statistics ===");
            log.info("Total Events: {} | Success: {} | Failed: {} | Success Rate: {:.2f}%",
                    totalEventsSent.get(),
                    successfulEvents.get(),
                    failedEvents.get(),
                    (successfulEvents.get() * 100.0 / totalEventsSent.get()));
            log.info("Active Users: {} | Total Sessions: {}",
                    userSessions.size(),
                    userSessions.values().stream().mapToInt(Set::size).sum());
            log.info("==================================");
        }
    }
}
