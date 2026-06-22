package com.renansiqueira.claudelab.tools;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Tool that estimates implementation complexity from a task description.
 *
 * <p>Uses a small, deterministic heuristic (keyword signals + length) so the
 * behavior is predictable and unit-testable. This is a placeholder for a more
 * sophisticated estimator.
 */
@Component
public class ComplexityTool {

    private static final Logger log = LoggerFactory.getLogger(ComplexityTool.class);

    /** Stems that hint at higher implementation complexity (English + Portuguese). */
    private static final List<String> COMPLEXITY_SIGNALS = List.of(
            "integrat", "migrat", "import", "csv", "async", "distribut", "concurren",
            "security", "secur", "payment", "refactor", "architect", "scalab", "encrypt",
            "real-time", "integra", "migrar", "importar", "pagamento", "seguran");

    @Tool(description = "Estimate the implementation complexity (LOW, MEDIUM or HIGH) of a task "
            + "from its description, with a short justification.")
    public ComplexityEstimate estimateComplexity(
            @ToolParam(description = "Description of the task to estimate") String description) {
        ComplexityEstimate estimate = compute(description);
        log.info("Tool estimateComplexity called -> {}", estimate.level());
        return estimate;
    }

    private ComplexityEstimate compute(String description) {
        if (description == null || description.isBlank()) {
            return new ComplexityEstimate(ComplexityLevel.LOW,
                    "No description provided; defaulting to LOW.");
        }

        String text = description.toLowerCase();
        long signals = COMPLEXITY_SIGNALS.stream().filter(text::contains).count();
        int words = description.trim().split("\\s+").length;

        ComplexityLevel level;
        if (signals >= 2 || words > 60) {
            level = ComplexityLevel.HIGH;
        } else if (signals == 1 || words > 25) {
            level = ComplexityLevel.MEDIUM;
        } else {
            level = ComplexityLevel.LOW;
        }

        String justification = "Detected %d complexity signal(s) and %d word(s)."
                .formatted(signals, words);
        return new ComplexityEstimate(level, justification);
    }
}
