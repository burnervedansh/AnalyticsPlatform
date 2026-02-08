package com.ecommerce.generator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration properties for the event generator.
 * Can be customized via application.properties or environment variables.
 */
@Configuration
@ConfigurationProperties(prefix = "generator")
@Data
public class GeneratorConfig {

    /**
     * Target backend URL for posting events
     */
    private String backendUrl = "http://backend:8080/api/events";

    /**
     * Number of events to generate per second
     */
    private int eventsPerSecond = 50;

    /**
     * Number of unique users to simulate
     */
    private int userPoolSize = 100;

    /**
     * Number of concurrent sessions per user (1-3)
     */
    private int maxSessionsPerUser = 3;

    /**
     * List of page URLs to randomly select from
     */
    private List<String> pageUrls = List.of(
            "/home",
            "/products/electronics",
            "/products/clothing",
            "/products/books",
            "/products/sports",
            "/products/home-garden",
            "/cart",
            "/checkout",
            "/account",
            "/search",
            "/categories",
            "/deals",
            "/wishlist",
            "/orders",
            "/help"
    );

    /**
     * List of event types to randomly select from
     */
    private List<String> eventTypes = List.of(
            "page_view",
            "click",
            "add_to_cart",
            "remove_from_cart",
            "search",
            "filter"
    );

    /**
     * Enable/disable event generation
     */
    private boolean enabled = true;

    /**
     * Delay before starting generation (in milliseconds)
     */
    private long startupDelayMs = 5000;
}
