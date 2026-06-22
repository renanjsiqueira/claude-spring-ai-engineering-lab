package com.renansiqueira.claudelab.api;

import com.renansiqueira.claudelab.application.BacklogItemSummaryResponse;
import com.renansiqueira.claudelab.application.ProjectResponse;
import com.renansiqueira.claudelab.application.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP entry point for projects and their backlog listing.
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody CreateProjectRequest request) {
        return projectService.create(request.id(), request.name(), request.description());
    }

    @GetMapping("/{id}")
    public ProjectResponse get(@PathVariable String id) {
        return projectService.get(id);
    }

    @GetMapping("/{id}/backlog")
    public List<BacklogItemSummaryResponse> backlog(@PathVariable String id) {
        return projectService.backlog(id);
    }
}
