package com.renansiqueira.claudelab.api;

/**
 * Response body for {@code POST /api/agent/backlog}.
 *
 * @param content Claude's final natural-language reply
 */
public record AgentBacklogResponse(String content) {
}
