package com.renansiqueira.claudelab.api;

import com.renansiqueira.claudelab.rag.RagAnswer;
import com.renansiqueira.claudelab.rag.RagAnswerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP entry point for asking questions over the project knowledge base (RAG).
 */
@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagAnswerService ragAnswerService;

    public RagController(RagAnswerService ragAnswerService) {
        this.ragAnswerService = ragAnswerService;
    }

    @PostMapping("/ask")
    public RagAskResponse ask(@Valid @RequestBody RagAskRequest request) {
        RagAnswer answer = ragAnswerService.answer(request.question());
        return new RagAskResponse(answer.answer(), answer.sources());
    }
}
