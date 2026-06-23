# Construindo um laboratório de AI Engineering com Java, Spring AI e Claude

> Como transformei os conceitos do curso *"Claude with the Anthropic API"* em um projeto Java real —
> de uma única chamada de chat até RAG, MCP e workflows agênticos — **com o código de cada etapa**.

Quando terminei o curso *"Claude with the Anthropic API"*, da Anthropic, percebi um padrão: quase
todos os exemplos eram em Python. Eu trabalho com **Java e Spring** há anos e queria responder uma
pergunta concreta: *como esses mesmos conceitos ficam em uma stack JVM idiomática?*

O resultado é o **`claude-spring-ai-engineering-lab`**: um laboratório público, construído **fase por
fase**, conectando cada tópico do curso a **Java 21 + Spring Boot 3.5 + Spring AI 1.1**, com testes e
decisões de design documentadas.

Para não ficar abstrato, todas as fases giram em torno de um **case único e concreto**: um
**assistente de backlog de engenharia**. Você joga uma ideia crua ("importar transações via CSV") e o
sistema classifica, estrutura, estima, persiste, consulta a documentação do projeto e roda análises —
cada fase adicionando uma capacidade real sobre esse mesmo domínio.

Este artigo percorre o projeto etapa por etapa, **com o código de verdade**.

## Por que fiz o projeto

Três motivos:

1. **Aprender fazendo.** Reproduzir um conceito em outra linguagem força você a entendê-lo de verdade.
2. **Provar que AI Engineering não é exclusividade do Python.** O ecossistema Spring tem tudo que
   precisamos — e o Spring AI dá uma camada de abstração elegante sobre a API do Claude.
3. **Portfólio.** Um repositório limpo, testado e didático vale mais do que um README com promessas.

A regra que segui em todas as fases: **cada fase entrega uma capacidade, termina com `mvn test`
verde e tem as decisões registradas**. Nada de "implementa tudo de uma vez".

A arquitetura é uma camada simples e legível — cada pacote com uma responsabilidade clara:

```
api · application · ai · ai.prompt · domain · tools · rag · eval · workflow · persistence · infra
```

---

## Fase 1 — A primeira chamada: conectando Claude ao Spring AI

**Conceito.** O Spring AI expõe um `ChatClient` fluente que abstrai a API de mensagens do Claude.
A primeira fase é literalmente uma chamada:

```java
@Service
public class ClaudeChatService {

    private final ChatClient chatClient;

    public ClaudeChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String sendMessage(String message) {
        return chatClient.prompt().user(message).call().content();
    }
}
```

O `ChatClient` é montado uma vez, na camada `infra`, a partir do builder auto-configurado:

```java
@Bean
public ChatClient chatClient(ChatClient.Builder builder) {
    return builder.build();
}
```

**Decisão de design (que pagou em todas as fases seguintes).** A chave **nunca** fica no código —
vem de `ANTHROPIC_API_KEY`. E adotei um placeholder `not-set` no `application.yml`:

```yaml
spring:
  ai:
    anthropic:
      api-key: ${ANTHROPIC_API_KEY:not-set}
      chat:
        options:
          model: claude-opus-4-8
```

Com isso o contexto Spring sobe em testes/CI sem exigir uma chave real. **A suíte inteira roda
offline**: o Claude é mockado nos testes de web e a persistência usa H2. Cada fase pôde ser iterada
com segurança e em segundos.

---

## Fase 2 — Multi-turn, system prompt e temperatura

**Conceito.** Conversas com memória, uma persona fixa (system prompt) e controle de criatividade
(temperature) — os três vieram juntos.

```java
public Result chat(String conversationId, String message, Double temperature) {
    String resolvedId = (conversationId == null || conversationId.isBlank())
            ? UUID.randomUUID().toString() : conversationId;
    double resolvedTemperature = temperature != null ? temperature : DEFAULT_TEMPERATURE; // 0.2

    List<Message> history = memory.getHistory(resolvedId);

    String content = chatClient.prompt()
            .system(DEFAULT_SYSTEM_PROMPT)                 // persona fixa
            .messages(history)                             // histórico da conversa
            .user(message)
            .options(AnthropicChatOptions.builder()
                    .temperature(resolvedTemperature)      // temperatura por requisição
                    .build())
            .call()
            .content();

    memory.add(resolvedId, new UserMessage(message), new AssistantMessage(content));
    return new Result(resolvedId, content);
}
```

**No laboratório.** O `conversationId` deixa o usuário continuar a mesma thread (ex.: "agora adicione
critérios de borda para rate limiting"). A memória é **limitada**, para não crescer infinito — um
detalhe fácil de esquecer:

```java
public void add(String conversationId, Message... messages) {
    store.compute(conversationId, (id, existing) -> {
        List<Message> updated = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
        Collections.addAll(updated, messages);
        int size = updated.size();
        if (size > maxMessages) {                          // mantém só as N mais recentes
            return new ArrayList<>(updated.subList(size - maxMessages, size));
        }
        return updated;
    });
}
```

O system prompt aplica a persona do case: *"You are a senior software engineering assistant..."*.

---

## Fase 3 — Structured output: o "clique" do projeto

**Conceito.** Aqui está, para mim, a virada de chave do AI Engineering em backend. Com
`.entity(Tipo.class)`, o Spring AI injeta o JSON-schema do tipo no prompt e desserializa a resposta
direto em um `record` Java:

```java
public BacklogAnalysisResponse analyze(String input) {
    return chatClient.prompt()
            .system(BacklogPromptTemplate.SYSTEM)
            .user(input)
            .options(AnthropicChatOptions.builder().temperature(0.2).build())
            .call()
            .entity(BacklogAnalysisResponse.class);   // LLM -> objeto tipado
}
```

O contrato vive no `domain`, livre de framework:

```java
public record BacklogAnalysisResponse(
        BacklogType type,                 // FEATURE | BUG | REFACTOR | ARCHITECTURE | QUESTION
        String title,
        String summary,
        Priority priority,                // LOW | MEDIUM | HIGH | CRITICAL
        String userStory,
        List<String> acceptanceCriteria,  // critérios testáveis
        List<String> technicalTasks,
        List<String> risks,
        List<String> assumptions          // o que ficou faltando, em vez de inventar
) {}
```

**No laboratório.** Mando `"Quero importar transações via CSV"` e recebo:

```json
{
  "type": "FEATURE", "title": "CSV Transaction Import", "priority": "HIGH",
  "userStory": "As a user, I want to import transactions via CSV, so that I avoid manual entry.",
  "acceptanceCriteria": ["Given a valid CSV, when uploaded, then transactions are persisted."],
  "technicalTasks": ["Add CSV parser", "Validate rows"],
  "risks": ["CSV column format is unspecified"],
  "assumptions": ["Assuming UTF-8 encoding"]
}
```

**Por que importa.** O LLM vira uma **função tipada**. Em vez de parsing frágil de texto livre, o
resto do sistema continua fortemente tipado, validável e testável. Repare no campo `assumptions`: a
regra é **não inventar** — quando falta contexto, o modelo registra a premissa em vez de chutar uma
regra de negócio.

---

## Fase 4 — Streaming com Server-Sent Events

**Conceito.** Resposta incremental, token a token. O Spring AI tem a variante reativa `.stream()`:

```java
public Flux<String> stream(String message) {
    return chatClient.prompt()
            .user(message)
            .stream()
            .content()
            .onErrorResume(ex -> Flux.just(
                    "\n\n[stream interrupted] the response could not be completed. Please try again."));
}
```

**Decisão de design.** O Reactor já vem transitivo do Spring AI, e o **Spring MVC transmite `Flux`
como `text/event-stream` nativamente** — não precisei adicionar WebFlux (evitando confusão de
dual-stack). Erro no meio do stream é tratado de forma amigável com `onErrorResume`, em vez de
estourar a conexão. Testei até com `asyncDispatch` no MockMvc, sem rede.

---

## Fase 5 — Prompt engineering: prompt é código

**Conceito.** O Claude responde muito bem a estrutura explícita. Em vez de strings soltas espalhadas
pelos serviços, os prompts viraram **classes versionadas** no pacote `ai.prompt`, com tags XML e
exemplos few-shot:

```java
public final class BacklogPromptTemplate {

    public static final String SYSTEM = """
            <role>
            You are a senior software engineer assistant.
            </role>

            <rules>
            - Be specific.
            - Do not invent business rules.
            - Prefer testable acceptance criteria.
            - Identify assumptions and risks.
            - Return only the expected structured output.
            </rules>

            <examples>
            Example 1 - Feature request
            Input: "Allow users to export their invoices as PDF."
            Output: { "type": "FEATURE", "title": "Invoice PDF Export", ... }

            Example 2 - Bug report
            Input: "The app crashes when the session token expires."
            Output: { "type": "BUG", "title": "Crash on expired session token", ... }
            </examples>
            """;
}
```

**No laboratório.** O `BacklogAnalysisService` (Fase 3) passou a apontar para esse template. E como
prompt é comportamento de produto, ele é **testado**:

```java
@Test
void usesXmlTags() {
    assertThat(BacklogPromptTemplate.SYSTEM)
            .contains("<role>").contains("<rules>").contains("<examples>");
}
```

Um edit descuidado que derrube uma seção ou um exemplo quebra o build, como qualquer regressão.

---

## Fase 6 — Prompt evaluation: trocar achismo por evidência

**Conceito.** Prompt merece teste. Criei datasets versionados e um **grading determinístico em
código** — sem LLM, então 100% testável e rápido. Cada caso do dataset:

```java
public record DatasetItem(
        String input,
        BacklogType expectedType,
        int expectedMinimumCriteriaCount,
        int expectedMinimumTasksCount,
        List<String> forbiddenTerms) {}
```

O grader checa o essencial e reporta `passed/failed`:

```java
boolean validJson          = response != null;
boolean correctType        = validJson && response.type() == item.expectedType();
boolean hasTitle           = validJson && response.title() != null && !response.title().isBlank();
boolean meetsCriteriaCount = criteriaCount >= item.expectedMinimumCriteriaCount();
boolean meetsTasksCount    = tasksCount    >= item.expectedMinimumTasksCount();
boolean noForbiddenTerms   = /* nenhum termo proibido aparece na saída */;

boolean passed = validJson && correctType && hasTitle
        && meetsCriteriaCount && meetsTasksCount && noForbiddenTerms;
```

**No laboratório.** `POST /api/evals/run` roda 5 cenários (feature, bug, vago, arquitetura, refactor)
e devolve `{ "total": 5, "passed": 4, "failed": 1, "scores": [...] }`, salvando o relatório em
`evals/results/`. Ao mexer num prompt, eu **vejo na hora** se regrediu. Antes de um LLM-juiz
sofisticado, checagens baratas e determinísticas pegam muita coisa.

---

## Fase 7 — Tool use: o modelo como orquestrador dos seus serviços

**Conceito.** Em tool use, o Claude não executa o meu código — ele **pede** chamadas, e o Spring AI
roda o loop (chama a ferramenta, devolve o resultado, repete). Exponho serviços Spring com `@Tool`:

```java
@Tool(description = "Create and persist a backlog item for a project. "
        + "Returns the created item including its generated id.")
public BacklogItem createBacklogItem(
        @ToolParam(description = "Identifier of the project the item belongs to") String projectId,
        @ToolParam(description = "Short, descriptive title of the backlog item") String title,
        @ToolParam(description = "Detailed description of what needs to be done") String description) {
    BacklogItemResponse saved = backlogService.createItem(projectId, title, description);
    log.info("Tool createBacklogItem persisted id={} projectId={} title='{}'",
            saved.id(), projectId, title);
    return new BacklogItem(saved.id().toString(), saved.projectId(), saved.title(), description);
}
```

O agente registra as ferramentas no `ChatClient` e deixa o Claude decidir o que chamar:

```java
return chatClient.prompt()
        .system(SYSTEM_PROMPT)
        .user("Project: " + projectId + "\n\nRequest: " + message)
        .tools(projectContextTool, backlogTool, complexityTool)
        .call()
        .content();
```

**No laboratório.** Em `POST /api/agent/backlog`, o agente consulta o contexto do projeto, estima
complexidade e persiste o item — os logs mostram a sequência:

```
Tool getProjectContext called for projectId=devbacklog-ai-assistant
Tool estimateComplexity called -> HIGH
Tool createBacklogItem persisted id=... projectId=devbacklog-ai-assistant title='Import customers via CSV'
```

**Virada arquitetural.** O modelo passa a ser um **orquestrador dos seus serviços**. Isso torna
*ainda mais* importante (não menos): ferramentas pequenas e bem descritas, schemas claros e uma
separação limpa entre controller, serviço e ferramenta.

---

## Fase 8 — Persistência real com PostgreSQL, JPA e Flyway

**Conceito.** O `createBacklogItem` da Fase 7 deixou de ser memória e passou a gravar no Postgres.
A lógica de persistência fica na camada `application`:

```java
@Transactional
public BacklogItemResponse createItem(String projectId, String title, String description) {
    Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new NotFoundException("project '" + projectId + "' not found"));
    BacklogItem item = new BacklogItem(project, title);
    item.setSummary(description);
    return toResponse(backlogItemRepository.save(item));
}
```

O schema é dono do Flyway; o Hibernate apenas **valida** (`ddl-auto: validate`):

```sql
-- V1__init.sql
create table backlog_item (
    id         uuid primary key,
    project_id varchar(64) not null references project (id),
    type       varchar(32),
    title      varchar(255) not null,
    ...
);
```

**Decisão de design.** O `Project.id` é um **código legível** (`devbacklog-ai-assistant`), não um UUID — assim o
`projectId` que o agente usa é o mesmo da persistência, e uma migration semeia `devbacklog-ai-assistant` para o
fluxo funcionar de cara. Os testes continuam offline (H2 em modo PostgreSQL via `@DataJpaTest`), mas
**verifiquei o caminho real** subindo o Postgres com Docker Compose: Flyway aplicou as migrations e o
`validate` do Hibernate passou.

---

## Fase 9 — RAG sobre a documentação do projeto

**Conceito.** Responder perguntas usando os próprios documentos do projeto, com citação de fontes.
Como o starter Anthropic do Spring AI não traz um modelo de embeddings, comecei **simples e honesto**:
um índice vetorial em memória com vetores lexicais (TF) e similaridade de cosseno — determinístico e
offline.

```java
public RagAnswer answer(String question) {
    List<ScoredChunk> hits = searchService.search(question, topK);
    if (hits.isEmpty()) {
        return new RagAnswer(NOT_ENOUGH_INFORMATION, List.of());   // não inventa
    }
    String context = buildContext(hits);                            // <document source="...">
    String content = chatClient.prompt()
            .system(SYSTEM_PROMPT)
            .user("<context>\n" + context + "\n</context>\n\n<question>\n" + question + "\n</question>")
            .call()
            .content();
    List<String> sources = hits.stream().map(h -> h.chunk().source()).distinct().toList();
    return new RagAnswer(content, sources);
}
```

**No laboratório.** `POST /api/rag/ask` com *"Quais padrões devo seguir para criar uma nova feature?"*
recupera trechos de `coding-standards.md` e `architecture-guidelines.md` e responde **citando os
arquivos**. A regra de ouro: responder somente com base nos documentos e, sem contexto suficiente,
dizer isso explicitamente.

**Decisão de design.** O índice tem a **mesma forma** de um `VectorStore` do Spring AI — dá para
trocar por embeddings de verdade depois sem mexer nos chamadores.

---

## Fase 10 — MCP: minhas ferramentas viram interoperáveis

**Conceito.** O **Model Context Protocol** deixa qualquer cliente compatível (como o Claude Desktop)
consumir capacidades do projeto. Virou um **módulo experimental e separado** (`mcp-server/`), com seu
próprio build — assim nunca afeta o app principal. Com o starter MCP do Spring AI (anotações):

```java
@McpTool(name = "search_project_docs",
        description = "Search the project documentation and return matching snippets with sources.")
public String searchProjectDocs(
        @McpToolParam(description = "The search query", required = true) String query) {
    return knowledge.search(query);
}

@McpResource(uri = "docs://coding-standards",
        name = "Coding standards", description = "The project's coding standards.")
public String codingStandards() {
    return knowledge.document("coding-standards.md");
}
```

Expus 4 **tools** (`search_project_docs`, `get_project_context`, `create_backlog_item`,
`estimate_task_complexity`), 3 **resources** (`project://devbacklog-ai-assistant/context`,
`docs://architecture-guidelines`, `docs://coding-standards`) e 2 **prompts**
(`generate_backlog_item`, `review_architecture_decision`). É o passo natural de "minhas ferramentas
viram um contrato aberto".

---

## Fase 11 — Agentes e workflows

**Conceito.** Implementei três padrões clássicos sobre o assistente de backlog. A decisão de design
mais valiosa foi colocar cada workflow atrás de uma abstração de **um método só**:

```java
public interface LlmStep {
    String complete(String systemPrompt, String userPrompt);
}
```

Com isso, a orquestração inteira fica **testável sem nenhuma chamada de API** — basta mockar o passo.

**1. Chaining (sequencial)** — cada saída alimenta a próxima:

```java
String classification = step.complete(CLASSIFY_SYSTEM, input).strip();
String item     = step.complete(BACKLOG_SYSTEM, "Classification: " + classification + "\n\n" + input);
String criteria = step.complete(CRITERIA_SYSTEM, item);
String review   = step.complete(REVIEW_SYSTEM, "Backlog item:\n" + item + "\n\nCriteria:\n" + criteria);
```

**2. Routing (despacho por tipo)** — classifica e escolhe um prompt especializado (feature, bug,
arquitetura ou refactor).

**3. Parallel review (fan-out + merge)** — quatro análises concorrentes, depois um merge:

```java
CompletableFuture<String> product   = CompletableFuture.supplyAsync(() -> step.complete(PRODUCT_SYSTEM, input));
CompletableFuture<String> technical = CompletableFuture.supplyAsync(() -> step.complete(TECHNICAL_SYSTEM, input));
CompletableFuture<String> risks     = CompletableFuture.supplyAsync(() -> step.complete(RISKS_SYSTEM, input));
CompletableFuture<String> tests     = CompletableFuture.supplyAsync(() -> step.complete(TESTS_SYSTEM, input));
CompletableFuture.allOf(product, technical, risks, tests).join();

String merged = step.complete(MERGE_SYSTEM, /* junta as quatro análises */);
```

**No laboratório.** O teste do chaining roda sem tocar no Claude — só mockando o `LlmStep`:

```java
LlmStep step = mock(LlmStep.class);
when(step.complete(anyString(), anyString()))
        .thenReturn("FEATURE", "backlog draft", "criteria", "final review");

WorkflowResult result = new ChainingWorkflow(step).run("Import CSV");

assertThat(result.finalAnalysis()).isEqualTo("final review");
assertThat(result.steps()).extracting(WorkflowStep::name)
        .containsExactly("classify", "backlog-item", "acceptance-criteria", "review");
```

`POST /api/workflows/analyze` expõe os três (seletor opcional `workflow`, default `CHAINING`).

---

## Visão sobre AI Engineering com Java

Algumas convicções que ficaram:

- **Structured output é o que torna LLM utilizável como peça de backend** num sistema tipado.
  `.entity(Tipo)` foi o divisor de águas do projeto.
- **Trate prompts como código**: versionados, com tags XML e few-shot, revisáveis e testáveis.
- **Avalie prompts** com datasets e checagens determinísticas, como qualquer outro comportamento.
- **Mantenha os testes offline** mockando o modelo (e H2 na persistência) — velocidade e segurança
  para iterar em todas as fases.
- **Tool use muda a arquitetura**: o modelo orquestra os *seus* serviços; design limpo importa mais.
- **Crie costuras de abstração** (como `LlmStep`) para deixar agentes testáveis.
- **Verifique o caminho real quando der** — subi o Postgres de verdade para confirmar migrations +
  `validate`, não só o H2.

E a conclusão maior: **o ecossistema Java/Spring está pronto para AI Engineering.** O Spring AI
entrega abstrações sólidas (chat, structured output, streaming, tool use, RAG, MCP) e tudo o que já
amamos no Spring — injeção de dependência, validação, testes de fatia, persistência, transações —
continua valendo. Não precisamos sair do Java para construir produtos sérios com LLMs.

---

*Projeto: **`claude-spring-ai-engineering-lab`** — Java 21, Spring Boot 3.5, Spring AI 1.1, Claude.
12 fases, suíte de testes 100% offline, licença Apache 2.0. Código, fases e documentação no
repositório.*

*Se você trabalha com Java e está olhando para LLMs: dá para começar hoje, na stack que você já
domina. Bora trocar ideia nos comentários. 🚀*
