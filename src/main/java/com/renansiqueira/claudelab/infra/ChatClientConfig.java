package com.renansiqueira.claudelab.infra;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Builds the {@link ChatClient} used to talk to Claude.
 *
 * <p>Spring AI auto-configures a {@link ChatClient.Builder} from the Anthropic
 * model properties (see {@code application.yml}). We turn it into a ready-to-use
 * {@link ChatClient} bean so application services can stay free of builder
 * wiring. In Phase 1 the client is intentionally plain — system prompts,
 * temperature and streaming come in later phases.
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
