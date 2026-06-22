package com.renansiqueira.claudelab;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies that the Spring application context starts successfully.
 *
 * <p>Uses the {@code test} profile (in-memory H2) so the full context — including
 * JPA — boots without a running Postgres.
 */
@SpringBootTest
@ActiveProfiles("test")
class ClaudeLabApplicationTests {

    @Test
    void contextLoads() {
    }
}
