package com.renansiqueira.claudelab.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.renansiqueira.claudelab.application.BacklogItemSummaryResponse;
import com.renansiqueira.claudelab.application.ConflictException;
import com.renansiqueira.claudelab.application.NotFoundException;
import com.renansiqueira.claudelab.application.ProjectResponse;
import com.renansiqueira.claudelab.application.ProjectService;
import com.renansiqueira.claudelab.domain.BacklogType;
import com.renansiqueira.claudelab.domain.Priority;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void createReturns201() throws Exception {
        when(projectService.create(any(), any(), any()))
                .thenReturn(new ProjectResponse("devbacklog-ai-assistant", "DevBacklog AI Assistant", "desc", Instant.now()));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"devbacklog-ai-assistant\", \"name\": \"DevBacklog AI Assistant\", \"description\": \"desc\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("devbacklog-ai-assistant"))
                .andExpect(jsonPath("$.name").value("DevBacklog AI Assistant"));
    }

    @Test
    void blankIdReturns400() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"\", \"name\": \"DevBacklog AI Assistant\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("id must not be blank"));
    }

    @Test
    void duplicateReturns409() throws Exception {
        when(projectService.create(any(), any(), any()))
                .thenThrow(new ConflictException("project 'devbacklog-ai-assistant' already exists"));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"devbacklog-ai-assistant\", \"name\": \"DevBacklog AI Assistant\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("project 'devbacklog-ai-assistant' already exists"));
    }

    @Test
    void getMissingReturns404() throws Exception {
        when(projectService.get("missing")).thenThrow(new NotFoundException("project 'missing' not found"));

        mockMvc.perform(get("/api/projects/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("project 'missing' not found"));
    }

    @Test
    void listsBacklog() throws Exception {
        when(projectService.backlog("devbacklog-ai-assistant")).thenReturn(List.of(
                new BacklogItemSummaryResponse(UUID.randomUUID(), BacklogType.FEATURE, "Import CSV", Priority.HIGH)));

        mockMvc.perform(get("/api/projects/devbacklog-ai-assistant/backlog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Import CSV"))
                .andExpect(jsonPath("$[0].type").value("FEATURE"));
    }
}
