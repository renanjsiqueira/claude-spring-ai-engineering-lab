package com.renansiqueira.claudelab.tools;

import com.renansiqueira.claudelab.application.BacklogItemResponse;
import com.renansiqueira.claudelab.application.BacklogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Tool that creates and persists backlog items in PostgreSQL via
 * {@link BacklogService}. Returns a lightweight {@link BacklogItem} record (not
 * the JPA entity) so the tool result fed back to Claude stays small.
 */
@Component
public class BacklogTool {

    private static final Logger log = LoggerFactory.getLogger(BacklogTool.class);

    private final BacklogService backlogService;

    public BacklogTool(BacklogService backlogService) {
        this.backlogService = backlogService;
    }

    @Tool(description = "Create and persist a backlog item for a project. "
            + "Returns the created item including its generated id.")
    public BacklogItem createBacklogItem(
            @ToolParam(description = "Identifier of the project the item belongs to") String projectId,
            @ToolParam(description = "Short, descriptive title of the backlog item") String title,
            @ToolParam(description = "Detailed description of what needs to be done") String description) {
        BacklogItemResponse saved = backlogService.createItem(projectId, title, description);
        log.info("Tool createBacklogItem persisted id={} projectId={} title='{}'",
                saved.id(), projectId, title);
        return new BacklogItem(saved.id().toString(), saved.projectId(), saved.title(), description);
    }
}
