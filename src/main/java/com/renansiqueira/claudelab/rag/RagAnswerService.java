package com.renansiqueira.claudelab.rag;

import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Answers a question strictly from the indexed knowledge base.
 *
 * <p>Retrieves the top chunks; if none are relevant it returns a fixed
 * "not enough information" answer without calling Claude. Otherwise it grounds
 * Claude with the retrieved context and returns the answer plus the distinct
 * source files cited.
 */
@Service
public class RagAnswerService {

    static final String NOT_ENOUGH_INFORMATION =
            "I could not find enough information in the project documentation to answer that.";

    static final String SYSTEM_PROMPT = """
            <role>
            You answer questions about a software project strictly from the provided documentation.
            </role>

            <rules>
            - Answer ONLY using the context inside <context>. Do not use outside knowledge.
            - If the context does not contain the answer, say you do not have enough information.
            - Be concise and specific.
            - Do not invent rules, names or numbers.
            </rules>
            """;

    private final DocumentSearchService searchService;
    private final ChatClient chatClient;
    private final int topK;

    public RagAnswerService(DocumentSearchService searchService,
                            ChatClient chatClient,
                            @Value("${claudelab.rag.top-k:4}") int topK) {
        this.searchService = searchService;
        this.chatClient = chatClient;
        this.topK = topK;
    }

    public RagAnswer answer(String question) {
        List<ScoredChunk> hits = searchService.search(question, topK);
        if (hits.isEmpty()) {
            return new RagAnswer(NOT_ENOUGH_INFORMATION, List.of());
        }

        String context = buildContext(hits);
        String content = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user("<context>\n" + context + "\n</context>\n\n<question>\n" + question + "\n</question>")
                .call()
                .content();

        List<String> sources = hits.stream()
                .map(h -> h.chunk().source())
                .distinct()
                .toList();
        return new RagAnswer(content, sources);
    }

    private static String buildContext(List<ScoredChunk> hits) {
        StringBuilder sb = new StringBuilder();
        for (ScoredChunk hit : hits) {
            sb.append("<document source=\"").append(hit.chunk().source()).append("\">\n")
                    .append(hit.chunk().text()).append("\n</document>\n");
        }
        return sb.toString();
    }
}
