package com.renansiqueira.claudelab.application;

import java.time.Instant;

/**
 * API view of a project.
 */
public record ProjectResponse(
        String id,
        String name,
        String description,
        Instant createdAt
) {
}
