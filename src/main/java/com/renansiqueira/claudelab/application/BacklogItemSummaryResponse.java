package com.renansiqueira.claudelab.application;

import com.renansiqueira.claudelab.domain.BacklogType;
import com.renansiqueira.claudelab.domain.Priority;
import java.util.UUID;

/**
 * Compact view of a backlog item, used when listing a project's backlog.
 */
public record BacklogItemSummaryResponse(
        UUID id,
        BacklogType type,
        String title,
        Priority priority
) {
}
