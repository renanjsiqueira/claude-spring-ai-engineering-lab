package com.renansiqueira.claudelab.rag;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class DocumentSearchServiceTest {

    private DocumentSearchService newIndex() {
        DocumentSearchService search = new DocumentSearchService();
        search.index(new DocumentChunk("coding-standards.md", 0,
                "When creating a new feature, model payloads as records and validate request bodies."));
        search.index(new DocumentChunk("domain-glossary.md", 0,
                "Priority is the urgency of a backlog item: LOW, MEDIUM, HIGH or CRITICAL."));
        return search;
    }

    @Test
    void retrievesMostRelevantChunkFirst() {
        DocumentSearchService search = newIndex();

        List<ScoredChunk> results = search.search("standards for creating a new feature", 4);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).chunk().source()).isEqualTo("coding-standards.md");
        assertThat(results.get(0).score()).isGreaterThan(0.0);
    }

    @Test
    void unrelatedQueryReturnsNothing() {
        DocumentSearchService search = newIndex();

        assertThat(search.search("photosynthesis chlorophyll sunlight", 4)).isEmpty();
    }

    @Test
    void respectsTopKLimit() {
        DocumentSearchService search = newIndex();

        assertThat(search.search("feature priority backlog item", 1)).hasSize(1);
    }
}
