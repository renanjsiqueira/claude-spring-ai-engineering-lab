package com.renansiqueira.claudelab.api;

import com.renansiqueira.claudelab.ai.BacklogAnalysisService;
import com.renansiqueira.claudelab.domain.BacklogAnalysisResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP entry point for structured backlog analysis.
 */
@RestController
@RequestMapping("/api/backlog")
public class BacklogController {

    private final BacklogAnalysisService analysisService;

    public BacklogController(BacklogAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/analyze")
    public BacklogAnalysisResponse analyze(@Valid @RequestBody BacklogAnalysisRequest request) {
        return analysisService.analyze(request.input());
    }
}
