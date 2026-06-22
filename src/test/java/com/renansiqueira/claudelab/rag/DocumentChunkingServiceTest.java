package com.renansiqueira.claudelab.rag;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class DocumentChunkingServiceTest {

    @Test
    void splitsByParagraphAndDropsBlanks() {
        DocumentChunkingService service = new DocumentChunkingService(1000);

        List<String> chunks = service.chunk("First paragraph.\n\n   \n\nSecond paragraph.");

        assertThat(chunks).containsExactly("First paragraph.", "Second paragraph.");
    }

    @Test
    void splitsOversizedParagraphIntoFixedWindows() {
        DocumentChunkingService service = new DocumentChunkingService(10);

        List<String> chunks = service.chunk("abcdefghijklmnopqrstuvwxy"); // 25 chars

        assertThat(chunks).hasSize(3);
        assertThat(chunks.get(0)).hasSize(10);
        assertThat(String.join("", chunks)).isEqualTo("abcdefghijklmnopqrstuvwxy");
    }

    @Test
    void emptyTextYieldsNoChunks() {
        DocumentChunkingService service = new DocumentChunkingService(1000);

        assertThat(service.chunk("   ")).isEmpty();
        assertThat(service.chunk(null)).isEmpty();
    }
}
