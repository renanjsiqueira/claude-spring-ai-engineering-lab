# MCP Server (experimental)

An **experimental** Model Context Protocol (MCP) server that exposes a few of the lab's capabilities
to any MCP client (e.g. Claude Desktop). It lives in its own module, `mcp-server/`, and is
**completely separate from the core application** — its own `pom.xml`, not part of the root build, so
it can never affect the main app or `mvn test`.

> Status: experimental. Built with Spring AI 1.1's MCP server starter
> (`spring-ai-starter-mcp-server`, STDIO transport) and annotation-based registration.

## What it exposes

**Tools**
- `search_project_docs` — keyword search over the knowledge base, returns snippets + sources.
- `get_project_context` — a project's name/description/stack by id.
- `create_backlog_item` — creates a backlog item (in-memory in this experimental server).
- `estimate_task_complexity` — LOW/MEDIUM/HIGH estimate with a short justification.

**Resources**
- `project://devbacklog-ai-assistant/context`
- `docs://architecture-guidelines`
- `docs://coding-standards`

**Prompts**
- `generate_backlog_item` — template to turn a request into a structured backlog item.
- `review_architecture_decision` — template to review an architecture decision.

## Build & run

```bash
# Build the server jar
mvn -f mcp-server/pom.xml clean package

# Run it directly (STDIO; normally an MCP client launches it for you)
cd mcp-server && mvn spring-boot:run
```

STDIO uses stdout for the protocol, so the server logs to `mcp-server.log` (console logging is
disabled) and runs with no web server.

## Connect an MCP client

Point an MCP client at the jar. Example (Claude Desktop `claude_desktop_config.json` style):

```json
{
  "mcpServers": {
    "claudelab": {
      "command": "java",
      "args": ["-jar", "/ABSOLUTE/PATH/claude-spring-ai-engineering-lab/mcp-server/target/claude-spring-ai-engineering-lab-mcp-server-0.0.1-SNAPSHOT.jar"],
      "env": {
        "CLAUDELAB_MCP_KNOWLEDGE_BASE_DIR": "/ABSOLUTE/PATH/claude-spring-ai-engineering-lab/docs/knowledge-base"
      }
    }
  }
}
```

`CLAUDELAB_MCP_KNOWLEDGE_BASE_DIR` (or `--claudelab.mcp.knowledge-base-dir=...`) overrides where the
server reads docs from. The default, `../docs/knowledge-base`, assumes you launched from the
`mcp-server/` directory.

## Configuration

```
spring.ai.mcp.server.name                     = claudelab-mcp-server
spring.ai.mcp.server.type                      = SYNC
spring.ai.mcp.server.stdio                      = true
spring.ai.mcp.server.annotation-scanner.enabled = true
claudelab.mcp.knowledge-base-dir               = ../docs/knowledge-base
```

## Design notes

- **Isolated on purpose.** The module re-implements small, read-only helpers instead of depending on
  the core app, so the two never couple and the core build stays clean.
- **No Anthropic key needed to run the server** — it serves tools/resources/prompts; the *client*
  (e.g. Claude Desktop) brings the model.
- This is a starting point: the in-memory `create_backlog_item` and lexical `search_project_docs`
  mirror the core app's behavior and could later call the core's HTTP API instead.
