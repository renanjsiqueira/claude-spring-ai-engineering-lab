package com.renansiqueira.claudelab.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * In-memory store of conversation history keyed by {@code conversationId}.
 *
 * <p>Each conversation keeps an ordered list of {@link Message}s (user and
 * assistant turns; the system prompt is applied per request and never stored).
 * The history is bounded to the most recent {@code maxMessages} entries so it
 * cannot grow without limit. State lives only in memory and is lost on restart —
 * a persistent store can replace this later without changing callers.
 */
@Service
public class ConversationMemoryService {

    private final int maxMessages;
    private final Map<String, List<Message>> store = new ConcurrentHashMap<>();

    public ConversationMemoryService(
            @Value("${claudelab.chat.max-history-messages:20}") int maxMessages) {
        if (maxMessages <= 0) {
            throw new IllegalArgumentException("maxMessages must be positive, was " + maxMessages);
        }
        this.maxMessages = maxMessages;
    }

    /**
     * Returns an immutable copy of the conversation history in chronological
     * order, or an empty list if the conversation is unknown.
     */
    public List<Message> getHistory(String conversationId) {
        List<Message> history = store.get(conversationId);
        return history == null ? List.of() : List.copyOf(history);
    }

    /**
     * Appends messages to a conversation, trimming the oldest entries so that no
     * more than {@code maxMessages} are retained.
     */
    public void add(String conversationId, Message... messages) {
        store.compute(conversationId, (id, existing) -> {
            List<Message> updated = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
            Collections.addAll(updated, messages);
            int size = updated.size();
            if (size > maxMessages) {
                return new ArrayList<>(updated.subList(size - maxMessages, size));
            }
            return updated;
        });
    }

    /**
     * Forgets a conversation entirely.
     */
    public void clear(String conversationId) {
        store.remove(conversationId);
    }
}
