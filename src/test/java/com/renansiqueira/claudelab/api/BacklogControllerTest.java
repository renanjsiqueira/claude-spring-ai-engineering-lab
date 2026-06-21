package com.renansiqueira.claudelab.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.renansiqueira.claudelab.ai.BacklogAnalysisService;
import com.renansiqueira.claudelab.domain.BacklogAnalysisResponse;
import com.renansiqueira.claudelab.domain.BacklogType;
import com.renansiqueira.claudelab.domain.Priority;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web-layer tests for {@link BacklogController}. The Claude integration is
 * mocked, so these assert the structured JSON contract for each backlog type.
 */
@WebMvcTest(BacklogController.class)
class BacklogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BacklogAnalysisService analysisService;

    @Test
    void analyzesFeature() throws Exception {
        when(analysisService.analyze(any())).thenReturn(new BacklogAnalysisResponse(
                BacklogType.FEATURE,
                "CSV Transaction Import",
                "Import transactions from a CSV file.",
                Priority.HIGH,
                "As a user, I want to import transactions via CSV, so that I avoid manual entry.",
                List.of("Given a valid CSV, when uploaded, then transactions are persisted."),
                List.of("Add CSV parser", "Validate rows"),
                List.of("CSV format is unspecified"),
                List.of("Assuming UTF-8 encoding")));

        mockMvc.perform(post("/api/backlog/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"input\": \"Importar transações via CSV\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FEATURE"))
                .andExpect(jsonPath("$.title").value("CSV Transaction Import"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.acceptanceCriteria").isArray())
                .andExpect(jsonPath("$.assumptions[0]").value("Assuming UTF-8 encoding"));
    }

    @Test
    void analyzesBug() throws Exception {
        when(analysisService.analyze(any())).thenReturn(new BacklogAnalysisResponse(
                BacklogType.BUG,
                "Login fails on expired token",
                "Users are not redirected to login when the token expires.",
                Priority.CRITICAL,
                "As a user, I want a clear session-expired flow, so that I can log in again.",
                List.of("Given an expired token, when calling the API, then a 401 is returned."),
                List.of("Add token expiry check"),
                List.of("Reproduction steps not provided"),
                List.of()));

        mockMvc.perform(post("/api/backlog/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"input\": \"App quebra quando o token expira\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("BUG"))
                .andExpect(jsonPath("$.priority").value("CRITICAL"))
                .andExpect(jsonPath("$.risks[0]").value("Reproduction steps not provided"));
    }

    @Test
    void analyzesRefactor() throws Exception {
        when(analysisService.analyze(any())).thenReturn(new BacklogAnalysisResponse(
                BacklogType.REFACTOR,
                "Extract payment gateway",
                "Decouple the payment gateway behind an interface.",
                Priority.MEDIUM,
                "As a developer, I want a payment abstraction, so that I can swap providers.",
                List.of("Given the refactor, when tests run, then behavior is unchanged."),
                List.of("Introduce PaymentGateway interface", "Move Stripe logic behind it"),
                List.of(),
                List.of()));

        mockMvc.perform(post("/api/backlog/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"input\": \"Refatorar o módulo de pagamentos\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("REFACTOR"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.technicalTasks").isArray());
    }

    @Test
    void blankInputReturns400() throws Exception {
        mockMvc.perform(post("/api/backlog/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"input\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("input must not be blank"));
    }
}
