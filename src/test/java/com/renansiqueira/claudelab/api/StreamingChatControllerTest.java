package com.renansiqueira.claudelab.api;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.renansiqueira.claudelab.ai.StreamingChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Flux;

/**
 * Web-layer tests for {@link StreamingChatController}. The Claude integration is
 * mocked with a fixed {@link Flux} of chunks, so no real Anthropic call happens.
 */
@WebMvcTest(StreamingChatController.class)
class StreamingChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StreamingChatService streamingChatService;

    @Test
    void streamsIncrementalChunks() throws Exception {
        when(streamingChatService.stream(any())).thenReturn(Flux.just("Hello", " world"));

        MvcResult result = mockMvc.perform(get("/api/chat/stream").param("message", "hi"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(allOf(containsString("Hello"), containsString("world"))));
    }

    @Test
    void blankMessageReturns400() throws Exception {
        mockMvc.perform(get("/api/chat/stream").param("message", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("message must not be blank"));
    }
}
