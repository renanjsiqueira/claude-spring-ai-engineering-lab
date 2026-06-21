# Prompt Evaluation

How this lab measures the quality of the structured backlog output, so prompt changes can be judged
against evidence instead of vibes.

## Layout

```
evals/
├── datasets/   # input cases + expectations (versioned, in git)
│   ├── feature-request.json
│   ├── bug-report.json
│   ├── vague-request.json
│   ├── architecture-question.json
│   └── refactor-request.json
└── results/    # generated reports (git-ignored, folder kept via .gitkeep)
```

## Dataset format

Each file is a JSON array of cases. A case:

```json
{
  "input": "Quero criar uma funcionalidade para importar transações via CSV.",
  "expectedType": "FEATURE",
  "expectedMinimumCriteriaCount": 2,
  "expectedMinimumTasksCount": 2,
  "forbiddenTerms": ["blockchain", "TODO"]
}
```

| Field | Meaning |
|---|---|
| `input` | text fed to the backlog analyzer |
| `expectedType` | the `BacklogType` the model should return |
| `expectedMinimumCriteriaCount` | minimum acceptance criteria expected |
| `expectedMinimumTasksCount` | minimum technical tasks expected |
| `forbiddenTerms` | optional terms that must NOT appear anywhere in the output |

## Code-based grading

`PromptEvaluationService.grade(...)` is pure and deterministic — no LLM — so it is fully unit-tested.
For each case it checks:

1. **Valid JSON** — a structured response was produced (the call did not fail / return garbage).
2. **Correct type** — `type` matches `expectedType`.
3. **Has title** — `title` is present and non-blank.
4. **Minimum criteria** — `acceptanceCriteria.size() >= expectedMinimumCriteriaCount`.
5. **Minimum tasks** — `technicalTasks.size() >= expectedMinimumTasksCount`.
6. **No forbidden terms** — none of `forbiddenTerms` appears in any text field.

A case `passed` only when every check holds; otherwise `failures` explains what broke.

## Running the evals

`PromptEvaluationRunner` loads every dataset, calls `BacklogAnalysisService` per case (**real Claude
calls** — set `ANTHROPIC_API_KEY`), grades each, and writes a timestamped report to `evals/results/`.

Via the endpoint:

```bash
export ANTHROPIC_API_KEY="your-key"
mvn spring-boot:run
curl -X POST http://localhost:8080/api/evals/run
```

Response (aggregate + per-case breakdown):

```json
{
  "total": 5,
  "passed": 4,
  "failed": 1,
  "scores": [
    {
      "input": "...",
      "expectedType": "FEATURE",
      "actualType": "FEATURE",
      "validJson": true,
      "correctType": true,
      "hasTitle": true,
      "meetsCriteriaCount": true,
      "meetsTasksCount": true,
      "noForbiddenTerms": true,
      "passed": true,
      "failures": []
    }
  ]
}
```

The same report is saved as `evals/results/eval-result-<timestamp>.json`.

## Design notes

- **Grading is separate from running.** Grading is a pure function (unit-tested with hand-built
  responses); the runner is the only piece that touches Claude (exercised manually). This keeps the
  build fast and offline.
- **Expectations are intentionally loose for vague input.** The `vague-request` case expects
  `QUESTION` with zero required criteria/tasks — a vague ask should *not* be padded with invented
  detail, which aligns with the "do not invent business rules" prompt rule.
- **Forbidden terms catch hallucinated tech/markers** (e.g. `blockchain`, `TODO`).

## Optional / next steps

- **Model-based grading.** A second pass where Claude scores the output against a rubric (clarity,
  testability of acceptance criteria) would complement the structural checks here. Not yet
  implemented — the code-based checks come first because they are cheap, deterministic and CI-safe.
- **Threshold/CI gate.** A future step could fail CI when the pass rate drops below a threshold.
