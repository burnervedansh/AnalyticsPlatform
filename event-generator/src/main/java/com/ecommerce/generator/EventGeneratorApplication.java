package com.ecommerce.generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Event Generator.
 * This standalone service generates mock user events and sends them to the analytics backend.
 */
@SpringBootApplication
@EnableScheduling
public class EventGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventGeneratorApplication.class, args);
    }
}
