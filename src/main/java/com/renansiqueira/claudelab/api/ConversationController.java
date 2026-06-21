package com.renansiqueira.claudelab.api;

import com.renansiqueira.claudelab.ai.MultiTurnChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP entry point for multi-turn conversations with Claude.
 */
@RestController
@RequestMapping("/api/chat")
public class ConversationController {

    private final MultiTurnChatService chatService;

    public ConversationController(MultiTurnChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/conversations")
    public MultiTurnChatResponse chat(@Valid @RequestBody MultiTurnChatRequest request) {
        MultiTurnChatService.Result result = chatService.chat(
                request.conversationId(), request.message(), request.temperature());
        return new MultiTurnChatResponse(result.conversationId(), result.content());
    }
}
