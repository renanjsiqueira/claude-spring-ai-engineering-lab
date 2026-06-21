package com.renansiqueira.claudelab.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Streams Claude's answer incrementally as it is generated.
 *
 * <p>Uses Spring AI's reactive {@code .stream().content()}, which yields a
 * {@link Flux} of text chunks. This is intentionally separate from the
 * synchronous {@link ClaudeChatService} so the blocking {@code /api/chat}
 * endpoint is unaffected.
 */
@Service
public class StreamingChatService {

    private final ChatClient chatClient;

    public StreamingChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Streams Claude's response to a single message as a sequence of text chunks.
     *
     * <p>If the underlying call fails mid-stream, a friendly final chunk is
     * emitted instead of propagating the raw error to the client.
     *
     * @param message the user message
     * @return a stream of incremental text chunks
     */
    public Flux<String> stream(String message) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .content()
                .onErrorResume(ex -> Flux.just(
                        "\n\n[stream interrupted] the response could not be completed. Please try again."));
    }
}
