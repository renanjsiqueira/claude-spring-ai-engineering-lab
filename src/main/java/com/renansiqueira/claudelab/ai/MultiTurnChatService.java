package com.renansiqueira.claudelab.ai;

import java.util.List;
import java.util.UUID;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

/**
 * Orchestrates multi-turn conversations with Claude.
 *
 * <p>For each request it: resolves (or generates) a {@code conversationId},
 * applies a fixed system prompt, replays the stored history, sends the new user
 * message with the requested temperature, then persists the user message and
 * Claude's answer back into {@link ConversationMemoryService}.
 */
@Service
public class MultiTurnChatService {

    /** Default persona applied to every conversation. */
    public static final String DEFAULT_SYSTEM_PROMPT =
            "You are a senior software engineering assistant. Help the user reason about "
                    + "software architecture, backlog items, acceptance criteria and implementation "
                    + "trade-offs.";

    /** Default sampling temperature when the caller does not provide one. */
    public static final double DEFAULT_TEMPERATURE = 0.2;

    private final ChatClient chatClient;
    private final ConversationMemoryService memory;

    public MultiTurnChatService(ChatClient chatClient, ConversationMemoryService memory) {
        this.chatClient = chatClient;
        this.memory = memory;
    }

    /**
     * Sends a message within a conversation and returns Claude's answer.
     *
     * @param conversationId existing id to continue, or {@code null}/blank to start a new one
     * @param message        the user message
     * @param temperature    sampling temperature in [0.0, 1.0], or {@code null} for the default
     * @return the (possibly new) conversation id and Claude's answer
     */
    public Result chat(String conversationId, String message, Double temperature) {
        String resolvedId = (conversationId == null || conversationId.isBlank())
                ? UUID.randomUUID().toString()
                : conversationId;
        double resolvedTemperature = temperature != null ? temperature : DEFAULT_TEMPERATURE;

        List<Message> history = memory.getHistory(resolvedId);

        String content = chatClient.prompt()
                .system(DEFAULT_SYSTEM_PROMPT)
                .messages(history)
                .user(message)
                .options(AnthropicChatOptions.builder()
                        .temperature(resolvedTemperature)
                        .build())
                .call()
                .content();

        memory.add(resolvedId, new UserMessage(message), new AssistantMessage(content));

        return new Result(resolvedId, content);
    }

    /**
     * Result of a multi-turn exchange.
     *
     * @param conversationId the conversation id to reuse on the next turn
     * @param content        Claude's text answer
     */
    public record Result(String conversationId, String content) {
    }
}
