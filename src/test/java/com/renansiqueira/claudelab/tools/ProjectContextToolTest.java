package com.renansiqueira.claudelab.tools;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProjectContextToolTest {

    private final ProjectContextTool tool = new ProjectContextTool();

    @Test
    void returnsContextForKnownProject() {
        ProjectContext context = tool.getProjectContext("brabrix-dev");

        assertThat(context.projectId()).isEqualTo("brabrix-dev");
        assertThat(context.name()).isEqualTo("Brabrix");
        assertThat(context.stack()).contains("Java 21", "Spring AI");
    }

    @Test
    void returnsFallbackForUnknownProject() {
        ProjectContext context = tool.getProjectContext("does-not-exist");

        assertThat(context.projectId()).isEqualTo("does-not-exist");
        assertThat(context.name()).isEqualTo("Unknown project");
        assertThat(context.stack()).isEmpty();
    }
}
