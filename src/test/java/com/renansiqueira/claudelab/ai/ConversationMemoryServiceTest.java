package com.renansiqueira.claudelab.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

class ConversationMemoryServiceTest {

    @Test
    void unknownConversationHasEmptyHistory() {
        ConversationMemoryService memory = new ConversationMemoryService(10);

        assertThat(memory.getHistory("missing")).isEmpty();
    }

    @Test
    void storesMessagesInChronologicalOrder() {
        ConversationMemoryService memory = new ConversationMemoryService(10);

        memory.add("c1", new UserMessage("hi"), new AssistantMessage("hello"));
        memory.add("c1", new UserMessage("how are you?"));

        List<Message> history = memory.getHistory("c1");
        assertThat(history).extracting(Message::getText)
                .containsExactly("hi", "hello", "how are you?");
    }

    @Test
    void historyIsBoundedToMaxMessages() {
        ConversationMemoryService memory = new ConversationMemoryService(3);

        memory.add("c1", new UserMessage("m1"));
        memory.add("c1", new AssistantMessage("m2"));
        memory.add("c1", new UserMessage("m3"));
        memory.add("c1", new AssistantMessage("m4"));
        memory.add("c1", new UserMessage("m5"));

        List<Message> history = memory.getHistory("c1");
        assertThat(history).hasSize(3);
        assertThat(history).extracting(Message::getText)
                .containsExactly("m3", "m4", "m5");
    }

    @Test
    void conversationsAreIsolated() {
        ConversationMemoryService memory = new ConversationMemoryService(10);

        memory.add("c1", new UserMessage("from c1"));
        memory.add("c2", new UserMessage("from c2"));

        assertThat(memory.getHistory("c1")).extracting(Message::getText).containsExactly("from c1");
        assertThat(memory.getHistory("c2")).extracting(Message::getText).containsExactly("from c2");
    }

    @Test
    void clearForgetsConversation() {
        ConversationMemoryService memory = new ConversationMemoryService(10);
        memory.add("c1", new UserMessage("hi"));

        memory.clear("c1");

        assertThat(memory.getHistory("c1")).isEmpty();
    }

    @Test
    void rejectsNonPositiveMaxMessages() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> new ConversationMemoryService(0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
