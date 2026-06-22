package com.renansiqueira.claudelab.rag;

/**
 * A chunk paired with its relevance score for a query.
 *
 * @param chunk the matched chunk
 * @param score similarity score (higher is more relevant)
 */
public record ScoredChunk(DocumentChunk chunk, double score) {
}
