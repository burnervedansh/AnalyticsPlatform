package com.ecommerce.analytics.repository;

import com.ecommerce.analytics.model.UserEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for UserEvent entities.
 * Provides data access methods for event storage and retrieval.
 */
@Repository
public interface EventRepository extends MongoRepository<UserEvent, String> {

    /**
     * Find all events within a time range
     */
    @Query("{ 'createdAt': { $gte: ?0, $lte: ?1 } }")
    List<UserEvent> findEventsBetween(Instant start, Instant end);

    /**
     * Find events for a specific user within a time range
     */
    @Query("{ 'userId': ?0, 'createdAt': { $gte: ?1 } }")
    List<UserEvent> findByUserIdAndCreatedAtAfter(String userId, Instant after);

    /**
     * Find events for a specific page URL within a time range
     */
    @Query("{ 'pageUrl': ?0, 'createdAt': { $gte: ?1 } }")
    List<UserEvent> findByPageUrlAndCreatedAtAfter(String pageUrl, Instant after);

    /**
     * Count distinct users in a time range
     */
    @Query(value = "{ 'createdAt': { $gte: ?0 } }", count = true)
    long countEventsAfter(Instant after);

    /**
     * Delete events older than a specific time (for cleanup)
     */
    void deleteByCreatedAtBefore(Instant before);
}
