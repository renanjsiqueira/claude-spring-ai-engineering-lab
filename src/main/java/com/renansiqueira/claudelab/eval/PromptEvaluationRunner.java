package com.renansiqueira.claudelab.eval;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renansiqueira.claudelab.ai.BacklogAnalysisService;
import com.renansiqueira.claudelab.domain.BacklogAnalysisResponse;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Runs the full evaluation suite: loads every dataset, asks Claude to analyze each
 * case via {@link BacklogAnalysisService}, grades the output with
 * {@link PromptEvaluationService}, and writes a timestamped JSON report.
 *
 * <p>This is the only part of the eval workflow that actually calls Claude, so it
 * is exercised manually (CLI/endpoint) rather than in the unit test suite.
 */
@Service
public class PromptEvaluationRunner {

    private static final Logger log = LoggerFactory.getLogger(PromptEvaluationRunner.class);
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final BacklogAnalysisService analysisService;
    private final PromptEvaluationService evaluationService;
    private final ObjectMapper objectMapper;
    private final Path datasetsDir;
    private final Path resultsDir;

    public PromptEvaluationRunner(
            BacklogAnalysisService analysisService,
            PromptEvaluationService evaluationService,
            ObjectMapper objectMapper,
            @Value("${claudelab.evals.datasets-dir:evals/datasets}") String datasetsDir,
            @Value("${claudelab.evals.results-dir:evals/results}") String resultsDir) {
        this.analysisService = analysisService;
        this.evaluationService = evaluationService;
        this.objectMapper = objectMapper;
        this.datasetsDir = Path.of(datasetsDir);
        this.resultsDir = Path.of(resultsDir);
    }

    /**
     * Loads all datasets, evaluates every case and persists the report.
     *
     * @return the aggregate result
     */
    public EvaluationResult run() {
        List<DatasetItem> items = loadDatasets();
        List<EvaluationScore> scores = new ArrayList<>();

        for (DatasetItem item : items) {
            BacklogAnalysisResponse response;
            try {
                response = analysisService.analyze(item.input());
            } catch (Exception ex) {
                log.warn("Analysis failed for input '{}': {}", item.input(), ex.getMessage());
                response = null;
            }
            scores.add(evaluationService.grade(item, response));
        }

        int passed = (int) scores.stream().filter(EvaluationScore::passed).count();
        EvaluationResult result = new EvaluationResult(scores.size(), passed, scores.size() - passed, scores);

        writeReport(result);
        return result;
    }

    private List<DatasetItem> loadDatasets() {
        if (!Files.isDirectory(datasetsDir)) {
            log.warn("Datasets directory '{}' not found — no cases to evaluate", datasetsDir);
            return List.of();
        }
        try (Stream<Path> files = Files.list(datasetsDir)) {
            List<DatasetItem> items = new ArrayList<>();
            files.filter(p -> p.toString().endsWith(".json"))
                    .sorted()
                    .forEach(file -> {
                        try {
                            DatasetItem[] parsed = objectMapper.readValue(file.toFile(), DatasetItem[].class);
                            items.addAll(List.of(parsed));
                        } catch (IOException ex) {
                            throw new UncheckedIOException("Failed to read dataset " + file, ex);
                        }
                    });
            return items;
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to list datasets in " + datasetsDir, ex);
        }
    }

    private void writeReport(EvaluationResult result) {
        try {
            Files.createDirectories(resultsDir);
            Path report = resultsDir.resolve("eval-result-" + LocalDateTime.now().format(TIMESTAMP) + ".json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(report.toFile(), result);
            log.info("Evaluation report written to {}", report);
        } catch (IOException ex) {
            // Reporting is best-effort — a write failure must not fail the run.
            log.warn("Could not write evaluation report: {}", ex.getMessage());
        }
    }
}
