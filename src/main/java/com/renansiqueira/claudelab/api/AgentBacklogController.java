package com.renansiqueira.claudelab.api;

import com.renansiqueira.claudelab.ai.AgentBacklogService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP entry point for the tool-using backlog agent.
 *
 * <p>Delegates to {@link AgentBacklogService}; tool logic lives in the
 * {@code tools} package, never in the controller.
 */
@RestController
@RequestMapping("/api/agent")
public class AgentBacklogController {

    private final AgentBacklogService agentBacklogService;

    public AgentBacklogController(AgentBacklogService agentBacklogService) {
        this.agentBacklogService = agentBacklogService;
    }

    @PostMapping("/backlog")
    public AgentBacklogResponse backlog(@Valid @RequestBody AgentBacklogRequest request) {
        String content = agentBacklogService.handle(request.projectId(), request.message());
        return new AgentBacklogResponse(content);
    }
}
