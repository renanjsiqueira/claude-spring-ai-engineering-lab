package com.renansiqueira.claudelab.workflow;

import java.util.List;

/**
 * The result of running a workflow.
 *
 * @param type          which workflow ran
 * @param finalAnalysis the final, user-facing analysis
 * @param steps         the intermediate steps, in order
 */
public record WorkflowResult(WorkflowType type, String finalAnalysis, List<WorkflowStep> steps) {
}
