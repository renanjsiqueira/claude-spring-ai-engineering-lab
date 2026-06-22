package com.renansiqueira.claudelab.tools;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ComplexityToolTest {

    private final ComplexityTool tool = new ComplexityTool();

    @Test
    void highWhenMultipleSignals() {
        ComplexityEstimate estimate = tool.estimateComplexity(
                "Import customers via CSV with external integration and data migration.");

        assertThat(estimate.level()).isEqualTo(ComplexityLevel.HIGH);
        assertThat(estimate.justification()).isNotBlank();
    }

    @Test
    void mediumWhenSingleSignal() {
        ComplexityEstimate estimate = tool.estimateComplexity("Refactor the user service.");

        assertThat(estimate.level()).isEqualTo(ComplexityLevel.MEDIUM);
    }

    @Test
    void lowForSimpleShortTask() {
        ComplexityEstimate estimate = tool.estimateComplexity("Fix a typo in the footer.");

        assertThat(estimate.level()).isEqualTo(ComplexityLevel.LOW);
    }

    @Test
    void lowAndSafeForBlankDescription() {
        ComplexityEstimate estimate = tool.estimateComplexity("   ");

        assertThat(estimate.level()).isEqualTo(ComplexityLevel.LOW);
        assertThat(estimate.justification()).isNotBlank();
    }
}
