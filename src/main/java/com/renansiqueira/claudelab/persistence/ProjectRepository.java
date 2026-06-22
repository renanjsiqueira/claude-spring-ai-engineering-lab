package com.renansiqueira.claudelab.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for {@link Project} (id is the project code).
 */
public interface ProjectRepository extends JpaRepository<Project, String> {
}
