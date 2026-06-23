package com.renansiqueira.claudelab.workflow;

/**
 * An engineering-analysis workflow over a raw input.
 */
public interface EngineeringWorkflow {

    /** The workflow this implementation provides. */
    WorkflowType type();

    /** Runs the workflow for the given input. */
    WorkflowResult run(String input);
}
