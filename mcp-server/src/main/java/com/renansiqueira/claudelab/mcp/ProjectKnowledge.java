package com.renansiqueira.claudelab.mcp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Shared, read-only access to the lab's knowledge base and project context, used
 * by the MCP tools and resources. Self-contained so the MCP server does not
 * depend on the core application.
 */
@Component
public class ProjectKnowledge {

    private static final Map<String, String> PROJECTS = Map.of(
            "devbacklog-ai-assistant",
            "DevBacklog AI Assistant — Turns ideas, bugs and feature requests into structured engineering backlog items. "
                    + "Stack: Java 21, Spring Boot, Spring AI, PostgreSQL.");

    private final Path knowledgeBaseDir;

    public ProjectKnowledge(
            @Value("${claudelab.mcp.knowledge-base-dir:../docs/knowledge-base}") String knowledgeBaseDir) {
        this.knowledgeBaseDir = Path.of(knowledgeBaseDir);
    }

    public String projectContext(String projectId) {
        return PROJECTS.getOrDefault(projectId,
                "No context is registered for project '" + projectId + "'.");
    }

    public String document(String fileName) {
        Path file = knowledgeBaseDir.resolve(fileName);
        if (!Files.isRegularFile(file)) {
            return "Document '" + fileName + "' not found.";
        }
        try {
            return Files.readString(file);
        } catch (IOException ex) {
            return "Could not read '" + fileName + "': " + ex.getMessage();
        }
    }

    /**
     * Naive keyword search across the knowledge base; returns matching file names
     * with a short snippet, or a "not found" message.
     */
    public String search(String query) {
        if (query == null || query.isBlank() || !Files.isDirectory(knowledgeBaseDir)) {
            return "No relevant documentation found.";
        }
        List<String> terms = List.of(query.toLowerCase().split("[^\\p{L}\\p{Nd}]+"));
        StringBuilder result = new StringBuilder();
        try (Stream<Path> files = Files.list(knowledgeBaseDir)) {
            files.filter(p -> p.toString().endsWith(".md")).sorted().forEach(file -> {
                String content = silentRead(file);
                String lower = content.toLowerCase();
                if (terms.stream().anyMatch(t -> t.length() >= 3 && lower.contains(t))) {
                    String snippet = content.strip();
                    snippet = snippet.substring(0, Math.min(280, snippet.length()));
                    result.append("## ").append(file.getFileName()).append('\n')
                            .append(snippet).append("\n\n");
                }
            });
        } catch (IOException ex) {
            return "Search failed: " + ex.getMessage();
        }
        return result.isEmpty() ? "No relevant documentation found." : result.toString().strip();
    }

    private String silentRead(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException ex) {
            return "";
        }
    }
}
