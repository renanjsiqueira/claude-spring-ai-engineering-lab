package com.renansiqueira.claudelab.ai;

import com.renansiqueira.claudelab.tools.BacklogTool;
import com.renansiqueira.claudelab.tools.ComplexityTool;
import com.renansiqueira.claudelab.tools.ProjectContextTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Tool-using agent that turns a user request into a persisted backlog item.
 *
 * <p>Registers the three Spring tools on the {@link ChatClient}; Spring AI then
 * runs the tool-calling loop automatically — Claude decides when to call
 * {@code getProjectContext}, {@code estimateComplexity} and
 * {@code createBacklogItem}, and we return its final natural-language reply.
 */
@Service
public class AgentBacklogService {

    static final String SYSTEM_PROMPT = """
            You are an engineering assistant that turns a user's request into a backlog item for a
            specific project. You have these tools:
            - getProjectContext(projectId): learn the project's name, description and tech stack.
            - estimateComplexity(description): estimate LOW/MEDIUM/HIGH complexity of the work.
            - createBacklogItem(projectId, title, description): persist the backlog item.

            Workflow:
            1. Call getProjectContext to ground yourself in the project.
            2. Call estimateComplexity for the requested work.
            3. Call createBacklogItem once, with a clear title and a description aligned to the
               project's stack.
            4. Reply to the user in plain language, confirming the created item's title and its
               estimated complexity. Do not output JSON.

            Use the projectId provided by the user. Create exactly one backlog item per request.
            """;

    private final ChatClient chatClient;
    private final ProjectContextTool projectContextTool;
    private final BacklogTool backlogTool;
    private final ComplexityTool complexityTool;

    public AgentBacklogService(ChatClient chatClient,
                               ProjectContextTool projectContextTool,
                               BacklogTool backlogTool,
                               ComplexityTool complexityTool) {
        this.chatClient = chatClient;
        this.projectContextTool = projectContextTool;
        this.backlogTool = backlogTool;
        this.complexityTool = complexityTool;
    }

    /**
     * Handles a backlog request for a project, letting Claude call tools as needed.
     *
     * @param projectId the target project
     * @param message   the user's request
     * @return Claude's final natural-language reply
     */
    public String handle(String projectId, String message) {
        String userMessage = "Project: " + projectId + "\n\nRequest: " + message;
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userMessage)
                .tools(projectContextTool, backlogTool, complexityTool)
                .call()
                .content();
    }
}
