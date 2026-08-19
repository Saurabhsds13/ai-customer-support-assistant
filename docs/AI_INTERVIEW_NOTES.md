# AI Integration Interview Questions & Answers

## Phase 2: AI Ticket Analysis

---

### Q1: What is Spring AI and why did you choose it?

Spring AI is the official Spring project for LLM integration. It provides a consistent interface (`ChatModel`) across providers (OpenAI, Anthropic, Azure, Ollama). I chose it because it follows Spring conventions — dependency injection, auto-configuration, externalized properties — and allows provider switching without code changes.

---

### Q2: How do you prevent tight coupling to OpenAI?

The service depends on the `ChatModel` interface, not `OpenAiChatModel`. The concrete implementation is injected by Spring's auto-configuration based on whichever starter is on the classpath. Switching providers means changing a Maven dependency and config properties — zero Java code changes.

---

### Q3: What happens if the LLM returns garbage?

Three layers of defense:

1. **Structured output** — the prompt includes a JSON schema so the model knows exactly what format to produce.
2. **JSON deserialization** — if the response isn't valid JSON matching our DTO, the `BeanOutputConverter` throws an exception.
3. **Business validation** — we explicitly check for non-empty summary, category, priority, sentiment, and recommended actions.

---

### Q4: Why return 503 for AI failures instead of 500?

503 (Service Unavailable) signals that a downstream dependency failed, not our application. Clients can implement retry logic on 503. A 500 suggests a bug in our own code. This distinction helps operations teams triage issues faster.

---

### Q5: How do you test AI code without making real API calls?

We mock the `ChatModel` interface in unit tests. The mock returns pre-built `ChatResponse` objects with known JSON content. This tests our prompt construction, response parsing, validation, and error handling — everything except the actual LLM behavior. Real LLM calls belong in integration/evaluation tests with cost controls.

---

### Q6: Why use a prompt template file instead of inline strings?

Separation of concerns. The prompt is a content artifact — it can be reviewed, versioned, and modified by someone who isn't a Java developer. It also makes A/B testing prompts trivial (load different templates per environment or feature flag). Prompt engineers can iterate without recompiling the application.

---

### Q7: Why is the AI package separate from the business logic?

The `ai/` package is a bounded module. It has its own models, exceptions, and configuration. Business logic (`TicketService`) doesn't know about `ChatModel`, prompts, or structured output. This means AI features can evolve (RAG, tool calling, agents) without touching core CRUD code.

---

### Q8: How would you handle rate limiting from the AI provider?

Options:

1. **Spring Retry** with exponential backoff on the `ChatModel.call()`.
2. **Circuit breaker** (Resilience4j) that opens after N failures, returning a fast 503 instead of waiting for timeouts.
3. **Queue-based approach** for batch analysis where throughput matters more than latency.

The current `AiConfig` class is the natural place to add these beans.

---

### Q9: What is a ChatModel in Spring AI?

It's the central abstraction — an interface with a `call()` method that accepts a `Prompt` and returns a `ChatResponse`. OpenAI's implementation is `OpenAiChatModel`, Anthropic's is `AnthropicChatModel`, etc. Your service depends on the interface, not the implementation, making the provider interchangeable.

---

### Q10: What is structured output and why is it better than parsing free-form text?

Structured output means instructing the LLM to return JSON conforming to a specific schema (generated from a Java class via `BeanOutputConverter`). Benefits:

- No fragile regex or string parsing
- Type-safe deserialization directly into a Java DTO
- The model self-corrects because the schema is part of the prompt
- Validation failures are caught at deserialization time

---

### Q11: Why do you use separate DTOs for AI internal results vs. API responses?

`TicketAnalysisResult` is the AI's internal output model (what the LLM produces). `TicketAnalysisResponse` is the API contract (what clients receive — includes `ticketId`, could add timestamps, metadata, versioning). This separation means changes to the AI output format don't break the public API contract.

---

### Q12: How does this architecture allow changing AI providers later?

Three touch points, none in Java code:

1. **Maven dependency** — swap `spring-ai-openai-spring-boot-starter` for `spring-ai-anthropic-spring-boot-starter`
2. **Configuration** — change `spring.ai.openai.*` to `spring.ai.anthropic.*` in `application.yml`
3. **Environment variable** — update the API key env var

The `ChatModel` interface stays the same, the prompt template stays the same, all tests continue to pass.

---

### Q13: Why must API keys be externalized?

- **Security** — committed keys get scraped by bots within minutes
- **Rotation** — keys can be rotated without code changes or redeployment
- **Environment isolation** — dev/staging/prod use different keys with different rate limits
- **Compliance** — secrets in source control violate most security standards (SOC2, ISO 27001)

---

### Q14: What is the request flow for `POST /api/tickets/{id}/ai/analyze`?

1. Controller receives request → looks up ticket by ID (404 if not found)
2. Passes the `Ticket` entity to `TicketAnalysisService.analyzeTicket()`
3. Service builds a `Prompt` from the template + ticket data + format instructions
4. Calls `ChatModel.call(prompt)` — Spring AI handles the HTTP call to the provider
5. Extracts the response content string
6. `BeanOutputConverter` deserializes JSON into `TicketAnalysisResult`
7. Validates all required fields are present and non-empty
8. Controller wraps result into `TicketAnalysisResponse` with the ticket ID
9. Returns HTTP 200 with the analysis JSON

---

### Q15: How do you handle different types of AI failures?

The `AiServiceException` carries a typed enum (`AiErrorType`):

| Error Type | Trigger | HTTP Response |
|---|---|---|
| `TIMEOUT` | LLM call exceeds time limit | 503 |
| `PROVIDER_UNAVAILABLE` | Connection refused / network error | 503 |
| `MALFORMED_RESPONSE` | Empty or unparseable AI output | 503 |
| `VALIDATION_FAILURE` | Valid JSON but missing required fields | 503 |
| `UNKNOWN` | Any other unexpected exception | 503 |

All map to 503 because the failure is in the downstream AI service, not in our application logic.
