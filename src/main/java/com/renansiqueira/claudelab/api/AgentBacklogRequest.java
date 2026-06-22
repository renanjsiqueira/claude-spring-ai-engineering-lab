package com.renansiqueira.claudelab.api;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/agent/backlog}.
 *
 * @param projectId target project identifier; must not be blank
 * @param message   the user's request; must not be blank
 */
public record AgentBacklogRequest(

        @NotBlank(message = "projectId must not be blank")
        String projectId,

        @NotBlank(message = "message must not be blank")
        String message
) {
}
