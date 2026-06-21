package com.renansiqueira.claudelab.eval;

import com.renansiqueira.claudelab.domain.BacklogAnalysisResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Code-based grading for backlog analysis output.
 *
 * <p>Pure, deterministic checks — no LLM involved — so the grading logic itself
 * is fully unit-testable. Each {@link DatasetItem} is graded against the
 * {@link BacklogAnalysisResponse} the analyzer produced (or {@code null} if the
 * call failed / returned invalid JSON).
 */
@Service
public class PromptEvaluationService {

    /**
     * Grades a single response against its expected case.
     *
     * @param item     the expected case
     * @param response the produced analysis, or {@code null} if it was invalid/failed
     * @return the per-case score
     */
    public EvaluationScore grade(DatasetItem item, BacklogAnalysisResponse response) {
        List<String> failures = new ArrayList<>();

        boolean validJson = response != null;
        if (!validJson) {
            failures.add("response was not valid structured JSON");
        }

        boolean correctType = validJson && response.type() == item.expectedType();
        if (validJson && !correctType) {
            failures.add("expected type " + item.expectedType() + " but got " + response.type());
        }

        boolean hasTitle = validJson && response.title() != null && !response.title().isBlank();
        if (validJson && !hasTitle) {
            failures.add("title is missing");
        }

        int criteriaCount = validJson ? size(response.acceptanceCriteria()) : 0;
        boolean meetsCriteriaCount = criteriaCount >= item.expectedMinimumCriteriaCount();
        if (validJson && !meetsCriteriaCount) {
            failures.add("acceptanceCriteria count " + criteriaCount
                    + " is below minimum " + item.expectedMinimumCriteriaCount());
        }

        int tasksCount = validJson ? size(response.technicalTasks()) : 0;
        boolean meetsTasksCount = tasksCount >= item.expectedMinimumTasksCount();
        if (validJson && !meetsTasksCount) {
            failures.add("technicalTasks count " + tasksCount
                    + " is below minimum " + item.expectedMinimumTasksCount());
        }

        boolean noForbiddenTerms = true;
        if (validJson && item.forbiddenTerms() != null && !item.forbiddenTerms().isEmpty()) {
            String haystack = allText(response);
            for (String term : item.forbiddenTerms()) {
                if (term != null && !term.isBlank()
                        && haystack.contains(term.toLowerCase())) {
                    noForbiddenTerms = false;
                    failures.add("output contains forbidden term: " + term);
                }
            }
        }

        boolean passed = validJson && correctType && hasTitle
                && meetsCriteriaCount && meetsTasksCount && noForbiddenTerms;

        return new EvaluationScore(
                item.input(),
                item.expectedType(),
                validJson ? response.type() : null,
                validJson,
                correctType,
                hasTitle,
                meetsCriteriaCount,
                meetsTasksCount,
                noForbiddenTerms,
                passed,
                List.copyOf(failures));
    }

    private static int size(List<?> list) {
        return list == null ? 0 : list.size();
    }

    private static String allText(BacklogAnalysisResponse r) {
        StringBuilder sb = new StringBuilder();
        append(sb, r.title());
        append(sb, r.summary());
        append(sb, r.userStory());
        appendAll(sb, r.acceptanceCriteria());
        appendAll(sb, r.technicalTasks());
        appendAll(sb, r.risks());
        appendAll(sb, r.assumptions());
        return sb.toString().toLowerCase();
    }

    private static void append(StringBuilder sb, String value) {
        if (value != null) {
            sb.append(value).append('\n');
        }
    }

    private static void appendAll(StringBuilder sb, List<String> values) {
        if (values != null) {
            values.forEach(v -> append(sb, v));
        }
    }
}
