package com.renansiqueira.claudelab.eval;

import static org.assertj.core.api.Assertions.assertThat;

import com.renansiqueira.claudelab.domain.BacklogAnalysisResponse;
import com.renansiqueira.claudelab.domain.BacklogType;
import com.renansiqueira.claudelab.domain.Priority;
import java.util.List;
import org.junit.jupiter.api.Test;

class PromptEvaluationServiceTest {

    private final PromptEvaluationService service = new PromptEvaluationService();

    private static BacklogAnalysisResponse response(BacklogType type, String title,
                                                    List<String> criteria, List<String> tasks) {
        return new BacklogAnalysisResponse(type, title, "summary", Priority.MEDIUM, "As a user...",
                criteria, tasks, List.of(), List.of());
    }

    private static DatasetItem item(BacklogType type, int minCriteria, int minTasks,
                                    List<String> forbidden) {
        return new DatasetItem("some input", type, minCriteria, minTasks, forbidden);
    }

    @Test
    void passesWhenAllChecksHold() {
        EvaluationScore score = service.grade(
                item(BacklogType.FEATURE, 2, 2, List.of("blockchain")),
                response(BacklogType.FEATURE, "A title",
                        List.of("AC1", "AC2"), List.of("T1", "T2")));

        assertThat(score.passed()).isTrue();
        assertThat(score.failures()).isEmpty();
    }

    @Test
    void failsOnNullResponse() {
        EvaluationScore score = service.grade(item(BacklogType.FEATURE, 1, 1, null), null);

        assertThat(score.passed()).isFalse();
        assertThat(score.validJson()).isFalse();
        assertThat(score.actualType()).isNull();
        assertThat(score.failures()).anyMatch(f -> f.contains("valid structured JSON"));
    }

    @Test
    void failsOnWrongType() {
        EvaluationScore score = service.grade(
                item(BacklogType.BUG, 1, 1, null),
                response(BacklogType.FEATURE, "T", List.of("AC1"), List.of("T1")));

        assertThat(score.correctType()).isFalse();
        assertThat(score.passed()).isFalse();
        assertThat(score.failures()).anyMatch(f -> f.contains("expected type BUG"));
    }

    @Test
    void failsOnMissingTitle() {
        EvaluationScore score = service.grade(
                item(BacklogType.FEATURE, 1, 1, null),
                response(BacklogType.FEATURE, "  ", List.of("AC1"), List.of("T1")));

        assertThat(score.hasTitle()).isFalse();
        assertThat(score.passed()).isFalse();
    }

    @Test
    void failsOnTooFewCriteriaAndTasks() {
        EvaluationScore score = service.grade(
                item(BacklogType.FEATURE, 3, 3, null),
                response(BacklogType.FEATURE, "T", List.of("AC1"), List.of("T1")));

        assertThat(score.meetsCriteriaCount()).isFalse();
        assertThat(score.meetsTasksCount()).isFalse();
        assertThat(score.passed()).isFalse();
    }

    @Test
    void failsOnForbiddenTerm() {
        EvaluationScore score = service.grade(
                item(BacklogType.FEATURE, 1, 1, List.of("blockchain")),
                response(BacklogType.FEATURE, "Use Blockchain here",
                        List.of("AC1"), List.of("T1")));

        assertThat(score.noForbiddenTerms()).isFalse();
        assertThat(score.passed()).isFalse();
        assertThat(score.failures()).anyMatch(f -> f.contains("forbidden term: blockchain"));
    }
}
