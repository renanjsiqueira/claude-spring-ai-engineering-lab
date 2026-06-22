package com.renansiqueira.claudelab.rag;

/**
 * A chunk of a knowledge-base document.
 *
 * @param source the source file name (used as the citation)
 * @param index  the chunk's position within its source
 * @param text   the chunk text
 */
public record DocumentChunk(String source, int index, String text) {
}
