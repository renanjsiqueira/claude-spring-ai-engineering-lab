package com.renansiqueira.claudelab.rag;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Splits document text into chunks: one chunk per paragraph (blank-line separated),
 * with any oversized paragraph further split into fixed-size windows.
 */
@Service
public class DocumentChunkingService {

    private final int maxChunkChars;

    public DocumentChunkingService(
            @Value("${claudelab.rag.max-chunk-chars:1000}") int maxChunkChars) {
        if (maxChunkChars <= 0) {
            throw new IllegalArgumentException("maxChunkChars must be positive, was " + maxChunkChars);
        }
        this.maxChunkChars = maxChunkChars;
    }

    /**
     * Chunks the given text. Blank/whitespace-only fragments are dropped.
     */
    public List<String> chunk(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<String> chunks = new ArrayList<>();
        for (String paragraph : text.split("\\n\\s*\\n")) {
            String trimmed = paragraph.strip();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.length() <= maxChunkChars) {
                chunks.add(trimmed);
            } else {
                for (int start = 0; start < trimmed.length(); start += maxChunkChars) {
                    int end = Math.min(start + maxChunkChars, trimmed.length());
                    String window = trimmed.substring(start, end).strip();
                    if (!window.isEmpty()) {
                        chunks.add(window);
                    }
                }
            }
        }
        return chunks;
    }
}
