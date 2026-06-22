package com.renansiqueira.claudelab.application;

import com.renansiqueira.claudelab.domain.BacklogType;
import com.renansiqueira.claudelab.domain.Priority;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Full view of a persisted backlog item, including its criteria and tasks.
 */
public record BacklogItemResponse(
        UUID id,
        String projectId,
        BacklogType type,
        String title,
        String summary,
        Priority priority,
        String userStory,
        List<String> acceptanceCriteria,
        List<String> technicalTasks,
        Instant createdAt
) {
}
