package com.renansiqueira.claudelab.api;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/chat}.
 *
 * @param message the user message to send to Claude; must not be blank
 */
public record ChatRequest(

        @NotBlank(message = "message must not be blank")
        String message
) {
}
