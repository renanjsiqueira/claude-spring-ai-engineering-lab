# RAG — Retrieval-Augmented Generation

How the lab answers questions about the project from its own documentation.

## Pipeline

```
docs/knowledge-base/*.md
   │  DocumentIngestionService (startup)
   ▼
DocumentChunkingService  ──chunks──▶  DocumentSearchService (in-memory vector index)
                                              │  search(question, topK)
                                              ▼
                                      RagAnswerService ──context+question──▶ Claude ──▶ answer + sources
```

- **`DocumentChunkingService`** — splits each document into chunks: one per paragraph
  (blank-line separated), with any oversized paragraph cut into fixed-size windows.
- **`DocumentSearchService`** — a simple in-memory vector store. Each chunk is a term-frequency
  vector (a lexical "embedding"); queries are ranked by **cosine similarity**. Deterministic and
  fully offline.
- **`DocumentIngestionService`** — at startup, loads every `.md` under `docs/knowledge-base/`, chunks
  it, and indexes each chunk with its source file name. Tolerant of a missing directory.
- **`RagAnswerService`** — retrieves the top-K chunks; if none are relevant it returns a fixed
  "not enough information" message **without calling Claude**; otherwise it grounds Claude with the
  retrieved context and returns the answer plus the distinct source files.

## Why a lexical vector store (and not embeddings)

The Anthropic Spring AI starter does not ship an embedding model, and we want `mvn test` to stay
offline and deterministic. So the store uses lexical TF vectors + cosine similarity instead of neural
embeddings. It exposes the same shape (index + similarity search) as a Spring AI `VectorStore`, so it
can be swapped for `SimpleVectorStore` + an embedding model later without touching callers.

## Knowledge base

`docs/knowledge-base/`: `product-rules.md`, `architecture-guidelines.md`, `coding-standards.md`,
`domain-glossary.md`. Edit or add `.md` files there; they are ingested on the next startup.

Configuration (with defaults):

```
claudelab.rag.knowledge-base-dir = docs/knowledge-base
claudelab.rag.max-chunk-chars    = 1000
claudelab.rag.top-k              = 4
```

## Endpoint

`POST /api/rag/ask`

```bash
curl -X POST http://localhost:8080/api/rag/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "Quais padrões devo seguir para criar uma nova feature?"}'
```

```json
{
  "answer": "Model payloads as records, validate request bodies, write unit tests...",
  "sources": ["coding-standards.md", "architecture-guidelines.md"]
}
```

## Grounding rules

The system prompt instructs Claude to answer **only** from the provided context and to say it does
not have enough information when the context lacks the answer. The `sources` are the distinct file
names of the retrieved chunks — simple, file-level citations.

## Limitations / next steps

- Lexical retrieval matches on shared terms; it misses synonyms a neural embedding would catch.
  Swapping in a real `VectorStore` + embedding model is the natural upgrade.
- No re-ranking or chunk-overlap; chunking is paragraph-based.
- The "not enough information" gate triggers when no chunk shares any term with the question.
