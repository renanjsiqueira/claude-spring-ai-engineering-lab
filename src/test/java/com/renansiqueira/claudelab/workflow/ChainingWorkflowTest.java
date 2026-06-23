package com.renansiqueira.claudelab.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

class ChainingWorkflowTest {

    @Test
    void runsStepsInOrderAndReturnsReviewAsFinal() {
        LlmStep step = mock(LlmStep.class);
        when(step.complete(anyString(), anyString()))
                .thenReturn("FEATURE", "backlog draft", "criteria", "final review");

        WorkflowResult result = new ChainingWorkflow(step).run("Import CSV");

        assertThat(result.type()).isEqualTo(WorkflowType.CHAINING);
        assertThat(result.finalAnalysis()).isEqualTo("final review");
        assertThat(result.steps()).extracting(WorkflowStep::name)
                .containsExactly("classify", "backlog-item", "acceptance-criteria", "review");
        assertThat(result.steps().get(0).output()).isEqualTo("FEATURE");
    }
}
