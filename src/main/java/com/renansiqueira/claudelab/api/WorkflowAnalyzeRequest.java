package com.renansiqueira.claudelab.api;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/workflows/analyze}.
 *
 * @param input    the text to analyze; must not be blank
 * @param workflow which workflow to run (CHAINING, ROUTING, PARALLEL); optional, defaults to CHAINING
 */
public record WorkflowAnalyzeRequest(

        @NotBlank(message = "input must not be blank")
        String input,

        String workflow
) {
}
