package com.renansiqueira.claudelab.workflow;

import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Sequential workflow: classify the input, draft a backlog item, expand its
 * acceptance criteria, then review the result. Each step's output feeds the next.
 */
@Component
public class ChainingWorkflow implements EngineeringWorkflow {

    private static final String CLASSIFY_SYSTEM =
            "Classify the request as exactly one of: FEATURE, BUG, REFACTOR, ARCHITECTURE, QUESTION. "
                    + "Reply with only the single word.";
    private static final String BACKLOG_SYSTEM =
            "You are a senior software engineer. Draft a backlog item (title, summary, user story) for "
                    + "the request. Do not invent business rules; note assumptions if context is missing.";
    private static final String CRITERIA_SYSTEM =
            "Write specific, testable acceptance criteria for this backlog item, in Given/When/Then form.";
    private static final String REVIEW_SYSTEM =
            "Review the backlog item and its acceptance criteria for clarity, completeness and "
                    + "testability. Return the improved, final version.";

    private final LlmStep step;

    public ChainingWorkflow(LlmStep step) {
        this.step = step;
    }

    @Override
    public WorkflowType type() {
        return WorkflowType.CHAINING;
    }

    @Override
    public WorkflowResult run(String input) {
        String classification = step.complete(CLASSIFY_SYSTEM, input).strip();
        String item = step.complete(BACKLOG_SYSTEM,
                "Classification: " + classification + "\n\nRequest:\n" + input);
        String criteria = step.complete(CRITERIA_SYSTEM, item);
        String review = step.complete(REVIEW_SYSTEM,
                "Backlog item:\n" + item + "\n\nAcceptance criteria:\n" + criteria);

        List<WorkflowStep> steps = List.of(
                new WorkflowStep("classify", classification),
                new WorkflowStep("backlog-item", item),
                new WorkflowStep("acceptance-criteria", criteria),
                new WorkflowStep("review", review));
        return new WorkflowResult(WorkflowType.CHAINING, review, steps);
    }
}
