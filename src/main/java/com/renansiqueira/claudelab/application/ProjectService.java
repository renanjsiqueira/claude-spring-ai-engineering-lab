package com.renansiqueira.claudelab.application;

import com.renansiqueira.claudelab.persistence.BacklogItem;
import com.renansiqueira.claudelab.persistence.BacklogItemRepository;
import com.renansiqueira.claudelab.persistence.Project;
import com.renansiqueira.claudelab.persistence.ProjectRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases for projects and their backlog listing.
 */
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final BacklogItemRepository backlogItemRepository;

    public ProjectService(ProjectRepository projectRepository,
                          BacklogItemRepository backlogItemRepository) {
        this.projectRepository = projectRepository;
        this.backlogItemRepository = backlogItemRepository;
    }

    @Transactional
    public ProjectResponse create(String id, String name, String description) {
        if (projectRepository.existsById(id)) {
            throw new ConflictException("project '" + id + "' already exists");
        }
        Project saved = projectRepository.save(new Project(id, name, description));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(String id) {
        return toResponse(require(id));
    }

    @Transactional(readOnly = true)
    public List<BacklogItemSummaryResponse> backlog(String projectId) {
        require(projectId);
        return backlogItemRepository.findByProject_IdOrderByCreatedAtAsc(projectId).stream()
                .map(item -> new BacklogItemSummaryResponse(
                        item.getId(), item.getType(), item.getTitle(), item.getPriority()))
                .toList();
    }

    private Project require(String id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("project '" + id + "' not found"));
    }

    private ProjectResponse toResponse(Project p) {
        return new ProjectResponse(p.getId(), p.getName(), p.getDescription(), p.getCreatedAt());
    }
}
