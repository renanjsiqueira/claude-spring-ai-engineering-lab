package com.renansiqueira.claudelab.eval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renansiqueira.claudelab.ai.BacklogAnalysisService;
import com.renansiqueira.claudelab.domain.BacklogAnalysisResponse;
import com.renansiqueira.claudelab.domain.BacklogType;
import com.renansiqueira.claudelab.domain.Priority;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PromptEvaluationRunnerTest {

    private static BacklogAnalysisResponse response(BacklogType type) {
        return new BacklogAnalysisResponse(type, "Title", "summary", Priority.MEDIUM, "As a user...",
                List.of("AC1", "AC2"), List.of("T1", "T2"), List.of(), List.of());
    }

    @Test
    void runsDatasetsAndAggregatesPassedAndFailed(@TempDir Path tempDir) throws Exception {
        Path datasets = Files.createDirectories(tempDir.resolve("datasets"));
        Path results = tempDir.resolve("results");
        Files.writeString(datasets.resolve("cases.json"), """
                [
                  {"input": "feature one", "expectedType": "FEATURE",
                   "expectedMinimumCriteriaCount": 2, "expectedMinimumTasksCount": 2},
                  {"input": "bug two", "expectedType": "BUG",
                   "expectedMinimumCriteriaCount": 2, "expectedMinimumTasksCount": 2}
                ]
                """);

        BacklogAnalysisService analysisService = mock(BacklogAnalysisService.class);
        // First case gets a matching FEATURE (passes); second expects BUG but gets FEATURE (fails).
        when(analysisService.analyze(anyString()))
                .thenReturn(response(BacklogType.FEATURE))
                .thenReturn(response(BacklogType.FEATURE));

        PromptEvaluationRunner runner = new PromptEvaluationRunner(
                analysisService, new PromptEvaluationService(), new ObjectMapper(),
                datasets.toString(), results.toString());

        EvaluationResult result = runner.run();

        assertThat(result.total()).isEqualTo(2);
        assertThat(result.passed()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(1);
        assertThat(result.scores()).hasSize(2);

        // A report file was written.
        try (var files = Files.list(results)) {
            assertThat(files.anyMatch(p -> p.getFileName().toString().startsWith("eval-result-")))
                    .isTrue();
        }
    }

    @Test
    void missingDatasetsDirectoryYieldsEmptyResult(@TempDir Path tempDir) {
        PromptEvaluationRunner runner = new PromptEvaluationRunner(
                mock(BacklogAnalysisService.class), new PromptEvaluationService(), new ObjectMapper(),
                tempDir.resolve("does-not-exist").toString(), tempDir.resolve("results").toString());

        EvaluationResult result = runner.run();

        assertThat(result.total()).isZero();
        assertThat(result.scores()).isEmpty();
    }
}
