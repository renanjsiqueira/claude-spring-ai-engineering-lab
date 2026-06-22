package com.renansiqueira.claudelab.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal health endpoint used to prove the application boots correctly.
 *
 * <p>Intentionally does not call Claude — that wiring arrives in Phase 1.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("UP", "claude-spring-ai-engineering-lab", "phase-8");
    }

    /**
     * Simple immutable response describing the service status.
     */
    public record HealthResponse(String status, String service, String phase) {
    }
}
