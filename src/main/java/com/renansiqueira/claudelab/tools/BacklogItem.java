package com.renansiqueira.claudelab.tools;

/**
 * A persisted backlog item, returned by {@link BacklogTool}.
 *
 * @param id          generated identifier
 * @param projectId   the project the item belongs to
 * @param title       short title
 * @param description detailed description
 */
public record BacklogItem(
        String id,
        String projectId,
        String title,
        String description
) {
}
