# Tool Use

How this lab lets Claude call Spring services as tools.

## Idea

Claude doesn't run our code — it *requests* tool calls, and Spring AI executes them and feeds the
results back, looping until Claude produces a final answer. We expose plain Spring beans whose
methods are annotated with `@Tool`; the method signature + descriptions become the tool schema the
model sees.

## The tools (`com.renansiqueira.claudelab.tools`)

| Tool | Method | Returns | Backing |
|---|---|---|---|
| `ProjectContextTool` | `getProjectContext(projectId)` | name, description, stack | in-memory registry |
| `BacklogTool` | `createBacklogItem(projectId, title, description)` | created item with id | in-memory store |
| `ComplexityTool` | `estimateComplexity(description)` | `LOW`/`MEDIUM`/`HIGH` + justification | deterministic heuristic |

Each method is small and single-purpose, and each parameter has a `@ToolParam` description so the
schema is self-explanatory to the model:

```java
@Tool(description = "Get the context of a project: its name, description and technology stack. "
        + "Call this before creating a backlog item so the item fits the project.")
public ProjectContext getProjectContext(
        @ToolParam(description = "The project identifier, e.g. 'brabrix-dev'") String projectId) { ... }
```

Every tool logs when it is invoked (`Tool getProjectContext called for projectId=...`), so you can see
the call sequence in the application logs.

## Wiring (`AgentBacklogService`)

The service registers the tools on the `ChatClient` and lets Spring AI drive the loop:

```java
chatClient.prompt()
    .system(SYSTEM_PROMPT)
    .user("Project: " + projectId + "\n\nRequest: " + message)
    .tools(projectContextTool, backlogTool, complexityTool)
    .call()
    .content();
```

The system prompt describes the available tools and the intended workflow (get context → estimate
complexity → create item → reply in plain language). Claude decides which tools to call and when.

## Endpoint

`POST /api/agent/backlog`

```bash
curl -X POST http://localhost:8080/api/agent/backlog \
  -H "Content-Type: application/json" \
  -d '{"projectId": "brabrix-dev", "message": "Crie uma tarefa para importar clientes via CSV"}'
```

```json
{ "content": "Created the backlog item 'Import customers via CSV' for Brabrix (estimated complexity: HIGH)." }
```

While it runs, the logs show the tool calls, e.g.:

```
Tool getProjectContext called for projectId=brabrix-dev
Tool estimateComplexity called -> HIGH
Tool createBacklogItem called: id=... projectId=brabrix-dev title='Import customers via CSV'
```

## Design rules followed

- **Small, well-described tools.** One responsibility each; descriptions on the tool and on every
  parameter.
- **No controller/tool mixing.** The controller only validates input and delegates to
  `AgentBacklogService`; tool logic lives entirely in the `tools` package.
- **Tools are unit-tested directly** (no LLM needed) — `ProjectContextToolTest`, `BacklogToolTest`,
  `ComplexityToolTest`.

## Limitations / next steps

- Storage is in-memory (lost on restart); a real repository can replace `BacklogTool`'s store without
  changing the tool contract.
- `ComplexityTool` uses a keyword/length heuristic — illustrative, not production-grade.
- No human-in-the-loop approval before `createBacklogItem` persists; a future phase could gate
  side-effecting tools.
