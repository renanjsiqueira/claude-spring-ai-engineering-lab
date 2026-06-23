package com.renansiqueira.claudelab.workflow;

/**
 * A single LLM call: a system prompt + a user prompt in, the text answer out.
 *
 * <p>Workflows depend on this abstraction (not on {@code ChatClient} directly) so
 * their orchestration logic can be unit-tested by mocking the step.
 */
public interface LlmStep {

    String complete(String systemPrompt, String userPrompt);
}
