# Prompt Engineering Notes

Living notes on how this lab prompts Claude. Each phase that touches prompting adds a section here.

## Principles

- **Constrain the output, not just the wording.** When the answer must be machine-readable, use
  structured output so the model fills a contract instead of producing prose we have to parse.
- **State what *not* to do.** Explicit "do not invent / do not guess" rules reduce hallucinated
  business logic far more than positive instructions alone.
- **Make vagueness visible.** When the input lacks context, the model should surface that
  (assumptions / risks) rather than silently filling the gap.
- **Keep the persona stable.** System prompts define the role; per-request data goes in the user
  turn.

## Phase 3 — Structured backlog analysis

**Goal:** turn a raw idea, bug or feature request into a structured backlog item — no free text
outside the contract.

### How structured output works here

We use Spring AI's `ChatClient` structured output:

```java
chatClient.prompt()
    .system(SYSTEM_PROMPT)
    .user(input)
    .options(AnthropicChatOptions.builder().temperature(0.2).build())
    .call()
    .entity(BacklogAnalysisResponse.class);
```

`.entity(Class)` uses a `BeanOutputConverter` that:
1. generates a JSON schema from the target record (including enum values for `BacklogType` /
   `Priority`),
2. appends that schema as format instructions to the prompt, and
3. parses Claude's reply straight into the record.

So the model is told *exactly* which fields and enum values are allowed.

### The system prompt (rules encoded)

The prompt (`BacklogAnalysisService.SYSTEM_PROMPT`) encodes the phase's rules:

- Classify `type` and set `priority` from impact/urgency.
- **Do not invent** business rules, numbers or constraints the input does not state.
- When the input is vague, record the unknowns in `assumptions` and the resulting `risks` instead
  of guessing.
- `acceptanceCriteria` must be **specific and testable** (pass/fail), preferring Given/When/Then.
- Respond **only** with the structured data — no commentary.

### Why temperature 0.2

Backlog analysis should be stable and repeatable for the same input, so we keep temperature low.
This is not creative writing; determinism matters more than variety.

### Design choices

- Added an `assumptions` field (beyond the original example contract) because the rules explicitly
  want a place to record missing context separate from `risks`.
- The contract lives in `domain` (`BacklogAnalysisResponse`, `BacklogType`, `Priority`) so it is
  reusable beyond the HTTP layer.

### Known limitations / next steps

- The schema constrains *shape*, not *quality*: testable acceptance criteria are requested in prose,
  not enforced. Phase "Prompt evaluation" will add graders to measure this.
- No retry/repair loop yet if the model returns invalid JSON — a parse failure surfaces as a 500.
