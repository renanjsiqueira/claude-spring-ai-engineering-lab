package com.renansiqueira.claudelab.rag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

/**
 * A simple in-memory vector store.
 *
 * <p>Each chunk is represented as a term-frequency vector (lexical "embedding");
 * search ranks chunks by cosine similarity to the query vector. This is fully
 * deterministic and offline — no embedding API needed — and exposes the same
 * shape (index + search) a real Spring AI {@code VectorStore} would, so it can be
 * swapped later.
 */
@Service
public class DocumentSearchService {

    private record IndexedChunk(DocumentChunk chunk, Map<String, Integer> termFrequencies, double norm) {
    }

    private final List<IndexedChunk> index = new CopyOnWriteArrayList<>();

    /** Indexes a single chunk. */
    public void index(DocumentChunk chunk) {
        Map<String, Integer> tf = termFrequencies(tokenize(chunk.text()));
        index.add(new IndexedChunk(chunk, tf, norm(tf)));
    }

    /** Returns the matching chunks (score &gt; 0) ordered by descending relevance, capped at {@code topK}. */
    public List<ScoredChunk> search(String query, int topK) {
        Map<String, Integer> queryTf = termFrequencies(tokenize(query));
        double queryNorm = norm(queryTf);
        if (queryNorm == 0.0) {
            return List.of();
        }
        return index.stream()
                .map(c -> new ScoredChunk(c.chunk(), cosine(queryTf, queryNorm, c)))
                .filter(s -> s.score() > 0.0)
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .limit(topK)
                .toList();
    }

    public int size() {
        return index.size();
    }

    public void clear() {
        index.clear();
    }

    private static double cosine(Map<String, Integer> queryTf, double queryNorm, IndexedChunk chunk) {
        if (chunk.norm() == 0.0) {
            return 0.0;
        }
        // Iterate the smaller map for the dot product.
        Map<String, Integer> small = queryTf.size() <= chunk.termFrequencies().size()
                ? queryTf : chunk.termFrequencies();
        Map<String, Integer> large = small == queryTf ? chunk.termFrequencies() : queryTf;
        double dot = 0.0;
        for (Map.Entry<String, Integer> e : small.entrySet()) {
            Integer other = large.get(e.getKey());
            if (other != null) {
                dot += (double) e.getValue() * other;
            }
        }
        return dot / (queryNorm * chunk.norm());
    }

    private static Map<String, Integer> termFrequencies(List<String> tokens) {
        Map<String, Integer> tf = new HashMap<>();
        for (String token : tokens) {
            tf.merge(token, 1, Integer::sum);
        }
        return tf;
    }

    private static double norm(Map<String, Integer> tf) {
        double sum = 0.0;
        for (int count : tf.values()) {
            sum += (double) count * count;
        }
        return Math.sqrt(sum);
    }

    private static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String[] raw = text.toLowerCase().split("[^\\p{L}\\p{Nd}]+");
        List<String> tokens = new java.util.ArrayList<>(raw.length);
        for (String token : raw) {
            if (token.length() >= 3) {
                tokens.add(token);
            }
        }
        return tokens;
    }
}
