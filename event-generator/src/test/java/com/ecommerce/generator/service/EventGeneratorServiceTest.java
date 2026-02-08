package com.ecommerce.generator.service;

import com.ecommerce.generator.config.GeneratorConfig;
import com.ecommerce.generator.model.UserEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EventGeneratorService
 */
@ExtendWith(MockitoExtension.class)
class EventGeneratorServiceTest {

    @Mock
    private GeneratorConfig config;

    @InjectMocks
    private EventGeneratorService service;

    @BeforeEach
    void setUp() {
        // Set up default config values
        config = new GeneratorConfig();
        config.setBackendUrl("http://localhost:8080/api/events");
        config.setEventsPerSecond(50);
        config.setUserPoolSize(10);
        config.setMaxSessionsPerUser(3);
        config.setEnabled(true);
        
        service = new EventGeneratorService(config);
    }

    @Test
    void testGenerateRandomEvent_shouldCreateValidEvent() {
        // Use reflection to call private method for testing
        UserEvent event = ReflectionTestUtils.invokeMethod(service, "generateRandomEvent");

        assertNotNull(event);
        assertNotNull(event.getTimestamp());
        assertNotNull(event.getUserId());
        assertNotNull(event.getEventType());
        assertNotNull(event.getPageUrl());
        assertNotNull(event.getSessionId());
        
        assertTrue(event.getUserId().startsWith("usr_"));
        assertTrue(event.getSessionId().startsWith("sess_"));
    }

    @Test
    void testGenerateUserId_shouldBeWithinUserPool() {
        for (int i = 0; i < 100; i++) {
            String userId = ReflectionTestUtils.invokeMethod(service, "generateUserId");
            
            assertNotNull(userId);
            assertTrue(userId.startsWith("usr_"));
            
            // Extract user number and verify it's within pool
            String numberPart = userId.replace("usr_", "");
            int userNumber = Integer.parseInt(numberPart);
            
            assertTrue(userNumber >= 1 && userNumber <= config.getUserPoolSize(),
                    "User number should be between 1 and " + config.getUserPoolSize());
        }
    }

    @Test
    void testGetOrCreateSessionId_shouldCreateSessionForNewUser() {
        String userId = "usr_1";
        
        String sessionId = ReflectionTestUtils.invokeMethod(service, "getOrCreateSessionId", userId);
        
        assertNotNull(sessionId);
        assertTrue(sessionId.startsWith("sess_"));
    }

    @Test
    void testGetOrCreateSessionId_shouldReuseExistingSession() {
        String userId = "usr_1";
        
        // Create first session
        String sessionId1 = ReflectionTestUtils.invokeMethod(service, "getOrCreateSessionId", userId);
        
        // Subsequent calls should sometimes return the same session
        boolean foundSameSession = false;
        for (int i = 0; i < 50; i++) {
            String sessionId = ReflectionTestUtils.invokeMethod(service, "getOrCreateSessionId", userId);
            if (sessionId.equals(sessionId1)) {
                foundSameSession = true;
                break;
            }
        }
        
        assertTrue(foundSameSession, "Should reuse existing sessions");
    }

    @Test
    void testSelectRandom_shouldReturnElementFromList() {
        List<String> testList = List.of("A", "B", "C", "D");
        
        for (int i = 0; i < 20; i++) {
            String selected = ReflectionTestUtils.invokeMethod(service, "selectRandom", testList);
            assertTrue(testList.contains(selected));
        }
    }

    @Test
    void testUserEventCreate_shouldHaveCorrectFormat() {
        UserEvent event = UserEvent.create("usr_123", "page_view", "/home", "sess_456");
        
        assertEquals("usr_123", event.getUserId());
        assertEquals("page_view", event.getEventType());
        assertEquals("/home", event.getPageUrl());
        assertEquals("sess_456", event.getSessionId());
        assertNotNull(event.getTimestamp());
        
        // Timestamp should be in ISO 8601 format
        assertTrue(event.getTimestamp().contains("T"));
        assertTrue(event.getTimestamp().contains("Z"));
    }
}
