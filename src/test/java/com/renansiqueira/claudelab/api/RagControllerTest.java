package com.renansiqueira.claudelab.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.renansiqueira.claudelab.rag.RagAnswer;
import com.renansiqueira.claudelab.rag.RagAnswerService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RagController.class)
class RagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RagAnswerService ragAnswerService;

    @Test
    void returnsAnswerWithSources() throws Exception {
        when(ragAnswerService.answer(any())).thenReturn(
                new RagAnswer("Use records and validate inputs.", List.of("coding-standards.md")));

        mockMvc.perform(post("/api/rag/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\": \"Quais padrões devo seguir para criar uma nova feature?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Use records and validate inputs."))
                .andExpect(jsonPath("$.sources[0]").value("coding-standards.md"));
    }

    @Test
    void blankQuestionReturns400() throws Exception {
        mockMvc.perform(post("/api/rag/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("question must not be blank"));
    }
}
