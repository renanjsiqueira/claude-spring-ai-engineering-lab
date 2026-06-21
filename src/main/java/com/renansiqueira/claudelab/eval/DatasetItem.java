package com.renansiqueira.claudelab.eval;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.renansiqueira.claudelab.domain.BacklogType;
import java.util.List;

/**
 * One evaluation case loaded from a dataset file.
 *
 * @param input                        the raw text fed to the backlog analyzer
 * @param expectedType                 the {@link BacklogType} the model should return
 * @param expectedMinimumCriteriaCount minimum number of acceptance criteria expected
 * @param expectedMinimumTasksCount    minimum number of technical tasks expected
 * @param forbiddenTerms               terms that must NOT appear in the output (optional)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DatasetItem(
        String input,
        BacklogType expectedType,
        int expectedMinimumCriteriaCount,
        int expectedMinimumTasksCount,
        List<String> forbiddenTerms
) {
}
