package com.renansiqueira.claudelab.api;

/**
 * Response body for {@code POST /api/chat/conversations}.
 *
 * @param conversationId the conversation id to reuse on the next turn
 * @param content        Claude's text answer
 */
public record MultiTurnChatResponse(String conversationId, String content) {
}
