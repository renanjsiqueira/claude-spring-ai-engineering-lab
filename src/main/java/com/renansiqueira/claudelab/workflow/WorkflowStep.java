package com.renansiqueira.claudelab.workflow;

/**
 * One step of a workflow run: a step name and the text it produced.
 *
 * @param name   the step name
 * @param output the step output
 */
public record WorkflowStep(String name, String output) {
}
