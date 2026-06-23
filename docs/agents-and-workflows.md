# Agents & Workflows

Three classic agentic workflow patterns built on top of the backlog assistant, in the `workflow`
package. They are exposed through one endpoint and share a common shape.

## Shared design

- **`EngineeringWorkflow`** — interface (`type()` + `run(input)`), one implementation per pattern.
- **`LlmStep`** — a one-call abstraction (`complete(systemPrompt, userPrompt)`). Workflows depend on
  this, not on `ChatClient` directly, so their orchestration is unit-tested by mocking the step (no
  Claude needed). The production implementation is `ChatClientLlmStep`.
- **`WorkflowResult`** — `type`, `finalAnalysis`, and ordered `steps` (`name` + `output`).
- **`WorkflowService`** — dispatches to the requested workflow (default `CHAINING`).

## 1. ChainingWorkflow (sequential)

Each step's output feeds the next:

```
classify ─▶ draft backlog item ─▶ expand acceptance criteria ─▶ review final
```

`finalAnalysis` is the reviewed result. Use when the task is a single pipeline where later steps
depend on earlier ones.

## 2. RoutingWorkflow (dispatch by type)

```
classify ─▶ FEATURE  → feature prompt
            BUG      → bug prompt
            ARCHITECTURE → architecture prompt
            REFACTOR → refactor prompt
```

Classifies the input, then runs a prompt specialized for that type. Unclear classification defaults
to FEATURE. Use when different input kinds deserve genuinely different handling.

## 3. ParallelReviewWorkflow (fan-out + merge)

```
          ┌─ product analysis ─┐
input ─▶  ├─ technical analysis ┤ ─▶ merge ─▶ final analysis
          ├─ risk analysis ─────┤
          └─ test analysis ─────┘
```

Runs four independent analyses concurrently (`CompletableFuture`), then merges them. Use when several
independent perspectives should be gathered fast and combined.

## Endpoint

`POST /api/workflows/analyze`

```bash
curl -X POST http://localhost:8080/api/workflows/analyze \
  -H "Content-Type: application/json" \
  -d '{"input": "Precisamos criar importação CSV com validação e processamento assíncrono"}'
```

```json
{
  "workflowType": "CHAINING",
  "finalAnalysis": "...",
  "steps": [
    { "name": "classify", "output": "FEATURE" },
    { "name": "backlog-item", "output": "..." },
    { "name": "acceptance-criteria", "output": "..." },
    { "name": "review", "output": "..." }
  ]
}
```

Select a workflow with an optional `workflow` field (`CHAINING` | `ROUTING` | `PARALLEL`,
case-insensitive); it defaults to `CHAINING`. An unknown value returns `400`, a blank `input` returns
`400`.

```bash
curl -X POST http://localhost:8080/api/workflows/analyze \
  -H "Content-Type: application/json" \
  -d '{"input": "App crashes when the token expires", "workflow": "routing"}'
```

## Testing

Each workflow is unit-tested with a mocked `LlmStep` — the chaining order, the routing decision, and
the parallel fan-out + merge are all verified without any model call. The parallel test keys its mock
on the system prompt so it is independent of (nondeterministic) execution order.

## Limitations / next steps

- Workflows are fixed pipelines, not open-ended agents that choose their own trajectory.
- No shared memory between workflow runs; each call is independent.
- The parallel workflow uses the common `ForkJoinPool`; a dedicated bounded executor would be the next
  step for production load.
