package com.renansiqueira.claudelab.application;

import com.renansiqueira.claudelab.persistence.AcceptanceCriterion;
import com.renansiqueira.claudelab.persistence.BacklogItem;
import com.renansiqueira.claudelab.persistence.BacklogItemRepository;
import com.renansiqueira.claudelab.persistence.Project;
import com.renansiqueira.claudelab.persistence.ProjectRepository;
import com.renansiqueira.claudelab.persistence.TechnicalTask;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases for creating and reading backlog items.
 */
@Service
public class BacklogService {

    private final ProjectRepository projectRepository;
    private final BacklogItemRepository backlogItemRepository;

    public BacklogService(ProjectRepository projectRepository,
                          BacklogItemRepository backlogItemRepository) {
        this.projectRepository = projectRepository;
        this.backlogItemRepository = backlogItemRepository;
    }

    /**
     * Creates and persists a backlog item under an existing project.
     *
     * @throws NotFoundException if the project does not exist
     */
    @Transactional
    public BacklogItemResponse createItem(String projectId, String title, String description) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("project '" + projectId + "' not found"));
        BacklogItem item = new BacklogItem(project, title);
        item.setSummary(description);
        return toResponse(backlogItemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public BacklogItemResponse get(UUID id) {
        BacklogItem item = backlogItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("backlog item '" + id + "' not found"));
        return toResponse(item);
    }

    private BacklogItemResponse toResponse(BacklogItem item) {
        return new BacklogItemResponse(
                item.getId(),
                item.getProject().getId(),
                item.getType(),
                item.getTitle(),
                item.getSummary(),
                item.getPriority(),
                item.getUserStory(),
                item.getAcceptanceCriteria().stream().map(AcceptanceCriterion::getDescription).toList(),
                item.getTechnicalTasks().stream().map(TechnicalTask::getDescription).toList(),
                item.getCreatedAt());
    }
}
