# Coding Standards

- When creating a new feature, follow these standards: model request and response payloads as Java
  records, validate request bodies with Jakarta Bean Validation, and return clear, structured error
  responses.
- Every new service must have unit tests. Tests must not call external APIs and must not require
  Docker — use mocks for Claude and in-memory H2 for persistence.
- Prefer constructor injection. Keep methods small, single-purpose and clearly named.
- Prompts live in versioned template classes (the ai.prompt package), never as inline string
  literals scattered across services.
- Do not put secrets in code or configuration. The Anthropic key is read only from the
  ANTHROPIC_API_KEY environment variable.
- Keep controllers thin: validation and delegation only. Business logic lives in services; tool logic
  lives in the tools package.
