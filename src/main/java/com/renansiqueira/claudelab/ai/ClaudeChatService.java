package com.renansiqueira.claudelab.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Thin service around Spring AI's {@link ChatClient} for sending a single
 * message to Claude and getting the text answer back.
 *
 * <p>This is the simplest possible Claude integration: one user message in,
 * one string out. Later phases build on top of it (multi-turn, system prompts,
 * streaming, structured output, etc.).
 */
@Service
public class ClaudeChatService {

    private final ChatClient chatClient;

    public ClaudeChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Sends a single user message to Claude and returns its text response.
     *
     * @param message the user message; must not be blank
     * @return Claude's text answer
     */
    public String sendMessage(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
