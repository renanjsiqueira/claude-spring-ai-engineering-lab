package com.renansiqueira.claudelab.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

class ParallelReviewWorkflowTest {

    @Test
    void runsFourAnalysesAndMerges() {
        LlmStep step = mock(LlmStep.class);
        // Key off the system prompt so the answer is independent of (parallel) call order.
        when(step.complete(anyString(), anyString())).thenAnswer(invocation -> {
            String system = invocation.<String>getArgument(0).toLowerCase();
            if (system.contains("merge")) {
                return "MERGED";
            }
            if (system.contains("product")) {
                return "P";
            }
            if (system.contains("technical")) {
                return "T";
            }
            if (system.contains("risk")) {
                return "R";
            }
            if (system.contains("qa") || system.contains("test")) {
                return "Q";
            }
            return "?";
        });

        WorkflowResult result = new ParallelReviewWorkflow(step).run("Import CSV with async processing");

        assertThat(result.type()).isEqualTo(WorkflowType.PARALLEL);
        assertThat(result.finalAnalysis()).isEqualTo("MERGED");
        assertThat(result.steps()).extracting(WorkflowStep::name)
                .containsExactly("product", "technical", "risks", "tests", "merge");
        assertThat(result.steps().get(2).output()).isEqualTo("R");
    }
}
