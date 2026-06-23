# claude-spring-ai-engineering-lab

> An applied, phase-by-phase lab that connects the concepts from the **"Claude with the Anthropic
> API"** course to **Java 21**, **Spring Boot** and **Spring AI** — from a single chat call all the
> way to RAG, MCP and agentic workflows.

A clean, didactic reference for **AI Engineering in Java**: each phase adds one capability, fully
tested, with the design decisions written down. Built as a public portfolio project.

---

## Goal

Most Claude / Anthropic examples are written in Python. This lab shows the same ideas in idiomatic
Java with Spring Boot and a simple, readable layered architecture — so the patterns (structured
output, prompt evaluation, tool use, RAG, MCP, workflows) are easy to follow and reuse in a real JVM
stack.

Principles followed throughout:

- One capability per phase, each ending green (`mvn test`) and documented.
- Tests never hit the network and never require Docker (Claude is mocked; persistence runs on H2).
- Secrets only ever come from the environment (`ANTHROPIC_API_KEY`).
- Prompts are versioned code, not scattered string literals.

---

## Mapping to the Anthropic course

| Course topic | Where it lives |
|---|---|
| Accessing Claude / first API call | Phase 1 — `POST /api/chat` |
| Multi-turn conversations | Phase 2 — `POST /api/chat/conversations` |
| System prompts | Phase 2 — `MultiTurnChatService` |
| Temperature | Phase 2 — `AnthropicChatOptions` |
| Structured output | Phase 3 — `POST /api/backlog/analyze` |
| Response streaming | Phase 4 — `GET /api/chat/stream` (SSE) |
| Prompt engineering with XML tags | Phase 5 — `ai.prompt` templates |
| Prompt evaluation | Phase 6 — `POST /api/evals/run`, `evals/` |
| Tool use | Phase 7 — `POST /api/agent/backlog` |
| RAG | Phase 9 — `POST /api/rag/ask` |
| MCP | Phase 10 — `mcp-server/` (experimental) |
| Agents & workflows | Phase 11 — `POST /api/workflows/analyze` |

Phase 8 (PostgreSQL persistence) is an applied extension, not a course topic.

---

## Tech stack

- Java 21
- Spring Boot 3.5
- Spring AI 1.1 (Anthropic / Claude) — default model `claude-opus-4-8`
- Spring Data JPA + PostgreSQL + Flyway
- Maven, Docker Compose

> Spring AI stays on the stable **1.1.x** line (Spring Boot 3.x). Spring AI 2.0 requires Spring Boot
> 4 + Spring Framework 7 and is still pre-release — see git history for that decision.

---

## Architecture

A simple layered architecture under `com.renansiqueira.claudelab`; each layer has a `package-info.java`
documenting its role.

| Layer | Responsibility |
|---|---|
| `api` | HTTP boundary: controllers, request/response DTOs, `GlobalExceptionHandler` |
| `application` | Use-case services (projects, backlog) + response DTOs + domain exceptions |
| `ai` | Claude integration via Spring AI (`ChatClient`); chat, structured, streaming, tool agent |
| `ai.prompt` | Versioned prompt templates (XML tags + few-shot) |
| `domain` | Framework-free model: enums and the backlog contract |
| `tools` | Spring beans exposed to Claude as `@Tool`s |
| `rag` | Retrieval-augmented generation over `docs/knowledge-base/` |
| `eval` | Dataset-driven prompt evaluation (code-based grading) |
| `workflow` | Agentic workflows: chaining, routing, parallel-review |
| `persistence` | JPA entities + Spring Data repositories |
| `infra` | Cross-cutting config (`ChatClientConfig`) |

Cross-cutting docs:

- **Prompt engineering** — [`docs/prompt-engineering.md`](docs/prompt-engineering.md)
- **Prompt evaluation** — [`docs/evals.md`](docs/evals.md)
- **Tool use** — [`docs/tool-use.md`](docs/tool-use.md)
- **RAG** — [`docs/rag.md`](docs/rag.md)
- **MCP server (experimental)** — [`docs/mcp.md`](docs/mcp.md)
- **Agents & workflows** — [`docs/agents-and-workflows.md`](docs/agents-and-workflows.md)
- **Repo guide for Claude Code** — [`CLAUDE.md`](CLAUDE.md)

The experimental **MCP server** lives in a separate module, [`mcp-server/`](mcp-server/), with its own
build (not part of the root build) so it can never affect the core app.

---

## Run locally

### Prerequisites

- Java 21
- Maven 3.9+
- Docker + Docker Compose (for PostgreSQL)

### 1. Start the database

```bash
docker compose up -d postgres
```

PostgreSQL starts on `localhost:5432` (db/user/password all `claudelab`). On app startup, **Flyway**
applies the migrations in `src/main/resources/db/migration/` (schema + a seeded `devbacklog-ai-assistant`
project) and Hibernate validates the entities against that schema.

### 2. Provide your Anthropic key

```bash
export ANTHROPIC_API_KEY="your-key-here"
```

### 3. Run

```bash
mvn spring-boot:run
curl http://localhost:8080/api/health
# { "status": "UP", "service": "claude-spring-ai-engineering-lab", "phase": "phase-11" }
```

### Test

```bash
mvn test          # offline: no Docker, no API key (Claude mocked, H2 for persistence)
mvn test -Dtest=BacklogPersistenceTest          # a single class
mvn test -Dtest=ChainingWorkflowTest#runsStepsInOrderAndReturnsReviewAsFinal   # a single method
```

If `ANTHROPIC_API_KEY` is unset the context still starts (using a non-functional `not-set`
placeholder), so tests pass without a key — but any real Claude call fails until a valid key is set.

---

## Environment variables

| Variable | Required | Default | Purpose |
|---|---|---|---|
| `ANTHROPIC_API_KEY` | for real Claude calls | `not-set` | Anthropic API key (never hard-coded) |
| `SPRING_DATASOURCE_URL` | no | `jdbc:postgresql://localhost:5432/claudelab` | Postgres JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | no | `claudelab` | DB user |
| `SPRING_DATASOURCE_PASSWORD` | no | `claudelab` | DB password |
| `CLAUDELAB_MCP_KNOWLEDGE_BASE_DIR` | no | `../docs/knowledge-base` | MCP server's docs directory |

Application properties (in `application.yml`, override via env/CLI):

| Property | Default | Purpose |
|---|---|---|
| `spring.ai.anthropic.chat.options.model` | `claude-opus-4-8` | Claude model |
| `claudelab.chat.max-history-messages` | `20` | Multi-turn history cap |
| `claudelab.rag.knowledge-base-dir` | `docs/knowledge-base` | RAG documents |
| `claudelab.rag.top-k` | `4` | RAG chunks retrieved |
| `claudelab.evals.datasets-dir` | `evals/datasets` | Eval datasets |

---

## API examples

### `POST /api/chat` — single message

```bash
curl -X POST http://localhost:8080/api/chat -H "Content-Type: application/json" \
  -d '{"message": "Explain what Spring AI is in one paragraph"}'
# { "content": "..." }
```

### `GET /api/chat/stream` — streaming (SSE)

```bash
curl -N "http://localhost:8080/api/chat/stream?message=Explique%20streaming%20de%20LLM"
# data: chunk 1\n\n data: chunk 2 ...
```

### `POST /api/chat/conversations` — multi-turn (system prompt + temperature)

```bash
curl -X POST http://localhost:8080/api/chat/conversations -H "Content-Type: application/json" \
  -d '{"message": "Help me write acceptance criteria for a login feature", "temperature": 0.2}'
# { "conversationId": "3f9c...", "content": "..." }   (reuse conversationId to continue)
```

### `POST /api/backlog/analyze` — structured output

```bash
curl -X POST http://localhost:8080/api/backlog/analyze -H "Content-Type: application/json" \
  -d '{"input": "Quero criar uma funcionalidade para importar transações via CSV"}'
```

```json
{
  "type": "FEATURE", "title": "CSV Transaction Import", "summary": "...", "priority": "HIGH",
  "userStory": "As a user, I want to import transactions via CSV, so that I avoid manual entry.",
  "acceptanceCriteria": ["Given a valid CSV, when uploaded, then transactions are persisted."],
  "technicalTasks": ["Add CSV parser", "Validate rows"],
  "risks": ["CSV column format is unspecified"], "assumptions": ["Assuming UTF-8 encoding"]
}
```

### `POST /api/agent/backlog` — tool use (persists to Postgres)

```bash
curl -X POST http://localhost:8080/api/agent/backlog -H "Content-Type: application/json" \
  -d '{"projectId": "devbacklog-ai-assistant", "message": "Crie uma tarefa para importar clientes via CSV"}'
```

### `POST /api/rag/ask` — RAG with citations

```bash
curl -X POST http://localhost:8080/api/rag/ask -H "Content-Type: application/json" \
  -d '{"question": "Quais padrões devo seguir para criar uma nova feature?"}'
# { "answer": "...", "sources": ["coding-standards.md", "architecture-guidelines.md"] }
```

### `POST /api/workflows/analyze` — agentic workflows

```bash
curl -X POST http://localhost:8080/api/workflows/analyze -H "Content-Type: application/json" \
  -d '{"input": "Precisamos criar importação CSV com validação e processamento assíncrono"}'
# { "workflowType": "CHAINING", "finalAnalysis": "...", "steps": [...] }
```

### Projects & backlog (persistence)

```bash
curl -X POST http://localhost:8080/api/projects -H "Content-Type: application/json" \
  -d '{"id": "devbacklog-ai-assistant", "name": "DevBacklog AI Assistant", "description": "AI backlog assistant"}'
curl http://localhost:8080/api/projects/devbacklog-ai-assistant
curl http://localhost:8080/api/projects/devbacklog-ai-assistant/backlog
curl http://localhost:8080/api/backlog/{id}
```

### `POST /api/evals/run` — prompt evaluation

```bash
curl -X POST http://localhost:8080/api/evals/run
# { "total": 5, "passed": 4, "failed": 1, "scores": [ ... ] }
```

Validation and error responses are uniform: `{"error": "..."}` with `400` (validation), `404`
(not found), `409` (conflict).

---

## Implemented phases

- [x] **Phase 0 — Foundation**: skeleton, layered packages, health endpoint, tests.
- [x] **Phase 1 — First Claude call**: `POST /api/chat` via Spring AI.
- [x] **Phase 2 — System prompt, temperature & multi-turn**: `POST /api/chat/conversations`.
- [x] **Phase 3 — Structured output**: `POST /api/backlog/analyze`.
- [x] **Phase 4 — Response streaming**: `GET /api/chat/stream` (SSE).
- [x] **Phase 5 — Prompt engineering with XML tags**: versioned templates + few-shot.
- [x] **Phase 6 — Prompt evaluation**: dataset-driven code-based grading.
- [x] **Phase 7 — Tool use**: Claude calls Spring services as tools.
- [x] **Phase 8 — Persistence**: Spring Data JPA + PostgreSQL + Flyway.
- [x] **Phase 9 — RAG**: retrieve from the knowledge base and answer with citations.
- [x] **Phase 10 — MCP (experimental)**: standalone MCP server (`mcp-server/`).
- [x] **Phase 11 — Agents and workflows**: chaining, routing & parallel-review.
- [x] **Phase 12 — Documentation & article**: this README + [`docs/linkedin-article.md`](docs/linkedin-article.md).

---

## Example responses

The persistence path was verified end-to-end against real PostgreSQL (Flyway migrations applied,
Hibernate `validate` passed):

```bash
$ curl -s http://localhost:8080/api/projects/devbacklog-ai-assistant
{ "id":"devbacklog-ai-assistant", "name":"DevBacklog AI Assistant",
  "description":"Turns ideas, bugs and feature requests into structured engineering backlog items.",
  "createdAt":"2026-06-22T00:27:59.324203Z" }
```

```
# application logs during POST /api/agent/backlog show the tool calls:
Tool getProjectContext called for projectId=devbacklog-ai-assistant
Tool estimateComplexity called -> HIGH
Tool createBacklogItem persisted id=... projectId=devbacklog-ai-assistant title='Import customers via CSV'
```

> Screenshots can be added here; the project is API-only, so the responses above are the primary
> artifacts.

---

## Next steps

- Swap the lexical RAG index for a real Spring AI `VectorStore` + embedding model.
- Add model-based grading to the evaluation suite (an LLM judge alongside the code-based checks) and a
  CI gate on the pass rate.
- Human-in-the-loop approval before side-effecting tools (e.g. persisting a backlog item).
- A bounded executor for the parallel workflow and richer, open-ended agents.
- Wire the MCP server's tools to the core app's HTTP API instead of in-memory copies.
- Revisit Spring AI 2.0 / Spring Boot 4 once GA.

---

## Lessons learned

- **Structured output is the unlock for "LLM as a backend".** `.entity(Type)` turns Claude into a
  typed function — the rest of the system stays strongly typed and testable.
- **Treat prompts as code.** Versioned template classes with XML tags and few-shot examples are
  reviewable, diffable and unit-testable; inline strings rot.
- **Evaluate prompts like any other behavior.** Cheap, deterministic code-based checks over a dataset
  catch regressions long before a human notices.
- **Keep tests offline.** Mocking the LLM (and using H2 for persistence) kept the whole suite fast and
  Docker-free, which made every phase safe to iterate on.
- **Tool use changes the architecture.** The model becomes an orchestrator of *your* services — so
  small, well-described tools and a clean controller/service/tool split matter more, not less.
- **An abstraction seam pays off for agents.** Putting workflows behind a one-method `LlmStep` made
  multi-call orchestration (chaining, routing, parallel) unit-testable without a single API call.
- **Verify the real path when you can.** The persistence phase was confirmed against actual Postgres,
  not just H2 — which caught nothing this time, but is exactly how you'd catch a Flyway/JPA drift.

---

## License

Licensed under the [Apache License, Version 2.0](LICENSE).

```
Copyright 2026 Renan Siqueira

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
