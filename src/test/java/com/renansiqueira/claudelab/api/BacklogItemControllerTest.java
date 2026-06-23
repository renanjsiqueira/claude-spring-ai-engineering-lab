package com.renansiqueira.claudelab.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.renansiqueira.claudelab.application.BacklogItemResponse;
import com.renansiqueira.claudelab.application.BacklogService;
import com.renansiqueira.claudelab.application.NotFoundException;
import com.renansiqueira.claudelab.domain.BacklogType;
import com.renansiqueira.claudelab.domain.Priority;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BacklogItemController.class)
class BacklogItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BacklogService backlogService;

    @Test
    void returnsBacklogItem() throws Exception {
        UUID id = UUID.randomUUID();
        when(backlogService.get(id)).thenReturn(new BacklogItemResponse(
                id, "devbacklog-ai-assistant", BacklogType.FEATURE, "Import CSV", "summary", Priority.HIGH,
                "As a user...", List.of("AC1"), List.of("T1"), Instant.now()));

        mockMvc.perform(get("/api/backlog/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.projectId").value("devbacklog-ai-assistant"))
                .andExpect(jsonPath("$.acceptanceCriteria[0]").value("AC1"));
    }

    @Test
    void missingItemReturns404() throws Exception {
        when(backlogService.get(any())).thenThrow(new NotFoundException("backlog item not found"));

        mockMvc.perform(get("/api/backlog/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("backlog item not found"));
    }
}
