package com.renansiqueira.claudelab.eval;

import java.util.List;

/**
 * Aggregate result of an evaluation run.
 *
 * @param total  number of cases evaluated
 * @param passed number of cases that passed every check
 * @param failed number of cases with at least one failed check
 * @param scores per-case breakdown
 */
public record EvaluationResult(
        int total,
        int passed,
        int failed,
        List<EvaluationScore> scores
) {
}
