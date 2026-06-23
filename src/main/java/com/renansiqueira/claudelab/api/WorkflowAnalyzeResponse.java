package com.renansiqueira.claudelab.api;

import com.renansiqueira.claudelab.workflow.WorkflowStep;
import java.util.List;

/**
 * Response body for {@code POST /api/workflows/analyze}.
 *
 * @param workflowType  which workflow ran
 * @param finalAnalysis the final analysis text
 * @param steps         the intermediate steps, in order
 */
public record WorkflowAnalyzeResponse(
        String workflowType,
        String finalAnalysis,
        List<WorkflowStep> steps
) {
}
