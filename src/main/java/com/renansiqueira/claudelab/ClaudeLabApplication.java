package com.renansiqueira.claudelab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Claude Spring AI Engineering Lab.
 *
 * <p>This project is built incrementally, phase by phase, to demonstrate the
 * concepts from the "Claude with the Anthropic API" course applied to Java 21,
 * Spring Boot and Spring AI. Phase 0 only establishes the foundation: the
 * application boots and exposes a health endpoint. No Claude integration yet.
 */
@SpringBootApplication
public class ClaudeLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClaudeLabApplication.class, args);
    }
}
