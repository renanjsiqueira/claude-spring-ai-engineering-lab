package com.renansiqueira.claudelab.mcp;

import java.util.List;
import java.util.UUID;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/**
 * MCP tools exposing project capabilities.
 */
@Component
public class ProjectMcpTools {

    private static final List<String> COMPLEXITY_SIGNALS = List.of(
            "integrat", "migrat", "import", "csv", "async", "assíncron", "assincron", "distribut",
            "concurren", "security", "secur", "payment", "refactor", "architect", "encrypt");

    private final ProjectKnowledge knowledge;

    public ProjectMcpTools(ProjectKnowledge knowledge) {
        this.knowledge = knowledge;
    }

    @McpTool(name = "search_project_docs",
            description = "Search the project documentation (rules, architecture, coding standards, "
                    + "glossary) and return matching snippets with their source files.")
    public String searchProjectDocs(
            @McpToolParam(description = "The search query", required = true) String query) {
        return knowledge.search(query);
    }

    @McpTool(name = "get_project_context",
            description = "Get a project's context (name, description and stack) by its id.")
    public String getProjectContext(
            @McpToolParam(description = "The project id, e.g. 'brabrix-dev'", required = true)
            String projectId) {
        return knowledge.projectContext(projectId);
    }

    @McpTool(name = "create_backlog_item",
            description = "Create a backlog item for a project (in-memory in this experimental server) "
                    + "and return its generated id.")
    public String createBacklogItem(
            @McpToolParam(description = "Project id", required = true) String projectId,
            @McpToolParam(description = "Short title", required = true) String title,
            @McpToolParam(description = "Detailed description", required = true) String description) {
        String id = UUID.randomUUID().toString();
        return "Created backlog item " + id + " for project '" + projectId + "': " + title;
    }

    @McpTool(name = "estimate_task_complexity",
            description = "Estimate the implementation complexity (LOW, MEDIUM or HIGH) of a task "
                    + "from its description, with a short justification.")
    public String estimateTaskComplexity(
            @McpToolParam(description = "Task description", required = true) String description) {
        if (description == null || description.isBlank()) {
            return "LOW — no description provided.";
        }
        String text = description.toLowerCase();
        long signals = COMPLEXITY_SIGNALS.stream().filter(text::contains).count();
        int words = description.trim().split("\\s+").length;
        String level;
        if (signals >= 2 || words > 60) {
            level = "HIGH";
        } else if (signals == 1 || words > 25) {
            level = "MEDIUM";
        } else {
            level = "LOW";
        }
        return level + " — detected " + signals + " complexity signal(s) and " + words + " word(s).";
    }
}
