package com.renansiqueira.claudelab.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.renansiqueira.claudelab.ai.AgentBacklogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web-layer tests for {@link AgentBacklogController}. The agent (and its tools)
 * are mocked, so no real Claude call happens here.
 */
@WebMvcTest(AgentBacklogController.class)
class AgentBacklogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgentBacklogService agentBacklogService;

    @Test
    void returnsAgentReply() throws Exception {
        when(agentBacklogService.handle(any(), any()))
                .thenReturn("Created 'Import customers via CSV' (complexity: HIGH).");

        mockMvc.perform(post("/api/agent/backlog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectId\": \"brabrix-dev\", \"message\": \"Crie uma tarefa para importar clientes via CSV\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Created 'Import customers via CSV' (complexity: HIGH)."));
    }

    @Test
    void blankProjectIdReturns400() throws Exception {
        mockMvc.perform(post("/api/agent/backlog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectId\": \"\", \"message\": \"do something\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("projectId must not be blank"));
    }

    @Test
    void blankMessageReturns400() throws Exception {
        mockMvc.perform(post("/api/agent/backlog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectId\": \"brabrix-dev\", \"message\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("message must not be blank"));
    }
}
