package com.renansiqueira.claudelab.mcp;

import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;

/**
 * MCP resources exposing project context and selected documentation.
 */
@Component
public class ProjectMcpResources {

    private final ProjectKnowledge knowledge;

    public ProjectMcpResources(ProjectKnowledge knowledge) {
        this.knowledge = knowledge;
    }

    @McpResource(uri = "project://brabrix-dev/context",
            name = "Brabrix project context",
            description = "Name, description and technology stack of the brabrix-dev project.")
    public String brabrixContext() {
        return knowledge.projectContext("brabrix-dev");
    }

    @McpResource(uri = "docs://architecture-guidelines",
            name = "Architecture guidelines",
            description = "The project's architecture guidelines.")
    public String architectureGuidelines() {
        return knowledge.document("architecture-guidelines.md");
    }

    @McpResource(uri = "docs://coding-standards",
            name = "Coding standards",
            description = "The project's coding standards.")
    public String codingStandards() {
        return knowledge.document("coding-standards.md");
    }
}
