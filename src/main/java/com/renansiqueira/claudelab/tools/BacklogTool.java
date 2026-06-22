package com.renansiqueira.claudelab.tools;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Tool that creates and persists backlog items.
 *
 * <p>Stored in memory for now; the contract is the same a database-backed
 * implementation would expose.
 */
@Component
public class BacklogTool {

    private static final Logger log = LoggerFactory.getLogger(BacklogTool.class);

    private final Map<String, BacklogItem> store = new ConcurrentHashMap<>();

    @Tool(description = "Create and persist a backlog item for a project. "
            + "Returns the created item including its generated id.")
    public BacklogItem createBacklogItem(
            @ToolParam(description = "Identifier of the project the item belongs to") String projectId,
            @ToolParam(description = "Short, descriptive title of the backlog item") String title,
            @ToolParam(description = "Detailed description of what needs to be done") String description) {
        String id = UUID.randomUUID().toString();
        BacklogItem item = new BacklogItem(id, projectId, title, description);
        store.put(id, item);
        log.info("Tool createBacklogItem called: id={} projectId={} title='{}'", id, projectId, title);
        return item;
    }

    /** Test/inspection helper: look up a stored item by id. */
    public Optional<BacklogItem> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    /** Test/inspection helper: number of stored items. */
    public int count() {
        return store.size();
    }

    /** Test/inspection helper: snapshot of stored items. */
    public List<BacklogItem> findAll() {
        return List.copyOf(store.values());
    }
}
