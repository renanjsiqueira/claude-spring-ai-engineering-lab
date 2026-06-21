package com.renansiqueira.claudelab.ai;

import com.renansiqueira.claudelab.ai.prompt.BacklogPromptTemplate;
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
 *
 * <p>The system prompt is the versioned {@link BacklogPromptTemplate}.
 */
@Service
public class BacklogAnalysisService {

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
                .system(BacklogPromptTemplate.SYSTEM)
                .user(input)
                .options(AnthropicChatOptions.builder()
                        .temperature(0.2)
                        .build())
                .call()
                .entity(BacklogAnalysisResponse.class);
    }
}
