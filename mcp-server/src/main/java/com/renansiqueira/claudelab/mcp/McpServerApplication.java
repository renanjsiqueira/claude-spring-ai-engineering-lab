package com.renansiqueira.claudelab.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Experimental MCP server exposing a few of the lab's capabilities as MCP tools,
 * resources and prompts.
 *
 * <p>This is a <strong>separate, standalone</strong> Spring Boot application — it
 * is intentionally not part of the core app's build, so it cannot affect the main
 * application or its tests. Run it on its own (STDIO transport); see docs/mcp.md.
 */
@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}
