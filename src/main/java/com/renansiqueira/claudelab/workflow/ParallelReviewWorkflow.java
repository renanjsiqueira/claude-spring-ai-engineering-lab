package com.renansiqueira.claudelab.workflow;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Component;

/**
 * Parallel workflow: run four independent analyses (product, technical, risks,
 * tests) concurrently, then merge them into one final analysis.
 */
@Component
public class ParallelReviewWorkflow implements EngineeringWorkflow {

    private static final String PRODUCT_SYSTEM =
            "You are a product analyst. Assess the product value, scope and user impact of the request.";
    private static final String TECHNICAL_SYSTEM =
            "You are a senior engineer. Assess the technical approach, design and implementation effort.";
    private static final String RISKS_SYSTEM =
            "You are a risk analyst. Identify the risks, edge cases and failure modes of the request.";
    private static final String TESTS_SYSTEM =
            "You are a QA analyst. Define the testing strategy and the key test cases for the request.";
    private static final String MERGE_SYSTEM =
            "Merge the four analyses below into one cohesive final engineering analysis with clear "
                    + "recommendations. Keep it structured and concise.";

    private final LlmStep step;

    public ParallelReviewWorkflow(LlmStep step) {
        this.step = step;
    }

    @Override
    public WorkflowType type() {
        return WorkflowType.PARALLEL;
    }

    @Override
    public WorkflowResult run(String input) {
        CompletableFuture<String> product = CompletableFuture.supplyAsync(() -> step.complete(PRODUCT_SYSTEM, input));
        CompletableFuture<String> technical = CompletableFuture.supplyAsync(() -> step.complete(TECHNICAL_SYSTEM, input));
        CompletableFuture<String> risks = CompletableFuture.supplyAsync(() -> step.complete(RISKS_SYSTEM, input));
        CompletableFuture<String> tests = CompletableFuture.supplyAsync(() -> step.complete(TESTS_SYSTEM, input));
        CompletableFuture.allOf(product, technical, risks, tests).join();

        String productOut = product.join();
        String technicalOut = technical.join();
        String risksOut = risks.join();
        String testsOut = tests.join();

        String merged = step.complete(MERGE_SYSTEM,
                "Product analysis:\n" + productOut
                        + "\n\nTechnical analysis:\n" + technicalOut
                        + "\n\nRisk analysis:\n" + risksOut
                        + "\n\nTest analysis:\n" + testsOut);

        List<WorkflowStep> steps = List.of(
                new WorkflowStep("product", productOut),
                new WorkflowStep("technical", technicalOut),
                new WorkflowStep("risks", risksOut),
                new WorkflowStep("tests", testsOut),
                new WorkflowStep("merge", merged));
        return new WorkflowResult(WorkflowType.PARALLEL, merged, steps);
    }
}
