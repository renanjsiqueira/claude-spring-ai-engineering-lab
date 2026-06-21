package com.renansiqueira.claudelab.api;

import com.renansiqueira.claudelab.eval.EvaluationResult;
import com.renansiqueira.claudelab.eval.PromptEvaluationRunner;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP entry point to trigger an evaluation run.
 *
 * <p>Note: this runs the full dataset against Claude and therefore makes real
 * API calls — it is a manual tool, not something to call on every request.
 */
@RestController
@RequestMapping("/api/evals")
public class EvaluationController {

    private final PromptEvaluationRunner evaluationRunner;

    public EvaluationController(PromptEvaluationRunner evaluationRunner) {
        this.evaluationRunner = evaluationRunner;
    }

    @PostMapping("/run")
    public EvaluationResult run() {
        return evaluationRunner.run();
    }
}
