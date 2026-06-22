package com.renansiqueira.claudelab.api;

import com.renansiqueira.claudelab.application.BacklogItemResponse;
import com.renansiqueira.claudelab.application.BacklogService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP entry point for reading a single persisted backlog item.
 *
 * <p>Separate from {@link BacklogController} (which does Claude-backed analysis at
 * {@code POST /api/backlog/analyze}); this one serves {@code GET /api/backlog/{id}}.
 */
@RestController
@RequestMapping("/api/backlog")
public class BacklogItemController {

    private final BacklogService backlogService;

    public BacklogItemController(BacklogService backlogService) {
        this.backlogService = backlogService;
    }

    @GetMapping("/{id}")
    public BacklogItemResponse get(@PathVariable UUID id) {
        return backlogService.get(id);
    }
}
