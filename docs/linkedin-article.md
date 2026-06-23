# Construindo um laboratório de AI Engineering com Java, Spring AI e Claude

> Como transformei os conceitos do curso *"Claude with the Anthropic API"* em um projeto Java real —
> de uma única chamada de chat até RAG, MCP e workflows agênticos — e o que aprendi no caminho.

Quando terminei o curso *"Claude with the Anthropic API"*, da Anthropic, percebi um padrão: quase
todos os exemplos eram em Python. Eu trabalho com **Java e Spring** há anos e queria responder uma
pergunta concreta: *como esses mesmos conceitos ficam em uma stack JVM idiomática?*

O resultado é o **`claude-spring-ai-engineering-lab`**: um laboratório público, construído **fase por
fase**, conectando cada tópico do curso a **Java 21 + Spring Boot + Spring AI**, com testes e decisões
de design documentadas. Este artigo conta o porquê, o como e as lições.

## Por que fiz o projeto

Três motivos:

1. **Aprender fazendo.** Reproduzir um conceito em outra linguagem força você a entendê-lo de verdade.
2. **Provar que AI Engineering não é exclusividade do Python.** O ecossistema Spring tem tudo que
   precisamos — e o Spring AI dá uma camada de abstração elegante sobre a API do Claude.
3. **Portfólio.** Um repositório limpo, testado e didático vale mais do que um README com promessas.

A regra que segui em todas as fases: **cada fase entrega uma capacidade, termina com `mvn test`
verde e tem as decisões registradas**. Nada de "implementa tudo de uma vez".

## O que aprendi no curso

O curso organiza a jornada de quem constrói com LLMs: chamadas básicas, conversas multi-turn, system
prompts, temperatura, streaming, **structured output**, engenharia de prompt com tags XML,
**avaliação de prompts**, **tool use**, **RAG**, **MCP** e, por fim, **agentes e workflows**. Cada um
desses tópicos virou uma fase do laboratório (mais uma fase extra de persistência com PostgreSQL).

## Como conectei a API do Claude ao Spring AI

O Spring AI expõe um `ChatClient` fluente. A primeira fase foi literalmente isto:

```java
chatClient.prompt().user(message).call().content();
```

A chave da API **nunca** fica no código — vem da variável de ambiente `ANTHROPIC_API_KEY`. Um detalhe
prático que adotei cedo: um placeholder `not-set` no `application.yml`, para o contexto Spring subir
em testes/CI sem exigir uma chave real. Assim, **a suíte de testes roda offline** (o Claude é mockado
nos testes de web e a persistência usa H2). Isso pagou dividendos em todas as fases seguintes.

Multi-turn, system prompt e temperatura vieram juntos: um serviço que guarda histórico por
`conversationId` (limitado, para não crescer infinito), aplica um system prompt fixo e repassa a
`temperature` via `AnthropicChatOptions`.

## Por que structured output importa

Esse foi o "clique" do projeto. Com `.entity(BacklogAnalysisResponse.class)`, o Spring AI injeta o
JSON-schema do tipo no prompt e desserializa a resposta direto em um `record` Java:

```java
chatClient.prompt()
    .system(BacklogPromptTemplate.SYSTEM)
    .user(input)
    .call()
    .entity(BacklogAnalysisResponse.class);
```

O efeito é profundo: **o LLM vira uma função tipada**. Em vez de fazer parsing frágil de texto livre,
o resto do sistema continua fortemente tipado, validável e testável. Recebi uma ideia vaga ("importar
transações via CSV") e devolvi um item de backlog estruturado — tipo, prioridade, critérios de aceite
testáveis, tarefas técnicas, riscos e premissas. É a base para usar "LLM como backend" com segurança.

## Por que prompt evaluation importa

Prompt é comportamento de produto. E comportamento merece teste. Criei datasets versionados
(`evals/datasets/*.json`) e um **grading determinístico em código**: o JSON é válido? o tipo está
correto? tem título? atende ao mínimo de critérios e tarefas? evita termos proibidos?

O grading é uma função pura — testável sem chamar o modelo — e roda sobre todos os cenários,
reportando `passed/failed`. Isso troca "achismo" por evidência: ao mexer num prompt, eu vejo na hora
se regrediu. Antes de um LLM-juiz sofisticado, **checagens baratas e determinísticas pegam muita
coisa**.

## Como tool use muda a arquitetura

Em tool use, o Claude não executa o meu código — ele **pede** chamadas, e o Spring AI roda o loop:
chama a ferramenta, devolve o resultado, repete até a resposta final. Expus serviços Spring como
ferramentas com `@Tool`:

```java
@Tool(description = "Create and persist a backlog item for a project.")
public BacklogItem createBacklogItem(@ToolParam(...) String projectId, ...) { ... }
```

A virada de chave arquitetural: **o modelo passa a ser um orquestrador dos *seus* serviços**. Isso
torna ainda mais importante (não menos) ter ferramentas pequenas e bem descritas, schemas claros e uma
separação limpa entre controller, serviço e ferramenta. No laboratório, o agente consulta o contexto
do projeto, estima complexidade e **persiste o item no PostgreSQL** — cada ferramenta logando quando é
chamada.

## Como RAG e MCP entram no roadmap

**RAG** respondeu perguntas sobre a própria documentação do projeto. Como o starter Anthropic do
Spring AI não traz um modelo de embeddings, comecei simples e honesto: um **índice vetorial em
memória** com vetores lexicais (TF) e similaridade de cosseno. É determinístico, roda offline e tem a
**mesma forma** de um `VectorStore` do Spring AI — então dá para trocar por embeddings de verdade
depois sem mexer nos chamadores. A regra de ouro: responder **somente** com base nos documentos,
citar as fontes e, quando faltar contexto, dizer que não há informação suficiente.

**MCP (Model Context Protocol)** virou um módulo **experimental e separado** (`mcp-server/`), com seu
próprio build — assim ele nunca afeta o app principal. Ele expõe capacidades do projeto (buscar docs,
contexto do projeto, criar backlog, estimar complexidade) como **tools, resources e prompts** MCP, que
qualquer cliente compatível (como o Claude Desktop) pode consumir. É o passo natural de "minhas
ferramentas viram interoperáveis".

Por fim, **agentes e workflows**: implementei três padrões clássicos — *chaining* (sequencial),
*routing* (despacho por tipo) e *parallel review* (fan-out + merge). Uma decisão de design valiosa foi
colocar cada workflow atrás de uma abstração de um método só, `LlmStep`. Com isso, consegui
**testar a orquestração inteira sem nenhuma chamada de API**, mockando o passo de LLM.

## Visão sobre AI Engineering com Java

Algumas convicções que ficaram:

- **Structured output é o que torna LLM utilizável como peça de backend** num sistema tipado.
- **Trate prompts como código**: versionados, com tags XML e few-shot, revisáveis e testáveis.
- **Avalie prompts** com datasets e checagens determinísticas, como qualquer outro comportamento.
- **Mantenha os testes offline** mockando o modelo — velocidade e segurança para iterar.
- **Tool use muda a arquitetura**: o modelo orquestra seus serviços; design limpo importa mais.
- **Crie costuras de abstração** (como `LlmStep`) para deixar agentes testáveis.

E a conclusão maior: **o ecossistema Java/Spring está pronto para AI Engineering.** Spring AI entrega
abstrações sólidas (chat, structured output, streaming, tool use, RAG, MCP) e tudo o que já amamos no
Spring — injeção de dependência, validação, testes de fatia, persistência, transações — continua
valendo. Não precisamos sair do Java para construir produtos sérios com LLMs.

---

*Projeto: `claude-spring-ai-engineering-lab` — Java 21, Spring Boot 3.5, Spring AI 1.1, Claude. Código,
fases e documentação no repositório (licença Apache 2.0).*
