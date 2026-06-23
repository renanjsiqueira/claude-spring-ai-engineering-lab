/**
 * <strong>workflow</strong> — agentic workflows built on the backlog assistant.
 *
 * <p>Three patterns: {@code ChainingWorkflow} (sequential steps), {@code RoutingWorkflow}
 * (type-based dispatch) and {@code ParallelReviewWorkflow} (fan-out + merge). Each implements
 * {@code EngineeringWorkflow} and talks to Claude through the {@code LlmStep} abstraction, which keeps
 * the orchestration logic unit-testable without calling the model.
 */
package com.renansiqueira.claudelab.workflow;
