package com.renansiqueira.claudelab.persistence;

import com.renansiqueira.claudelab.domain.BacklogType;
import com.renansiqueira.claudelab.domain.Priority;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A backlog item belonging to a {@link Project}, with its acceptance criteria and
 * technical tasks as owned child collections.
 */
@Entity
@Table(name = "backlog_item")
public class BacklogItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private BacklogType type;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private Priority priority;

    @Column(name = "user_story", length = 2000)
    private String userStory;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "backlogItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<AcceptanceCriterion> acceptanceCriteria = new ArrayList<>();

    @OneToMany(mappedBy = "backlogItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<TechnicalTask> technicalTasks = new ArrayList<>();

    protected BacklogItem() {
    }

    public BacklogItem(Project project, String title) {
        this.project = project;
        this.title = title;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public void addAcceptanceCriterion(String description) {
        acceptanceCriteria.add(new AcceptanceCriterion(this, description, acceptanceCriteria.size()));
    }

    public void addTechnicalTask(String description) {
        technicalTasks.add(new TechnicalTask(this, description, technicalTasks.size()));
    }

    public UUID getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public BacklogType getType() {
        return type;
    }

    public void setType(BacklogType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getUserStory() {
        return userStory;
    }

    public void setUserStory(String userStory) {
        this.userStory = userStory;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<AcceptanceCriterion> getAcceptanceCriteria() {
        return acceptanceCriteria;
    }

    public List<TechnicalTask> getTechnicalTasks() {
        return technicalTasks;
    }
}
