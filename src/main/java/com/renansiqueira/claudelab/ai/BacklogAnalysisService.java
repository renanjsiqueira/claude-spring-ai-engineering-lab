package com.renansiqueira.claudelab.ai;

import com.renansiqueira.claudelab.domain.BacklogAnalysisResponse;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Turns a raw idea, bug or feature request into a {@link BacklogAnalysisResponse}.
 *
 * <p>Uses Spring AI's structured output: {@code .entity(...)} appends the target
 * JSON schema to the prompt and parses Claude's reply straight into the record,
 * so the model is constrained to the contract instead of free text. A low
 * temperature keeps the analysis stable and deterministic.
 */
@Service
public class BacklogAnalysisService {

    static final String SYSTEM_PROMPT = """
            You are a senior software engineering assistant that converts a raw idea, bug report or
            feature request into a single structured backlog item.

            Follow these rules strictly:
            - Classify `type` as one of: FEATURE, BUG, REFACTOR, ARCHITECTURE, QUESTION.
            - Set `priority` (LOW, MEDIUM, HIGH, CRITICAL) from the impact and urgency implied by the input.
            - Do NOT invent business rules, numbers or constraints the input does not state.
            - When the input is vague or missing context, do NOT guess: record what is unknown in
              `assumptions` and the resulting `risks`.
            - `acceptanceCriteria` must be specific and testable (each verifiable as pass/fail);
              prefer Given/When/Then phrasing.
            - `technicalTasks` are concrete, implementation-level steps.
            - `userStory` uses the form: "As a <role>, I want <goal>, so that <benefit>".
            - Keep `title` and `summary` concise.
            - Respond ONLY with the structured data defined by the schema. No commentary, no extra text.
            """;

    private final ChatClient chatClient;

    public BacklogAnalysisService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Analyzes the given input and returns a structured backlog item.
     *
     * @param input a raw idea, bug report or feature request
     * @return the structured analysis
     */
    public BacklogAnalysisResponse analyze(String input) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(input)
                .options(AnthropicChatOptions.builder()
                        .temperature(0.2)
                        .build())
                .call()
                .entity(BacklogAnalysisResponse.class);
    }
}
