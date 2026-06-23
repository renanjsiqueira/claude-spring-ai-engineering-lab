package com.renansiqueira.claudelab.workflow;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Dispatches an analysis request to the requested {@link EngineeringWorkflow}
 * (defaulting to {@link WorkflowType#CHAINING}).
 */
@Service
public class WorkflowService {

    private final Map<WorkflowType, EngineeringWorkflow> workflows = new EnumMap<>(WorkflowType.class);

    public WorkflowService(List<EngineeringWorkflow> workflows) {
        for (EngineeringWorkflow workflow : workflows) {
            this.workflows.put(workflow.type(), workflow);
        }
    }

    public WorkflowResult analyze(String input, WorkflowType type) {
        WorkflowType resolved = type != null ? type : WorkflowType.CHAINING;
        EngineeringWorkflow workflow = workflows.get(resolved);
        if (workflow == null) {
            throw new IllegalArgumentException("no workflow registered for " + resolved);
        }
        return workflow.run(input);
    }
}
