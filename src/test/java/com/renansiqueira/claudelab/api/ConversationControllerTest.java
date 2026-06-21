package com.renansiqueira.claudelab.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.renansiqueira.claudelab.ai.MultiTurnChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web-layer tests for {@link ConversationController}. The Claude integration is
 * mocked, so no real Anthropic call happens.
 */
@WebMvcTest(ConversationController.class)
class ConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MultiTurnChatService chatService;

    @Test
    void returnsConversationIdAndContent() throws Exception {
        when(chatService.chat(any(), any(), any()))
                .thenReturn(new MultiTurnChatService.Result("conv-1", "An answer."));

        mockMvc.perform(post("/api/chat/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"What is a backlog item?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value("conv-1"))
                .andExpect(jsonPath("$.content").value("An answer."));
    }

    @Test
    void blankMessageReturns400() throws Exception {
        mockMvc.perform(post("/api/chat/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("message must not be blank"));
    }

    @Test
    void temperatureOutOfRangeReturns400() throws Exception {
        mockMvc.perform(post("/api/chat/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"hi\", \"temperature\": 1.5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("temperature must be between 0.0 and 1.0"));
    }
}
