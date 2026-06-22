package com.renansiqueira.claudelab.rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DocumentIngestionServiceTest {

    private DocumentIngestionService ingestionFor(DocumentSearchService search, Path dir) {
        return new DocumentIngestionService(new DocumentChunkingService(1000), search, dir.toString());
    }

    @Test
    void ingestsMarkdownFilesAndIndexesThemBySource(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("a.md"), "Alpha rules about features.\n\nSecond alpha paragraph.");
        Files.writeString(dir.resolve("b.md"), "Beta glossary of domain terms.");
        Files.writeString(dir.resolve("ignore.txt"), "not markdown");

        DocumentSearchService search = new DocumentSearchService();
        int chunks = ingestionFor(search, dir).ingestDirectory(dir);

        assertThat(chunks).isEqualTo(3);
        assertThat(search.size()).isEqualTo(3);

        List<ScoredChunk> hits = search.search("features alpha", 5);
        assertThat(hits).isNotEmpty();
        assertThat(hits.get(0).chunk().source()).isEqualTo("a.md");
    }

    @Test
    void missingDirectoryIngestsNothing(@TempDir Path dir) {
        DocumentSearchService search = new DocumentSearchService();

        int chunks = ingestionFor(search, dir).ingestDirectory(dir.resolve("missing"));

        assertThat(chunks).isZero();
        assertThat(search.size()).isZero();
    }

    @Test
    void ingestsRealKnowledgeBase() {
        Path kb = Path.of("docs/knowledge-base");
        assumeTrue(Files.isDirectory(kb), "knowledge base directory present");

        DocumentSearchService search = new DocumentSearchService();
        int chunks = ingestionFor(search, kb).ingestDirectory(kb);

        assertThat(chunks).isPositive();
        // The example question's key term should retrieve a relevant chunk.
        assertThat(search.search("padrões para criar uma nova feature", 4)).isNotEmpty();
    }
}
