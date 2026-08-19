# AI Customer Support Assistant — Kiro Project Roadmap
## Developer Context

This project is being developed by a Java/Spring Boot developer
who has basic AI knowledge and wants to demonstrate practical
AI engineering skills through a real backend project.

Primary goal:

Build strong Java/Spring Boot + practical AI experience that can
be confidently explained in technical interviews.

The project should prioritize understanding, clean architecture,
testing, security, and explainability over adding unnecessary AI
technologies.


> Master roadmap and Kiro prompt library for the `ai-customer-support-assistant` project.
>
> **Current status:** Phase 1 completed.
>
> **Next:** Phase 2 — Spring AI + AI Ticket Analysis.

---

# 1. Project Overview

## Project Name

AI Customer Support Assistant

## Repository Name

`ai-customer-support-assistant`

## Repository Description

> Production-style AI-powered customer support assistant built with Java, Spring Boot, Spring AI, RAG, vector search, LLMs, controlled tool calling, and human-in-the-loop workflows.

## Project Goal

Build an enterprise-style customer support backend where support agents can manage customer support tickets and use AI to:

- Analyze support tickets
- Classify tickets
- Determine priority
- Summarize customer issues
- Detect customer sentiment
- Search a company knowledge base
- Generate grounded customer responses
- Provide source citations
- Retrieve customer information through controlled tools
- Retrieve previous customer tickets
- Support human approval before AI-generated responses are sent
- Evaluate AI quality
- Audit AI usage
- Secure AI functionality

The project is designed to demonstrate:

- Strong Java skills
- Spring Boot experience
- REST API development
- PostgreSQL/JPA
- Security
- Testing
- AI/LLM integration
- Spring AI
- Prompt engineering
- Structured output
- Embeddings
- RAG
- Vector search
- Tool calling
- AI evaluation
- AI security
- Production readiness

---

# 2. Technology Stack

## Backend

- Java 21
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- PostgreSQL
- Maven

## AI

- Spring AI
- LLM provider
- Chat Model
- Prompt Templates
- Structured Output
- Embeddings
- RAG
- Vector Store
- Tool Calling

## Testing

- JUnit 5
- Mockito
- Spring Boot Test
- Testcontainers

## Deployment

- Docker
- Docker Compose

## API Documentation

- OpenAPI
- Swagger UI

## Observability

- Structured logging
- Request/correlation IDs
- Application metrics
- AI latency metrics
- AI error metrics
- Token usage where supported

---

# 3. Kiro Project Structure

Kiro may create and maintain its own specification files.

Example:

```text
.kiro/
├── specs/
│   └── ...
└── kiro.config



## Development Rule

Never implement a feature just because AI/Kiro can generate it.

For every feature:

1. Understand the problem.
2. Understand the generated design.
3. Implement it.
4. Test it.
5. Review the code.
6. Be able to explain it in an interview.

Do not claim functionality that is not actually implemented.


## Interview Rule

Interview answers must be based on the actual implementation.

Never say:

"I implemented X"

unless X actually exists in the project.

If something was considered but not implemented, explain it as:

"For the current version, I chose not to implement X because..."