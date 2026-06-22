package com.renansiqueira.claudelab.tools;

import java.util.List;

/**
 * Context about a project, returned by {@link ProjectContextTool}.
 *
 * @param projectId   the project identifier
 * @param name        human-readable project name
 * @param description short description of the project
 * @param stack       the project's technology stack
 */
public record ProjectContext(
        String projectId,
        String name,
        String description,
        List<String> stack
) {
}
