package com.renansiqueira.claudelab.domain;

import java.util.List;

/**
 * Structured analysis of a backlog item.
 *
 * <p>This is the contract Claude must fill — no free text is returned outside
 * these fields. When the input is vague, the missing context is captured in
 * {@code risks} and {@code assumptions} instead of being invented.
 *
 * @param type               classification of the item
 * @param title              short, descriptive title
 * @param summary            concise summary of the item
 * @param priority           suggested priority
 * @param userStory          user story ("As a ..., I want ..., so that ...")
 * @param acceptanceCriteria specific, testable acceptance criteria
 * @param technicalTasks     concrete implementation tasks
 * @param risks              risks and open questions raised by the item
 * @param assumptions        assumptions made when the input lacked context
 */
public record BacklogAnalysisResponse(
        BacklogType type,
        String title,
        String summary,
        Priority priority,
        String userStory,
        List<String> acceptanceCriteria,
        List<String> technicalTasks,
        List<String> risks,
        List<String> assumptions
) {
}
