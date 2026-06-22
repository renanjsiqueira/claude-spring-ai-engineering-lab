package com.renansiqueira.claudelab.api;

import java.util.List;

/**
 * Response body for {@code POST /api/rag/ask}.
 *
 * @param answer  the answer grounded in the knowledge base
 * @param sources distinct source file names cited (empty when no context was found)
 */
public record RagAskResponse(String answer, List<String> sources) {
}
