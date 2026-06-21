package com.renansiqueira.claudelab.api;

/**
 * Response body for {@code POST /api/chat}.
 *
 * @param content Claude's text answer
 */
public record ChatResponse(String content) {
}
