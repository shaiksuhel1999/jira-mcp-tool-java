# Jira MCP Toolkit (Java)

A two-module Spring Boot + [Spring AI](https://docs.spring.io/spring-ai/reference/) project that lets you manage **Atlassian Jira issues in plain English**. It exposes Jira operations as [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) tools from a server, and drives them from a client backed by a local **Ollama** LLM.

```
 User prompt ──▶ Client (Ollama LLM) ──MCP──▶ Server (Jira tools) ──REST──▶ Jira Cloud
   "Create a task in PROJ and assign it to John"
```

## Modules

| Module | Role | Description |
| --- | --- | --- |
| [`jira-mcp-toolkit-server`](jira-mcp-toolkit-server) | **MCP server** | Wraps the Jira REST Java Client and publishes each Jira operation as an MCP tool using `@Tool` annotations. |
| [`jira-mcp-toolkit-client`](jira-mcp-toolkit-client) | **MCP client** | Spring Boot web app with a `ChatClient` (Ollama) that discovers the server's tools and calls them based on a natural-language prompt. |

## Tech stack

- **Java 21**, **Spring Boot 3.5.x**, **Maven** (wrapper included)
- **Spring AI 1.1.2** — MCP server/client starters, Ollama model starter
- **Jira REST Java Client (JRJC) 4.0.0** + Atlassian Fugue
- **Ollama** running `llama3.2:3b` locally

## Exposed MCP tools

Defined in [`JiraManagementServiceImpl`](jira-mcp-toolkit-server/src/main/java/com/tekmaturix/jiramcptoolkitserver/service/JiraManagementServiceImpl.java):

| Tool | What it does |
| --- | --- |
| `createIssue` | Create an issue (project key, issue type id, summary, description, assignee, due-date offset in days) |
| `getIssue` | Fetch full details of an issue by key |
| `updateIssueDescription` | Update an issue's description |
| `addComment` | Add a comment to an issue |
| `getComments` | List all comments on an issue |
| `deleteIssue` | Delete an issue (optionally its subtasks) |
| `getTotalVotesCount` | Return the number of votes on an issue |

## Prerequisites

- JDK 21+
- [Ollama](https://ollama.com/) installed and running, with the model pulled:
  ```bash
  ollama pull llama3.2:3b
  ```
- A Jira Cloud site and an [API token](https://id.atlassian.com/manage-profile/security/api-tokens)

## Configuration

Jira credentials live in
[`JiraManagementConstants`](jira-mcp-toolkit-server/src/main/java/com/tekmaturix/jiramcptoolkitserver/constants/JiraManagementConstants.java).
Replace the placeholder values before running the server:

```java
public static final String BASE_URL  = "https://your-site.atlassian.net";
public static final String USER_NAME = "you@example.com";
public static final String API_TOKEN = "your-jira-api-token";
```

The client talks to Ollama at `http://localhost:11434` using `llama3.2:3b` (see the client's `application-*.yml`).

## Transport profiles

Both modules ship three Spring profiles that pair up. Pick one transport and run **server and client with the matching profile**.

| Profile | Transport | Server port | Client → Server endpoint |
| --- | --- | --- | --- |
| `sse` | Server-Sent Events | `8081` | `http://localhost:8081/sse` |
| `http` | Streamable HTTP | `8082` (server) | `http://localhost:8081/mcp` |
| `stdio` | Standard IO (process pipe) | — (web disabled) | launches the server jar as a subprocess |

> The `sse` profile is the simplest to start with (server on `8081`, client connects to `8081`).

## Build & run

### 1. Start the MCP server

```bash
cd jira-mcp-toolkit-server
./mvnw spring-boot:run -Dspring-boot.run.profiles=sse
```

### 2. Start the MCP client

```bash
cd jira-mcp-toolkit-client
./mvnw spring-boot:run -Dspring-boot.run.profiles=sse
```

On Windows use `mvnw.cmd` instead of `./mvnw`.

### stdio profile

For `stdio`, package the server jar and point
[`mcp-servers-config.json`](jira-mcp-toolkit-client/src/main/resources/mcp-servers-config.json)
at its path, then run the client with the `stdio` profile:

```bash
cd jira-mcp-toolkit-server
./mvnw clean package
```

## Usage

The client exposes a single REST endpoint
([`JiraMcpToolkitClientController`](jira-mcp-toolkit-client/src/main/java/com/tekmaturix/jiramcptoolkitclient/controller/JiraMcpToolkitClientController.java)):

```
GET /mcp/jira/api/v1/call?prompt=<your natural-language instruction>
```

Example:

```bash
curl "http://localhost:8080/mcp/jira/api/v1/call?prompt=Create a Task in project PROJ titled 'Fix login bug' assigned to John Doe due in 3 days"
```

The LLM interprets the prompt, selects the appropriate MCP tool(s), and the server executes them against Jira. More examples:

- `Get the details of issue PROJ-123`
- `Add a comment to PROJ-123 saying "deployed to staging"`
- `How many votes does PROJ-123 have?`
- `Delete issue PROJ-456 including its subtasks`

## Project layout

```
jira-mcp-tool-java/
├── jira-mcp-toolkit-server/     # MCP server — Jira tools
│   └── src/main/java/.../service/JiraManagementServiceImpl.java
└── jira-mcp-toolkit-client/     # MCP client — Ollama-backed chat + REST endpoint
    └── src/main/java/.../controller/JiraMcpToolkitClientController.java
```

## License

No license specified.
