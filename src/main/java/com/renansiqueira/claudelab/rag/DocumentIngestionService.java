package com.renansiqueira.claudelab.rag;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Loads knowledge-base documents, chunks them and indexes them in
 * {@link DocumentSearchService}. Runs once at startup so the RAG endpoint is
 * ready immediately; tolerant of a missing directory.
 */
@Service
public class DocumentIngestionService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final DocumentChunkingService chunkingService;
    private final DocumentSearchService searchService;
    private final Path knowledgeBaseDir;

    public DocumentIngestionService(
            DocumentChunkingService chunkingService,
            DocumentSearchService searchService,
            @Value("${claudelab.rag.knowledge-base-dir:docs/knowledge-base}") String knowledgeBaseDir) {
        this.chunkingService = chunkingService;
        this.searchService = searchService;
        this.knowledgeBaseDir = Path.of(knowledgeBaseDir);
    }

    @Override
    public void run(ApplicationArguments args) {
        ingestDirectory(knowledgeBaseDir);
    }

    /**
     * Ingests every {@code .md} file in the directory. Returns the number of chunks indexed.
     */
    public int ingestDirectory(Path dir) {
        if (!Files.isDirectory(dir)) {
            log.warn("Knowledge-base directory '{}' not found — RAG has no documents", dir);
            return 0;
        }
        int totalChunks = 0;
        try (Stream<Path> files = Files.list(dir)) {
            List<Path> markdown = files.filter(p -> p.toString().endsWith(".md")).sorted().toList();
            for (Path file : markdown) {
                String content = Files.readString(file);
                List<String> chunks = chunkingService.chunk(content);
                String source = file.getFileName().toString();
                for (int i = 0; i < chunks.size(); i++) {
                    searchService.index(new DocumentChunk(source, i, chunks.get(i)));
                }
                totalChunks += chunks.size();
                log.info("Ingested {} chunks from {}", chunks.size(), source);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to ingest knowledge base from " + dir, ex);
        }
        return totalChunks;
    }
}
