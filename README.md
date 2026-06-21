# claude-spring-ai-engineering-lab

An applied lab that connects the concepts from the **"Claude with the Anthropic API"** course
to **Java 21**, **Spring Boot** and **Spring AI**.

The goal is to build, **phase by phase**, a clean and didactic reference project that demonstrates
how to engineer real applications on top of Claude — from a single chat call all the way to agents
and workflows.

> Status: **Phase 0 — Foundation** ✅

## Why this project

Most Claude / Anthropic examples are written in Python. This lab shows the same ideas in idiomatic
Java with Spring Boot, with a simple layered architecture that is easy to read and good as a public
portfolio piece.

## Tech stack

- Java 21
- Spring Boot 3.4
- Spring AI (Anthropic) — added in Phase 1
- Maven

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

## Roadmap (phases)

The project is built incrementally. Each phase is small, tested, and ends with a suggested commit.

- [x] **Phase 0 — Foundation**: project skeleton, layered packages, health endpoint, tests, CI-friendly build.
- [ ] **Phase 1 — First Claude call**: integrate Claude via Spring AI (`ANTHROPIC_API_KEY`).
- [ ] **Phase 2 — Multi-turn conversations**
- [ ] **Phase 3 — System prompts**
- [ ] **Phase 4 — Temperature**
- [ ] **Phase 5 — Response streaming**
- [ ] **Phase 6 — Structured output**
- [ ] **Phase 7 — Prompt engineering with XML tags**
- [ ] **Phase 8 — Prompt evaluation**
- [ ] **Phase 9 — Tool use**
- [ ] **Phase 10 — RAG**
- [ ] **Phase 11 — MCP**
- [ ] **Phase 12 — Agents and workflows**

## Getting started

### Prerequisites

- Java 21
- Maven 3.9+

### Configuration

The Anthropic API key is **never** stored in the code or in version control. From Phase 1 onward
the application reads it from an environment variable:

```bash
export ANTHROPIC_API_KEY="your-key-here"
```

Phase 0 does not call Claude, so no key is required yet.

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
{ "status": "UP", "service": "claude-spring-ai-engineering-lab", "phase": "phase-0" }
```

## License

To be defined.
