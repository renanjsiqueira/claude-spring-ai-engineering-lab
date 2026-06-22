/**
 * <strong>rag</strong> — retrieval-augmented generation over the project knowledge base.
 *
 * <p>Documents are chunked ({@code DocumentChunkingService}), indexed in a simple in-memory vector
 * store ({@code DocumentSearchService}, lexical TF cosine similarity), ingested at startup
 * ({@code DocumentIngestionService}), and used to ground Claude's answers
 * ({@code RagAnswerService}). The store is deliberately simple so it runs offline; it can be
 * swapped for a Spring AI {@code VectorStore} + embedding model later.
 */
package com.renansiqueira.claudelab.rag;
