# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

An applied lab connecting the "Claude with the Anthropic API" course concepts to Java 21, Spring Boot
3.5 and Spring AI 1.1 (Anthropic / Claude). It is built **phase by phase**; each phase adds one
capability (chat → multi-turn → structured output → streaming → prompt templates → evals → tool use →
…). The roadmap and per-phase status live in `README.md`.

## Commands

```bash
mvn test                 # full test suite (runs offline — no API key needed)
mvn spring-boot:run      # start the app on :8080 (needs ANTHROPIC_API_KEY for real Claude calls)
mvn validate             # quick pom sanity check
mvn package              # build the jar

# run a single test class / method
mvn test -Dtest=ConversationMemoryServiceTest
mvn test -Dtest=BacklogControllerTest#blankInputReturns400
```

## Configuration & secrets

- The Anthropic key comes **only** from the `ANTHROPIC_API_KEY` env var. `application.yml` reads it as
  `${ANTHROPIC_API_KEY:not-set}` — the `not-set` placeholder lets the Spring context (and thus
  `mvn test`) start without a real key. Any real Claude call fails until a valid key is exported.
- Default model is `claude-opus-4-8`, set in `application.yml` (`spring.ai.anthropic.chat.options.model`).

## Architecture

Simple layered architecture under `com.renansiqueira.claudelab`; every layer package has a
`package-info.java` describing its role:

- `api` — controllers + request/response DTOs (records) + `GlobalExceptionHandler`.
- `ai` — Claude-backed services (the `ChatClient` lives here). `infra/ChatClientConfig` builds the
  `ChatClient` bean from Spring AI's auto-configured builder.
- `ai.prompt` — **versioned prompt templates** as classes (`BacklogPromptTemplate`,
  `ArchitecturePromptTemplate`, `ClaudePromptTemplates`); XML-tagged with few-shot examples.
- `domain` — framework-free model (enums `BacklogType`/`Priority`, the `BacklogAnalysisResponse`
  contract).
- `tools` — Spring beans with `@Tool` methods that Claude can call.
- `eval` — dataset-driven prompt evaluation (grading is pure; the runner calls Claude).
- `persistence` — JPA entities (`Project`, `BacklogItem`, `AcceptanceCriterion`, `TechnicalTask`) +
  Spring Data repositories; kept separate so `domain` stays framework-free.
- `application` — non-Claude use-case services (`ProjectService`, `BacklogService`) + response DTOs +
  `NotFoundException`/`ConflictException` (mapped to 404/409 in `GlobalExceptionHandler`).
- `rag`, `workflow`, `infra` — placeholders / config for later phases.

### Persistence (Phase 8)

- PostgreSQL + Spring Data JPA + **Flyway** (`src/main/resources/db/migration/`). `Project.id` is a
  human code (e.g. `brabrix-dev`) so it matches the agent's `projectId`; a migration seeds that
  project. `BacklogTool.createBacklogItem` now persists via `BacklogService`.
- Prod profile: Postgres datasource (env-var overridable), Flyway on, Hibernate `ddl-auto=validate`.
- `docker compose up -d postgres` starts the DB; `mvn spring-boot:run` applies migrations on startup.

### Key cross-file flows

- **Structured output**: `BacklogAnalysisService` uses `chatClient...entity(BacklogAnalysisResponse.class)`
  — Spring AI appends a JSON schema (incl. enum values) to the prompt and parses the reply into the
  record. The system prompt is `BacklogPromptTemplate.SYSTEM`, not an inline string.
- **Streaming**: `StreamingChatService` returns `Flux<String>` and `StreamingChatController` serves it
  as `text/event-stream`. Reactor comes transitively from Spring AI — **do not add the WebFlux
  starter**; Spring MVC streams `Flux` as SSE on its own.
- **Tool use**: `AgentBacklogService` registers tool beans via `.tools(projectContextTool, ...)` and
  Spring AI drives the call loop. Tools live entirely in `tools/`; controllers never contain tool
  logic. Each `@Tool` method logs when invoked.
- **Evaluation**: `PromptEvaluationService.grade(...)` is a pure, deterministic function (unit-tested
  with hand-built responses); `PromptEvaluationRunner` is the only eval piece that calls Claude. It
  loads `evals/datasets/*.json` and writes reports to `evals/results/` (git-ignored).

## Conventions to follow when adding a phase

- **Tests never hit the network and never need Docker.** Web layer: `@WebMvcTest(TheController.class)`
  + `@MockitoBean` for the service. Pure logic (memory, grading, tools, templates): plain JUnit,
  instantiate directly. Persistence: `@DataJpaTest` + `@ActiveProfiles("test")` +
  `@AutoConfigureTestDatabase(replace = NONE)` against in-memory H2 (PostgreSQL mode); import the
  service(s) under test. `@SpringBootTest contextLoads` uses `@ActiveProfiles("test")` (H2) and relies
  on the `not-set` key.
- **Validation**: request DTOs use Jakarta annotations (`@NotBlank`, `@DecimalMin/@DecimalMax`);
  `GlobalExceptionHandler` maps `MethodArgumentNotValidException` and `IllegalArgumentException` to
  `400 {"error": "..."}`. Reuse this contract.
- **Health phase marker**: `HealthController` returns `"phase": "phase-N"`; bump it each phase and
  update `HealthControllerTest`.
- **README is part of every phase** — update the roadmap checkbox, the health JSON, and add an endpoint
  example. Deeper rationale goes in `docs/` (`prompt-engineering.md`, `evals.md`, `tool-use.md`).
- Prefer records for DTOs and value types; keep prompts in `ai.prompt` classes (and unit-test their
  structure), not inline literals.

## Spring AI version note

Stays on the stable Spring AI 1.1.x line (Spring Boot 3.x). Spring AI 2.0 requires Spring Boot 4 +
Spring Framework 7 and is still pre-release — do not upgrade without an explicit decision (see git
history / README).
