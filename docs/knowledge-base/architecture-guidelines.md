# Architecture Guidelines

- The system uses a simple layered architecture: api, application, ai, domain, tools, rag, eval,
  persistence and infra. Dependencies point inward; the domain layer stays framework-free.
- When creating a new feature, add a controller in the api layer, an orchestration service in the
  application layer, and keep domain types (records, enums) free of framework annotations.
- Persistence uses Spring Data JPA with PostgreSQL. Every schema change goes through a Flyway
  migration in `src/main/resources/db/migration`; never edit the schema by hand.
- Claude integration goes through Spring AI in the ai layer. Controllers must never call the model
  directly — they delegate to a service.
- Prefer simple, proven designs over novelty. Call out scalability limits and failure modes when they
  matter, and avoid premature abstraction.
- Long-running or external calls (model calls, imports) should be resilient: validate input, handle
  partial failure, and surface friendly errors.
