package com.renansiqueira.claudelab.eval;

import com.renansiqueira.claudelab.domain.BacklogType;
import java.util.List;

/**
 * Code-based grading result for a single evaluation case.
 *
 * @param input              the case input (for reporting)
 * @param expectedType       the expected backlog type
 * @param actualType         the type the model returned, or {@code null} if the output was invalid
 * @param validJson          whether a valid structured response was produced
 * @param correctType        whether the type matched
 * @param hasTitle           whether a non-blank title was present
 * @param meetsCriteriaCount whether the minimum acceptance-criteria count was met
 * @param meetsTasksCount    whether the minimum technical-tasks count was met
 * @param noForbiddenTerms   whether the output avoided all forbidden terms
 * @param passed             true only if every check passed
 * @param failures           human-readable descriptions of each failed check
 */
public record EvaluationScore(
        String input,
        BacklogType expectedType,
        BacklogType actualType,
        boolean validJson,
        boolean correctType,
        boolean hasTitle,
        boolean meetsCriteriaCount,
        boolean meetsTasksCount,
        boolean noForbiddenTerms,
        boolean passed,
        List<String> failures
) {
}
