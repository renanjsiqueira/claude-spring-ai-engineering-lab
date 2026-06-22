package com.renansiqueira.claudelab.api;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/projects}.
 *
 * @param id          project code (e.g. "brabrix-dev"); must not be blank
 * @param name        human-readable name; must not be blank
 * @param description optional description
 */
public record CreateProjectRequest(

        @NotBlank(message = "id must not be blank")
        String id,

        @NotBlank(message = "name must not be blank")
        String name,

        String description
) {
}
