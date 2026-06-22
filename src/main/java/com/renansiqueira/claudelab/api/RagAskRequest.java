package com.renansiqueira.claudelab.api;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/rag/ask}.
 *
 * @param question the question to answer from the knowledge base; must not be blank
 */
public record RagAskRequest(

        @NotBlank(message = "question must not be blank")
        String question
) {
}
