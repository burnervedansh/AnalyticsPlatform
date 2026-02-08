package com.ecommerce.analytics.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Rate limiting configuration using Bucket4j.
 * Implements token bucket algorithm for API rate limiting.
 */
@Configuration
public class RateLimitConfig {

    @Value("${rate-limit.events-per-second:100}")
    private int eventsPerSecond;

    @Value("${rate-limit.burst-capacity:200}")
    private int burstCapacity;

    /**
     * Create a bucket for rate limiting event ingestion.
     * Allows burst up to burstCapacity, refills at eventsPerSecond rate.
     */
    @Bean
    public Bucket eventIngestionBucket() {
        // Define bandwidth: refill tokens at eventsPerSecond rate
        Bandwidth limit = Bandwidth.classic(
                burstCapacity,
                Refill.intervally(eventsPerSecond, Duration.ofSeconds(1)));

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
