# claude-spring-ai-engineering-lab

An applied lab that connects the concepts from the **"Claude with the Anthropic API"** course
to **Java 21**, **Spring Boot** and **Spring AI**.

The goal is to build, **phase by phase**, a clean and didactic reference project that demonstrates
how to engineer real applications on top of Claude — from a single chat call all the way to agents
and workflows.

> Status: **Phase 6 — Prompt evaluation** ✅

## Why this project

Most Claude / Anthropic examples are written in Python. This lab shows the same ideas in idiomatic
Java with Spring Boot, with a simple layered architecture that is easy to read and good as a public
portfolio piece.

## Tech stack

- Java 21
- Spring Boot 3.5
- Spring AI 1.1 (Anthropic / Claude)
- Maven

Default model: `claude-opus-4-8` (configurable in `application.yml`).

## Architecture

A simple, readable layered architecture. Each layer is a package under
`com.renansiqueira.claudelab` and is documented with a `package-info.java`:

| Layer         | Responsibility                                              |
|---------------|------------------------------------------------------------|
| `api`         | HTTP boundary: controllers and request/response DTOs       |
| `application` | Use cases / orchestration services                         |
| `ai`          | Claude integration via Spring AI                           |
| `domain`      | Core model: entities, value objects, domain types          |
| `tools`       | Tool / function definitions Claude can call                |
| `rag`         | Retrieval-augmented generation                             |
| `eval`        | Prompt and output evaluation                               |
| `workflow`    | Agents and multi-step workflows                            |
| `infra`       | Cross-cutting infrastructure and configuration             |

## Prompt engineering

Prompts are treated as versioned product artifacts, not inline string literals. They live in
`com.renansiqueira.claudelab.ai.prompt` (`BacklogPromptTemplate`, `ArchitecturePromptTemplate`,
aggregated by `ClaudePromptTemplates`), use **XML tags** (`<role>`, `<context>`, `<task>`, `<rules>`,
`<examples>`) to make structure explicit, and include **few-shot examples**. They are unit-tested so
a careless edit fails the build. See [`docs/prompt-engineering.md`](docs/prompt-engineering.md) for
the rationale and per-phase notes.

## Roadmap (phases)

The project is built incrementally. Each phase is small, tested, and ends with a suggested commit.

- [x] **Phase 0 — Foundation**: project skeleton, layered packages, health endpoint, tests, CI-friendly build.
- [x] **Phase 1 — First Claude call**: integrate Claude via Spring AI (`POST /api/chat`, `ANTHROPIC_API_KEY`).
- [x] **Phase 2 — System prompt, temperature & multi-turn**: `POST /api/chat/conversations` with bounded per-conversation history.
- [x] **Phase 3 — Structured output**: `POST /api/backlog/analyze` returns a typed backlog item (no free text outside the contract).
- [x] **Phase 4 — Response streaming**: `GET /api/chat/stream` streams chunks as Server-Sent Events.
- [x] **Phase 5 — Prompt engineering with XML tags**: versioned prompt templates (`ai.prompt`) with XML tags and few-shot examples.
- [x] **Phase 6 — Prompt evaluation**: dataset-driven code-based grading (`POST /api/evals/run`), reports in `evals/`.
- [ ] **Phase 7 — Tool use**
- [ ] **Phase 8 — RAG**
- [ ] **Phase 9 — MCP**
- [ ] **Phase 10 — Agents and workflows**

> Note: phases are grouped/ordered as they are actually built, so the numbering
> can differ from the original course topic order. See
> [`docs/prompt-engineering.md`](docs/prompt-engineering.md) for prompting notes.

## Getting started

### Prerequisites

- Java 21
- Maven 3.9+

### Configuration

The Anthropic API key is **never** stored in the code or in version control. The application reads
it from an environment variable:

```bash
export ANTHROPIC_API_KEY="your-key-here"
```

If the variable is not set, the context still starts (using a non-functional `not-set` placeholder)
so `mvn test` works without a key — but any real call to `/api/chat` will fail until a valid key is
exported.

### Build and test

```bash
mvn test
```

### Run

```bash
mvn spring-boot:run
```

Then check the health endpoint:

```bash
curl http://localhost:8080/api/health
```

Expected response:

```json
{ "status": "UP", "service": "claude-spring-ai-engineering-lab", "phase": "phase-6" }
```

## Endpoints

### `POST /api/chat` — send a message to Claude

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Explain what Spring AI is in one paragraph"}'
```

Response:

```json
{ "content": "..." }
```

A blank `message` returns `400`:

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": ""}'
```

```json
{ "error": "message must not be blank" }
```

### `GET /api/chat/stream` — streaming chat (Server-Sent Events)

Streams Claude's answer incrementally as `text/event-stream`, separate from the synchronous
`/api/chat` endpoint. Use `curl -N` to disable buffering and see chunks as they arrive:

```bash
curl -N "http://localhost:8080/api/chat/stream?message=Explique%20como%20desenhar%20um%20sistema%20de%20importacao%20CSV%20resiliente"
```

Output (one `data:` event per chunk):

```
data:Para

data: desenhar

data:  um sistema

...
```

A blank `message` returns `400`:

```json
{ "error": "message must not be blank" }
```

If the stream fails mid-response, a friendly final chunk is emitted instead of a raw error.

### `POST /api/chat/conversations` — multi-turn chat

Adds a fixed **system prompt** (a senior software-engineering assistant), configurable
**temperature**, and per-conversation **memory**.

- `conversationId` (optional): omit it to start a new conversation; the response returns the
  generated id so you can continue the thread.
- `message` (required): the user message.
- `temperature` (optional): between `0.0` and `1.0`; defaults to `0.2`.

History is kept in memory and bounded to the most recent `claudelab.chat.max-history-messages`
messages (default `20`), so it never grows without limit.

First turn (no `conversationId`):

```bash
curl -X POST http://localhost:8080/api/chat/conversations \
  -H "Content-Type: application/json" \
  -d '{"message": "Help me write acceptance criteria for a login feature", "temperature": 0.2}'
```

```json
{ "conversationId": "3f9c...", "content": "..." }
```

Continue the same conversation (reuse the returned `conversationId`):

```bash
curl -X POST http://localhost:8080/api/chat/conversations \
  -H "Content-Type: application/json" \
  -d '{"conversationId": "3f9c...", "message": "Now add edge cases for rate limiting"}'
```

A `temperature` outside `[0.0, 1.0]` returns `400`:

```json
{ "error": "temperature must be between 0.0 and 1.0" }
```

### `POST /api/backlog/analyze` — structured backlog analysis

Takes a raw idea, bug or feature request and returns a **structured** backlog item — no free text
outside the contract. Built with Spring AI structured output (see
[`docs/prompt-engineering.md`](docs/prompt-engineering.md)).

```bash
curl -X POST http://localhost:8080/api/backlog/analyze \
  -H "Content-Type: application/json" \
  -d '{"input": "Quero criar uma funcionalidade para importar transações via CSV"}'
```

```json
{
  "type": "FEATURE",
  "title": "CSV Transaction Import",
  "summary": "...",
  "priority": "HIGH",
  "userStory": "As a user, I want to import transactions via CSV, so that I avoid manual entry.",
  "acceptanceCriteria": ["Given a valid CSV, when uploaded, then transactions are persisted."],
  "technicalTasks": ["Add CSV parser", "Validate rows"],
  "risks": ["CSV column format is unspecified"],
  "assumptions": ["Assuming UTF-8 encoding"]
}
```

- `type`: `FEATURE` | `BUG` | `REFACTOR` | `ARCHITECTURE` | `QUESTION`
- `priority`: `LOW` | `MEDIUM` | `HIGH` | `CRITICAL`
- When the input is vague, missing context is captured in `risks` / `assumptions` instead of being
  invented.

A blank `input` returns `400`:

```json
{ "error": "input must not be blank" }
```

### `POST /api/evals/run` — run the prompt evaluation suite

Runs every dataset under `evals/datasets/` through the backlog analyzer, grades each case with
deterministic code-based checks, and returns a pass/fail report (also saved to `evals/results/`).
This makes **real Claude calls**, so it is a manual tool — see [`docs/evals.md`](docs/evals.md).

```bash
curl -X POST http://localhost:8080/api/evals/run
```

```json
{ "total": 5, "passed": 4, "failed": 1, "scores": [ /* per-case breakdown */ ] }
```

Each case checks: valid JSON, correct type, has a title, minimum acceptance criteria, minimum
technical tasks, and absence of forbidden terms.

## License

Licensed under the [Apache License, Version 2.0](LICENSE).

```
Copyright 2026 Renan Siqueira

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
