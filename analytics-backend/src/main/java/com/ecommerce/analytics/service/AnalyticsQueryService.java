package com.ecommerce.analytics.service;

import com.ecommerce.analytics.model.AnalyticsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for querying analytics metrics.
 * Reads from Redis cache for fast responses.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsQueryService {

        private final RedisTemplate<String, Object> redisTemplate;

        private static final String ACTIVE_USERS_KEY = "metrics:active_users";
        private static final String PAGE_VIEWS_KEY = "metrics:page_views";
        private static final String USER_SESSIONS_PREFIX = "metrics:sessions:";

        /**
         * Get active users count
         */
        public AnalyticsResponse.ActiveUsersResponse getActiveUsers() {
                try {
                        Object countObj = redisTemplate.opsForValue().get(ACTIVE_USERS_KEY);

                        if (countObj != null) {
                                long count = countObj instanceof Integer ? ((Integer) countObj).longValue()
                                                : countObj instanceof Long ? (Long) countObj : 0L;

                                return AnalyticsResponse.ActiveUsersResponse.builder()
                                                .activeUsers(count)
                                                .timestamp(Instant.now().toString())
                                                .build();
                        }

                        return AnalyticsResponse.ActiveUsersResponse.builder()
                                        .activeUsers(0)
                                        .timestamp(Instant.now().toString())
                                        .build();

                } catch (Exception e) {
                        log.error("Error retrieving active users: {}", e.getMessage());
                        return AnalyticsResponse.ActiveUsersResponse.builder()
                                        .activeUsers(0)
                                        .timestamp(Instant.now().toString())
                                        .build();
                }
        }

        /**
         * Get top pages by view count
         */
        public AnalyticsResponse.TopPagesResponse getTopPages(int limit) {
                try {
                        Map<Object, Object> pageViews = redisTemplate.opsForHash()
                                        .entries(PAGE_VIEWS_KEY);

                        List<AnalyticsResponse.PageViewCount> topPages = pageViews.entrySet().stream()
                                        .map(entry -> {
                                                String url = (String) entry.getKey();
                                                Object viewsObj = entry.getValue();
                                                long views = viewsObj instanceof Integer
                                                                ? ((Integer) viewsObj).longValue()
                                                                : (Long) viewsObj;

                                                return AnalyticsResponse.PageViewCount.builder()
                                                                .url(url)
                                                                .views(views)
                                                                .build();
                                        })
                                        .sorted(Comparator.comparingLong(
                                                        AnalyticsResponse.PageViewCount::getViews).reversed())
                                        .limit(limit)
                                        .collect(Collectors.toList());

                        return AnalyticsResponse.TopPagesResponse.builder()
                                        .pages(topPages)
                                        .timestamp(Instant.now().toString())
                                        .build();

                } catch (Exception e) {
                        log.error("Error retrieving top pages: {}", e.getMessage());
                        return AnalyticsResponse.TopPagesResponse.builder()
                                        .pages(Collections.emptyList())
                                        .timestamp(Instant.now().toString())
                                        .build();
                }
        }

        /**
         * Get active sessions for a specific user
         */
        public AnalyticsResponse.ActiveSessionsResponse getActiveSessions(String userId) {
                try {
                        String key = USER_SESSIONS_PREFIX + userId;

                        Set<Object> sessions = redisTemplate.opsForSet().members(key);

                        if (sessions != null && !sessions.isEmpty()) {
                                List<String> sessionList = sessions.stream()
                                                .map(Object::toString)
                                                .collect(Collectors.toList());

                                return AnalyticsResponse.ActiveSessionsResponse.builder()
                                                .userId(userId)
                                                .activeSessions(sessionList.size())
                                                .sessions(sessionList)
                                                .timestamp(Instant.now().toString())
                                                .build();
                        }

                        return AnalyticsResponse.ActiveSessionsResponse.builder()
                                        .userId(userId)
                                        .activeSessions(0)
                                        .sessions(Collections.emptyList())
                                        .timestamp(Instant.now().toString())
                                        .build();

                } catch (Exception e) {
                        log.error("Error retrieving active sessions for user {}: {}",
                                        userId, e.getMessage());
                        return AnalyticsResponse.ActiveSessionsResponse.builder()
                                        .userId(userId)
                                        .activeSessions(0)
                                        .sessions(Collections.emptyList())
                                        .timestamp(Instant.now().toString())
                                        .build();
                }
        }

        /**
         * Get recent users with active sessions (limit to specified count)
         */
        public AnalyticsResponse.RecentSessionsResponse getRecentActiveSessions(int limit) {
                try {
                        // Get all session keys from Redis
                        Set<String> keys = redisTemplate.keys(USER_SESSIONS_PREFIX + "*");

                        if (keys == null || keys.isEmpty()) {
                                return AnalyticsResponse.RecentSessionsResponse.builder()
                                                .users(Collections.emptyList())
                                                .timestamp(Instant.now().toString())
                                                .build();
                        }

                        List<AnalyticsResponse.UserSessionInfo> userSessions = new ArrayList<>();

                        for (String key : keys) {
                                String userId = key.replace(USER_SESSIONS_PREFIX, "");
                                Set<Object> sessions = redisTemplate.opsForSet().members(key);

                                if (sessions != null && !sessions.isEmpty()) {
                                        List<String> sessionList = sessions.stream()
                                                        .map(Object::toString)
                                                        .collect(Collectors.toList());

                                        userSessions.add(AnalyticsResponse.UserSessionInfo.builder()
                                                        .userId(userId)
                                                        .activeSessions(sessionList.size())
                                                        .sessions(sessionList)
                                                        .build());
                                }
                        }

                        List<AnalyticsResponse.UserSessionInfo> topUsers = userSessions.stream()
                                        .sorted((a, b) -> Long.compare(b.getActiveSessions(), a.getActiveSessions()))
                                        .limit(limit)
                                        .collect(Collectors.toList());

                        return AnalyticsResponse.RecentSessionsResponse.builder()
                                        .users(topUsers)
                                        .timestamp(Instant.now().toString())
                                        .build();

                } catch (Exception e) {
                        log.error("Error retrieving recent active sessions: {}", e.getMessage());
                        return AnalyticsResponse.RecentSessionsResponse.builder()
                                        .users(Collections.emptyList())
                                        .timestamp(Instant.now().toString())
                                        .build();
                }
        }
}
