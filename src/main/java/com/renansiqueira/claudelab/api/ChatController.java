package com.renansiqueira.claudelab.api;

import com.renansiqueira.claudelab.ai.ClaudeChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP entry point for chatting with Claude.
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    private final ClaudeChatService chatService;

    public ChatController(ClaudeChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        String content = chatService.sendMessage(request.message());
        return new ChatResponse(content);
    }
}
