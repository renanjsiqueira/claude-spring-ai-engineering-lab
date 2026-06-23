package com.renansiqueira.claudelab.api;

import com.renansiqueira.claudelab.workflow.WorkflowResult;
import com.renansiqueira.claudelab.workflow.WorkflowService;
import com.renansiqueira.claudelab.workflow.WorkflowType;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP entry point for the engineering-analysis workflows.
 */
@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/analyze")
    public WorkflowAnalyzeResponse analyze(@Valid @RequestBody WorkflowAnalyzeRequest request) {
        WorkflowType type = parseWorkflow(request.workflow());
        WorkflowResult result = workflowService.analyze(request.input(), type);
        return new WorkflowAnalyzeResponse(result.type().name(), result.finalAnalysis(), result.steps());
    }

    private static WorkflowType parseWorkflow(String value) {
        if (value == null || value.isBlank()) {
            return null; // service defaults to CHAINING
        }
        try {
            return WorkflowType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "unknown workflow '" + value + "'; expected one of CHAINING, ROUTING, PARALLEL");
        }
    }
}
