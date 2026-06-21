package com.renansiqueira.claudelab.api;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/backlog/analyze}.
 *
 * @param input a raw idea, bug report or feature request to analyze; must not be blank
 */
public record BacklogAnalysisRequest(

        @NotBlank(message = "input must not be blank")
        String input
) {
}
