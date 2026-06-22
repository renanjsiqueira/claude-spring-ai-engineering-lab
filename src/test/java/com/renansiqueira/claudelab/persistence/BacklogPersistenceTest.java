package com.renansiqueira.claudelab.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.renansiqueira.claudelab.application.BacklogItemResponse;
import com.renansiqueira.claudelab.application.BacklogService;
import com.renansiqueira.claudelab.application.ConflictException;
import com.renansiqueira.claudelab.application.NotFoundException;
import com.renansiqueira.claudelab.application.ProjectService;
import com.renansiqueira.claudelab.domain.BacklogType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Repository + service integration tests against H2 (PostgreSQL mode), no Docker.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({ProjectService.class, BacklogService.class})
class BacklogPersistenceTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private BacklogService backlogService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private BacklogItemRepository backlogItemRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    void createsAndFetchesProject() {
        projectService.create("p1", "Project One", "desc");

        assertThat(projectService.get("p1").name()).isEqualTo("Project One");
        assertThat(projectService.get("p1").createdAt()).isNotNull();
    }

    @Test
    void rejectsDuplicateProject() {
        projectService.create("p1", "Project One", null);

        assertThatThrownBy(() -> projectService.create("p1", "again", null))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void unknownProjectIsNotFound() {
        assertThatThrownBy(() -> projectService.get("missing"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void persistsBacklogItemUnderProjectAndLists() {
        projectService.create("p1", "Project One", null);

        BacklogItemResponse created = backlogService.createItem("p1", "Import CSV", "Import via CSV");

        assertThat(created.id()).isNotNull();
        assertThat(created.projectId()).isEqualTo("p1");
        assertThat(backlogService.get(created.id()).title()).isEqualTo("Import CSV");
        assertThat(projectService.backlog("p1")).hasSize(1);
    }

    @Test
    void creatingItemForMissingProjectIsNotFound() {
        assertThatThrownBy(() -> backlogService.createItem("nope", "t", "d"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void cascadesChildCollectionsInOrder() {
        Project project = projectRepository.save(new Project("p1", "Project One", null));
        BacklogItem item = new BacklogItem(project, "Title");
        item.setType(BacklogType.FEATURE);
        item.addAcceptanceCriterion("AC1");
        item.addAcceptanceCriterion("AC2");
        item.addTechnicalTask("T1");
        BacklogItem saved = backlogItemRepository.save(item);

        em.flush();
        em.clear();

        BacklogItem reloaded = backlogItemRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getAcceptanceCriteria())
                .extracting(AcceptanceCriterion::getDescription)
                .containsExactly("AC1", "AC2");
        assertThat(reloaded.getTechnicalTasks())
                .extracting(TechnicalTask::getDescription)
                .containsExactly("T1");
    }
}
