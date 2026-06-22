package com.renansiqueira.claudelab.tools;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Tool that exposes basic context about a project so Claude can ground backlog
 * items in the project's reality (name, description, tech stack).
 *
 * <p>Backed by an in-memory registry for now.
 */
@Component
public class ProjectContextTool {

    private static final Logger log = LoggerFactory.getLogger(ProjectContextTool.class);

    private final Map<String, ProjectContext> projects = Map.of(
            "brabrix-dev", new ProjectContext(
                    "brabrix-dev",
                    "Brabrix",
                    "SaaS platform for managing customers, transactions and billing.",
                    List.of("Java 21", "Spring Boot", "Spring AI", "PostgreSQL")));

    @Tool(description = "Get the context of a project: its name, description and technology stack. "
            + "Call this before creating a backlog item so the item fits the project.")
    public ProjectContext getProjectContext(
            @ToolParam(description = "The project identifier, e.g. 'brabrix-dev'") String projectId) {
        log.info("Tool getProjectContext called for projectId={}", projectId);
        return projects.getOrDefault(projectId, new ProjectContext(
                projectId, "Unknown project", "No context is registered for this project.", List.of()));
    }
}
