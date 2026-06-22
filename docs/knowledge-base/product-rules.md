# Product Rules

- Every feature must deliver clear user value and have testable acceptance criteria agreed before
  development starts.
- Backlog items are prioritized by impact and urgency using one of: LOW, MEDIUM, HIGH, CRITICAL.
- A bug that causes data loss, corrupts transactions, or blocks login is always CRITICAL.
- Do not invent business rules. When a request is vague or context is missing, capture the open
  questions as assumptions and risks instead of guessing.
- Customer-facing changes require a short release note describing what changed and why.
- Importing customer or transaction data (for example via CSV) must be validated row by row and must
  never partially commit a broken file.
