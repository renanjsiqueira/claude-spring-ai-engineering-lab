package com.renansiqueira.claudelab.workflow;

import com.renansiqueira.claudelab.domain.BacklogType;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Routing workflow: classify the input, then dispatch to a prompt specialized for
 * that type (feature, bug, architecture or refactor).
 */
@Component
public class RoutingWorkflow implements EngineeringWorkflow {

    private static final String CLASSIFY_SYSTEM =
            "Classify the request as exactly one of: FEATURE, BUG, REFACTOR, ARCHITECTURE. "
                    + "Reply with only the single word.";

    private static final String FEATURE_SYSTEM =
            "You are a product-minded engineer. Analyze this FEATURE request: value, scope, user story "
                    + "and testable acceptance criteria.";
    private static final String BUG_SYSTEM =
            "You are a debugging specialist. Analyze this BUG: likely root cause, reproduction, impact, "
                    + "and the fix with regression tests.";
    private static final String ARCHITECTURE_SYSTEM =
            "You are a principal architect. Analyze this ARCHITECTURE concern: options, trade-offs, "
                    + "risks, failure modes and a clear recommendation.";
    private static final String REFACTOR_SYSTEM =
            "You are a refactoring expert. Analyze this REFACTOR: target design, safe incremental steps, "
                    + "and how to preserve behavior with tests.";

    private final LlmStep step;

    public RoutingWorkflow(LlmStep step) {
        this.step = step;
    }

    @Override
    public WorkflowType type() {
        return WorkflowType.ROUTING;
    }

    @Override
    public WorkflowResult run(String input) {
        String classification = step.complete(CLASSIFY_SYSTEM, input);
        BacklogType routed = parse(classification);
        String analysis = step.complete(systemFor(routed), input);

        List<WorkflowStep> steps = List.of(
                new WorkflowStep("classify", routed.name()),
                new WorkflowStep("analyze-" + routed.name().toLowerCase(), analysis));
        return new WorkflowResult(WorkflowType.ROUTING, analysis, steps);
    }

    private static BacklogType parse(String classification) {
        String text = classification == null ? "" : classification.toUpperCase();
        if (text.contains("BUG")) {
            return BacklogType.BUG;
        }
        if (text.contains("ARCHITECT")) {
            return BacklogType.ARCHITECTURE;
        }
        if (text.contains("REFACTOR")) {
            return BacklogType.REFACTOR;
        }
        return BacklogType.FEATURE;
    }

    private static String systemFor(BacklogType type) {
        return switch (type) {
            case BUG -> BUG_SYSTEM;
            case ARCHITECTURE -> ARCHITECTURE_SYSTEM;
            case REFACTOR -> REFACTOR_SYSTEM;
            default -> FEATURE_SYSTEM;
        };
    }
}
