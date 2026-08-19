# AI-Powered Customer Support Assistant

A production-style, enterprise-grade customer support application built with **Java 21**, **Spring Boot 3**, and **Spring AI**. Support agents manage tickets and leverage AI to analyze issues, classify priority, generate grounded responses, and streamline resolution workflows.

> This project demonstrates strong Java/Spring Boot engineering combined with practical AI/LLM integration skills — designed as a professional portfolio piece.

---

## Business Problem

Customer support teams spend significant time on:
- Manually classifying and prioritizing incoming tickets
- Writing repetitive responses to common issues
- Searching knowledge bases for relevant information

This application augments support agents with AI capabilities — providing instant ticket analysis, suggested responses, and knowledge retrieval — while keeping humans in control of all customer-facing communications.

---

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.3 |
| Web | Spring Web (REST) |
| Persistence | Spring Data JPA, PostgreSQL |
| Security | Spring Security, JWT (jjwt) |
| Validation | Bean Validation (Jakarta) |
| AI | Spring AI, OpenAI (provider-agnostic) |
| Testing | JUnit 5, Mockito, Testcontainers |
| Build | Maven |
| Infrastructure | Docker Compose |
| Logging | SLF4J + Logstash (JSON structured) |

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                   REST Controllers                    │
│         (TicketController, TicketAiController)        │
├─────────────────────────────────────────────────────┤
│                   Service Layer                       │
│       (TicketService, TicketAnalysisService)          │
├─────────────────────────────────────────────────────┤
│              Repository Layer (JPA)                   │
│    (TicketRepository, CustomerRepository, etc.)      │
├─────────────────────────────────────────────────────┤
│                   PostgreSQL                          │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│                  AI Module (ai/)                      │
│  ┌───────────┐  ┌──────────┐  ┌─────────────────┐  │
│  │  Service   │  │  Prompt  │  │  Model / Config │  │
│  │ (ChatModel)│  │ Templates│  │  (Structured Out)│  │
│  └───────────┘  └──────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────┘
```

### Package Structure

```
com.example.aisupport/
├── ai/
│   ├── config/          # AI-specific configuration
│   ├── exception/       # AI error types
│   ├── model/           # Structured output DTOs
│   └── service/         # AI service interface + implementation
├── config/              # Security, logging, app config
├── controller/          # REST endpoints
├── dto/                 # Request/Response DTOs
├── entity/              # JPA entities
├── exception/           # Global exception handling
├── mapper/              # Entity ↔ DTO conversion
├── repository/          # Spring Data JPA interfaces
└── service/             # Business logic
```

---

## AI Architecture

The AI integration uses **Spring AI** with a provider-agnostic design:

- **Interface abstraction**: `TicketAnalysisService` — business logic depends on this, not on any LLM provider
- **ChatModel**: Spring AI's core interface; backed by OpenAI, Anthropic, or Ollama depending on config
- **Prompt templates**: Externalized `.st` files — prompts are content, not code
- **Structured output**: `BeanOutputConverter` generates JSON schemas from Java DTOs, ensuring type-safe LLM responses
- **Validation**: AI responses are validated before returning to clients

**Switching providers** requires zero code changes — swap the Maven dependency and update `spring.ai.*` properties.

---

## API Endpoints

### Ticket Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/tickets` | Create a ticket |
| GET | `/api/tickets` | List all tickets |
| GET | `/api/tickets/{id}` | Get ticket by ID |
| PUT | `/api/tickets/{id}` | Update a ticket |
| DELETE | `/api/tickets/{id}` | Delete a ticket |

### AI Analysis
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/tickets/{id}/ai/analyze` | AI-powered ticket analysis |

---

## AI Analysis Response Example

```json
{
  "ticketId": 1,
  "category": "ACCOUNT",
  "priority": "HIGH",
  "summary": "Customer cannot log in after resetting their password.",
  "sentiment": "FRUSTRATED",
  "recommendedActions": [
    "Verify password reset status",
    "Check whether the account is locked"
  ],
  "suggestedResponse": "We apologize for the inconvenience. Let us verify your account status and ensure the password reset completed successfully."
}
```

---

## Getting Started

### Prerequisites
- Java 21
- Maven 3.9+
- Docker & Docker Compose
- OpenAI API key (for AI features)

### Local Development

1. **Start PostgreSQL:**
   ```bash
   docker-compose up -d
   ```

2. **Set environment variables:**
   ```bash
   export DB_HOST=localhost
   export DB_PORT=5432
   export DB_NAME=support_assistant
   export DB_USERNAME=postgres
   export DB_PASSWORD=postgres
   export JWT_SECRET=your-256-bit-secret-key-here
   export OPENAI_API_KEY=sk-your-key-here
   ```

3. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

4. **Run tests:**
   ```bash
   mvn test
   ```

---

## Testing Strategy

| Layer | Approach | Tools |
|-------|----------|-------|
| Service | Unit tests with mocked dependencies | JUnit 5, Mockito |
| Controller | MockMvc endpoint tests | Spring Boot Test |
| Repository | Integration tests with real PostgreSQL | Testcontainers |
| AI Service | Mocked ChatModel (no real API calls) | Mockito |
| Security | Auth/authz rule verification | Spring Security Test |

Tests run independently with no shared mutable state. Integration tests are skipped gracefully when Docker is unavailable.

---

## Security

- JWT-based stateless authentication
- BCrypt password hashing (cost factor 10)
- Role-based authorization (ADMIN, SUPPORT_AGENT)
- No secrets in source code — all via environment variables
- CSRF disabled (stateless API)

---

## Development Phases

This project is built incrementally using spec-driven development:

| Phase | Focus | Status |
|-------|-------|--------|
| 1 | Foundation (entities, CRUD, validation, tests) | ✅ Complete |
| 2 | AI Ticket Analysis (Spring AI, structured output) | ✅ Complete |
| 3 | RAG & Knowledge Base | 🔜 Planned |
| 4 | Tool Calling & Customer Context | 🔜 Planned |
| 5 | Human Approval Workflow | 🔜 Planned |
| 6 | AI Evaluation & Observability | 🔜 Planned |

---

## Key Design Decisions

- **Provider-agnostic AI**: Spring AI `ChatModel` interface — swap OpenAI for Anthropic or Ollama without code changes
- **Prompt templates as resources**: Prompts are `.st` files, not Java strings — enables iteration without recompilation
- **Typed AI exceptions**: `AiServiceException` with `AiErrorType` enum for precise error categorization
- **DTO boundaries everywhere**: Entities never leak to API responses; AI internal models are separate from API contracts
- **Defensive AI validation**: Three layers — structured output schema, JSON deserialization, explicit business validation

---

## License

This project is for educational and portfolio demonstration purposes.
