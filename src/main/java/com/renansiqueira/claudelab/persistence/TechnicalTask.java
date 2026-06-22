package com.renansiqueira.claudelab.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * A single technical task owned by a {@link BacklogItem}.
 */
@Entity
@Table(name = "technical_task")
public class TechnicalTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "backlog_item_id", nullable = false)
    private BacklogItem backlogItem;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private int position;

    protected TechnicalTask() {
    }

    TechnicalTask(BacklogItem backlogItem, String description, int position) {
        this.backlogItem = backlogItem;
        this.description = description;
        this.position = position;
    }

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public int getPosition() {
        return position;
    }
}
