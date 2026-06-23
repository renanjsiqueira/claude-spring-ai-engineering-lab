package com.renansiqueira.claudelab.workflow;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * Default {@link LlmStep} backed by Spring AI's {@link ChatClient} (Claude).
 */
@Component
public class ChatClientLlmStep implements LlmStep {

    private final ChatClient chatClient;

    public ChatClientLlmStep(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }
}
