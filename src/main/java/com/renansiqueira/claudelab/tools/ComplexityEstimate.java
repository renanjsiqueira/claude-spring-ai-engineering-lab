package com.renansiqueira.claudelab.tools;

/**
 * Result of {@link ComplexityTool}: a complexity level plus a short justification.
 *
 * @param level         the estimated complexity
 * @param justification why this level was chosen
 */
public record ComplexityEstimate(
        ComplexityLevel level,
        String justification
) {
}
