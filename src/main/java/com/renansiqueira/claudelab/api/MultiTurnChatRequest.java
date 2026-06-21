package com.renansiqueira.claudelab.api;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/chat/conversations}.
 *
 * @param conversationId existing conversation to continue; if null/blank a new one is created
 * @param message        the user message; must not be blank
 * @param temperature    optional sampling temperature in [0.0, 1.0]; defaults to 0.2 when omitted
 */
public record MultiTurnChatRequest(

        String conversationId,

        @NotBlank(message = "message must not be blank")
        String message,

        @DecimalMin(value = "0.0", message = "temperature must be between 0.0 and 1.0")
        @DecimalMax(value = "1.0", message = "temperature must be between 0.0 and 1.0")
        Double temperature
) {
}
