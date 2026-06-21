package com.renansiqueira.claudelab.api;

import com.renansiqueira.claudelab.ai.StreamingChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * HTTP entry point for streaming chat responses as Server-Sent Events.
 *
 * <p>Kept separate from {@link ChatController} so the synchronous
 * {@code /api/chat} endpoint is not affected.
 */
@RestController
@RequestMapping("/api/chat")
public class StreamingChatController {

    private final StreamingChatService streamingChatService;

    public StreamingChatController(StreamingChatService streamingChatService) {
        this.streamingChatService = streamingChatService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam(required = false) String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
        return streamingChatService.stream(message);
    }
}
