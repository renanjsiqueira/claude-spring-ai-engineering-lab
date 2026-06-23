package com.renansiqueira.claudelab.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Component;

/**
 * MCP prompts: reusable prompt templates a client can fetch and run.
 */
@Component
public class ProjectMcpPrompts {

    @McpPrompt(name = "generate_backlog_item",
            description = "Generate a structured engineering backlog item from a raw request.")
    public McpSchema.GetPromptResult generateBacklogItem(
            @McpArg(name = "request", description = "The raw idea, bug or feature request", required = true)
            String request) {
        String text = """
                You are a senior software engineer assistant. Transform the request below into a
                structured backlog item: type, title, summary, priority, user story, testable
                acceptance criteria, technical tasks, risks and assumptions. Do not invent business
                rules; capture unknowns as assumptions/risks.

                Request:
                %s
                """.formatted(request);
        return new McpSchema.GetPromptResult(
                "Generate a structured backlog item",
                List.of(new McpSchema.PromptMessage(McpSchema.Role.USER, new McpSchema.TextContent(text))));
    }

    @McpPrompt(name = "review_architecture_decision",
            description = "Review an architecture decision and surface trade-offs and risks.")
    public McpSchema.GetPromptResult reviewArchitectureDecision(
            @McpArg(name = "decision", description = "The architecture decision to review", required = true)
            String decision) {
        String text = """
                You are a principal software architect. Review the architecture decision below.
                Give a clear recommendation, justify the trade-offs, and call out risks, failure modes
                and scalability limits. State assumptions explicitly; do not invent requirements.

                Decision:
                %s
                """.formatted(decision);
        return new McpSchema.GetPromptResult(
                "Review an architecture decision",
                List.of(new McpSchema.PromptMessage(McpSchema.Role.USER, new McpSchema.TextContent(text))));
    }
}
