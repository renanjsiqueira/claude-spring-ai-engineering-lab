package com.renansiqueira.claudelab.ai.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Guards the structure of the versioned backlog prompt: XML tags must be present
 * and at least two few-shot examples (a feature and a bug) must be included.
 */
class BacklogPromptTemplateTest {

    @Test
    void usesXmlTags() {
        assertThat(BacklogPromptTemplate.SYSTEM)
                .contains("<role>")
                .contains("</role>")
                .contains("<context>")
                .contains("<task>")
                .contains("<rules>")
                .contains("<examples>")
                .contains("</examples>");
    }

    @Test
    void includesAtLeastTwoFewShotExamples() {
        assertThat(BacklogPromptTemplate.SYSTEM)
                .contains("Example 1")
                .contains("Example 2")
                .contains("Feature request")
                .contains("Bug report");
    }

    @Test
    void aggregatorExposesTemplates() {
        assertThat(ClaudePromptTemplates.BACKLOG_ANALYSIS).isEqualTo(BacklogPromptTemplate.SYSTEM);
        assertThat(ClaudePromptTemplates.ARCHITECTURE_ANALYSIS)
                .isEqualTo(ArchitecturePromptTemplate.SYSTEM)
                .contains("<role>");
    }
}
