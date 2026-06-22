package com.renansiqueira.claudelab.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for {@link BacklogItem}.
 */
public interface BacklogItemRepository extends JpaRepository<BacklogItem, UUID> {

    List<BacklogItem> findByProject_IdOrderByCreatedAtAsc(String projectId);
}
