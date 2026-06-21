package com.renansiqueira.claudelab.ai.prompt;

/**
 * Single entry point to the versioned Claude prompt templates.
 *
 * <p>Aggregates the individual template classes so callers have one place to
 * discover the available prompts.
 *
 * @see BacklogPromptTemplate
 * @see ArchitecturePromptTemplate
 */
public final class ClaudePromptTemplates {

    private ClaudePromptTemplates() {
    }

    /** System prompt for structured backlog analysis. */
    public static final String BACKLOG_ANALYSIS = BacklogPromptTemplate.SYSTEM;

    /** System prompt for architecture analysis. */
    public static final String ARCHITECTURE_ANALYSIS = ArchitecturePromptTemplate.SYSTEM;
}
