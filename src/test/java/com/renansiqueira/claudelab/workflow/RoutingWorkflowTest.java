package com.renansiqueira.claudelab.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RoutingWorkflowTest {

    @Test
    void routesBugToBugPrompt() {
        LlmStep step = mock(LlmStep.class);
        when(step.complete(anyString(), anyString()))
                .thenReturn("This is a BUG", "bug analysis");

        WorkflowResult result = new RoutingWorkflow(step).run("App crashes on expired token");

        assertThat(result.type()).isEqualTo(WorkflowType.ROUTING);
        assertThat(result.finalAnalysis()).isEqualTo("bug analysis");
        assertThat(result.steps().get(0).output()).isEqualTo("BUG");

        ArgumentCaptor<String> systems = ArgumentCaptor.forClass(String.class);
        verify(step, times(2)).complete(systems.capture(), anyString());
        // the second call uses the bug-specialized prompt
        assertThat(systems.getAllValues().get(1).toLowerCase()).contains("bug");
    }

    @Test
    void defaultsToFeatureWhenUnclear() {
        LlmStep step = mock(LlmStep.class);
        when(step.complete(anyString(), anyString()))
                .thenReturn("not sure", "feature analysis");

        WorkflowResult result = new RoutingWorkflow(step).run("something");

        assertThat(result.steps().get(0).output()).isEqualTo("FEATURE");
    }
}
