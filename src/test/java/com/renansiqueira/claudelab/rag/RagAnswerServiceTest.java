package com.renansiqueira.claudelab.rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

class RagAnswerServiceTest {

    @Test
    void returnsNotEnoughInformationWithoutCallingClaudeWhenNoContext() {
        DocumentSearchService search = mock(DocumentSearchService.class);
        ChatClient chatClient = mock(ChatClient.class);
        when(search.search("anything", 4)).thenReturn(List.of());

        RagAnswerService service = new RagAnswerService(search, chatClient, 4);
        RagAnswer answer = service.answer("anything");

        assertThat(answer.answer()).isEqualTo(RagAnswerService.NOT_ENOUGH_INFORMATION);
        assertThat(answer.sources()).isEmpty();
        verifyNoInteractions(chatClient);
    }
}
