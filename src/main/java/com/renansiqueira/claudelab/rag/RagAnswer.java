package com.renansiqueira.claudelab.rag;

import java.util.List;

/**
 * Result of a RAG query: the generated answer and the source files it drew from.
 *
 * @param answer  the answer text
 * @param sources distinct source file names cited (empty when no context was found)
 */
public record RagAnswer(String answer, List<String> sources) {
}
