package com.renansiqueira.claudelab.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.renansiqueira.claudelab.workflow.WorkflowResult;
import com.renansiqueira.claudelab.workflow.WorkflowService;
import com.renansiqueira.claudelab.workflow.WorkflowStep;
import com.renansiqueira.claudelab.workflow.WorkflowType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WorkflowController.class)
class WorkflowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkflowService workflowService;

    @Test
    void runsDefaultChainingWorkflow() throws Exception {
        when(workflowService.analyze(any(), eq(null))).thenReturn(new WorkflowResult(
                WorkflowType.CHAINING, "final", List.of(new WorkflowStep("classify", "FEATURE"))));

        mockMvc.perform(post("/api/workflows/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"input\": \"Precisamos criar importação CSV\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workflowType").value("CHAINING"))
                .andExpect(jsonPath("$.finalAnalysis").value("final"))
                .andExpect(jsonPath("$.steps[0].name").value("classify"));
    }

    @Test
    void runsRequestedWorkflow() throws Exception {
        when(workflowService.analyze(any(), eq(WorkflowType.PARALLEL))).thenReturn(new WorkflowResult(
                WorkflowType.PARALLEL, "merged", List.of(new WorkflowStep("merge", "merged"))));

        mockMvc.perform(post("/api/workflows/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"input\": \"x\", \"workflow\": \"parallel\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workflowType").value("PARALLEL"));
    }

    @Test
    void blankInputReturns400() throws Exception {
        mockMvc.perform(post("/api/workflows/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"input\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("input must not be blank"));
    }

    @Test
    void unknownWorkflowReturns400() throws Exception {
        mockMvc.perform(post("/api/workflows/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"input\": \"x\", \"workflow\": \"bogus\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "unknown workflow 'bogus'; expected one of CHAINING, ROUTING, PARALLEL"));
    }
}
