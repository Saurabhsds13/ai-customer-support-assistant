# Requirements Document

## Introduction

Phase 1 of the AI-Powered Customer Support Assistant establishes the project foundation. This phase delivers a production-quality Spring Boot application with clean layered architecture, PostgreSQL persistence, JWT-based security, and CRUD REST APIs for managing users, customers, and support tickets. No AI features are included in this phase — the focus is entirely on a solid, testable, enterprise-grade backend foundation.

## Glossary

- **Application**: The Spring Boot 3.x backend service providing REST APIs for customer support management
- **User**: An internal system account (ADMIN or SUPPORT_AGENT) that authenticates via JWT to access protected endpoints
- **Customer**: An external person or entity who submits support tickets; managed by internal users
- **Ticket**: A support request submitted on behalf of a Customer, tracked through lifecycle statuses
- **JWT**: JSON Web Token used for stateless authentication and authorization
- **BCrypt**: A password hashing algorithm used for secure credential storage
- **ADMIN**: A role granting full system access including user management
- **SUPPORT_AGENT**: A role granting access to customer and ticket management operations
- **Controller_Layer**: The REST API layer that receives HTTP requests, validates inputs, and delegates to the Service_Layer
- **Service_Layer**: The business logic layer that orchestrates operations between the Controller_Layer and Repository_Layer
- **Repository_Layer**: The data access layer using Spring Data JPA to interact with PostgreSQL
- **Entity**: A JPA-annotated domain object mapped to a database table
- **DTO**: A Data Transfer Object used at API boundaries to decouple internal entities from external representations
- **Mapper**: A component responsible for converting between Entity and DTO objects
- **Global_Exception_Handler**: A centralized component that catches exceptions and returns consistent error responses
- **Security_Filter**: A servlet filter that intercepts requests to validate JWT tokens and establish authentication context
- **Testcontainers**: A Java library providing lightweight, disposable PostgreSQL instances for integration testing

## Requirements

### Requirement 1: Project Structure and Build Configuration

**User Story:** As a developer, I want a well-structured Spring Boot 3.x Maven project with Java 21, so that the codebase follows enterprise conventions and builds reliably.

#### Acceptance Criteria

1. THE Application SHALL use Spring Boot 3.2 or higher with Java 21 as the compilation target
2. THE Application SHALL use Maven as the build tool with a pom.xml declaring dependencies for Spring Web, Spring Data JPA, Spring Security, Bean Validation, PostgreSQL driver, JUnit 5, Mockito, Spring Boot Test, and Testcontainers, such that `mvn compile` succeeds without errors
3. THE Application SHALL organize source code into packages: controller, service, repository, entity, dto, mapper, exception, security, and config
4. THE Application SHALL include a Docker Compose file that provisions a PostgreSQL 15 or higher instance with credentials and port configured via environment variables
5. WHEN the project is built with `mvn compile`, THE Application SHALL complete without compilation errors

### Requirement 2: Database Entity Design

**User Story:** As a developer, I want well-defined JPA entities for User, Customer, and Ticket, so that the data model supports the core domain of customer support management.

#### Acceptance Criteria

1. THE Repository_Layer SHALL persist User entities with fields: id (auto-generated), username (max 50 characters, unique, not null), email (max 100 characters, unique, not null), password (max 255 characters, not null), role (not null), createdAt, and updatedAt
2. THE Repository_Layer SHALL persist Customer entities with fields: id (auto-generated), firstName (max 50 characters, not null), lastName (max 50 characters, not null), email (max 100 characters, unique, not null), phone (max 20 characters, nullable), company (max 100 characters, nullable), createdAt, and updatedAt
3. THE Repository_Layer SHALL persist Ticket entities with fields: id (auto-generated), title (max 150 characters, not null), description (max 2000 characters, not null), status (not null), priority (not null), category (not null), customer reference (not null), assignedAgent reference (nullable), createdAt, updatedAt, and resolvedAt (nullable)
4. THE Repository_Layer SHALL enforce that Ticket status values are restricted to OPEN, IN_PROGRESS, RESOLVED, and CLOSED
5. THE Repository_Layer SHALL enforce that Ticket priority values are restricted to LOW, MEDIUM, HIGH, and CRITICAL
6. THE Repository_Layer SHALL enforce that Ticket category values are restricted to BILLING, TECHNICAL, GENERAL, and ACCOUNT
7. THE Repository_Layer SHALL maintain a many-to-one relationship between Ticket and Customer
8. THE Repository_Layer SHALL maintain a many-to-one relationship between Ticket and User for agent assignment
9. THE Repository_Layer SHALL enforce that User role values are restricted to ADMIN and SUPPORT_AGENT
10. WHEN a User, Customer, or Ticket entity is first persisted, THE Repository_Layer SHALL automatically set the createdAt field to the current timestamp
11. WHEN a User, Customer, or Ticket entity is updated, THE Repository_Layer SHALL automatically set the updatedAt field to the current timestamp

### Requirement 3: Spring Data JPA Repositories

**User Story:** As a developer, I want Spring Data JPA repositories for each entity, so that data access is consistent and leverages Spring conventions.

#### Acceptance Criteria

1. THE Repository_Layer SHALL provide a JPA repository interface for User entities with query methods: findByUsername returning Optional, findByEmail returning Optional, and existsByUsername and existsByEmail returning boolean
2. THE Repository_Layer SHALL provide a JPA repository interface for Customer entities with query methods: findByEmail returning Optional, and existsByEmail returning boolean
3. THE Repository_Layer SHALL provide a JPA repository interface for Ticket entities with query methods: findByStatus returning List, findByAssignedAgentId returning List, and findByCustomerId returning List

### Requirement 4: JWT Authentication

**User Story:** As a user, I want to authenticate with username and password and receive a JWT, so that I can access protected endpoints without re-entering credentials on each request.

#### Acceptance Criteria

1. WHEN a valid username and password are submitted to the login endpoint, THE Application SHALL return HTTP 200 with a JWT containing the username and role, where the token expiration is determined by the configured expiration setting (between 15 minutes and 24 hours)
2. IF an invalid username or password is submitted to the login endpoint, THEN THE Application SHALL return HTTP 401 with an error message indicating authentication failure without revealing whether the username or password was incorrect
3. THE Security_Filter SHALL validate the JWT signature and expiration from the Authorization header Bearer token on every request to a protected endpoint
4. IF a request contains an expired or malformed JWT, THEN THE Security_Filter SHALL reject the request with HTTP 401
5. IF a request to a protected endpoint contains no JWT, THEN THE Security_Filter SHALL reject the request with HTTP 401
6. THE Application SHALL allow unauthenticated access to the login endpoint

### Requirement 5: Role-Based Authorization

**User Story:** As an administrator, I want role-based access control, so that only authorized users can perform sensitive operations.

#### Acceptance Criteria

1. THE Application SHALL support two roles: ADMIN and SUPPORT_AGENT
2. WHILE a User has the ADMIN role, THE Application SHALL grant access to all endpoints including user management
3. WHILE a User has the SUPPORT_AGENT role, THE Application SHALL grant full CRUD access to customer management and ticket management endpoints
4. WHILE a User has the SUPPORT_AGENT role, THE Application SHALL deny access to user management endpoints
5. IF an authenticated User attempts to access an endpoint outside the permissions of the assigned role, THEN THE Application SHALL return HTTP 403
6. THE Application SHALL allow unauthenticated access to the login endpoint without requiring any role

### Requirement 6: Secure Password Storage

**User Story:** As a security-conscious developer, I want passwords stored with BCrypt hashing, so that credentials remain protected even if the database is compromised.

#### Acceptance Criteria

1. WHEN a User account is created, THE Application SHALL hash the password using BCrypt with a cost factor of 10 or higher before persisting it
2. WHEN a User updates a password, THE Application SHALL hash the new password using BCrypt before persisting it
3. WHEN a User authenticates, THE Application SHALL verify the submitted password against the stored BCrypt hash
4. THE Application SHALL enforce a minimum password length of 8 characters
5. THE Application SHALL never store or return plaintext passwords in API responses, logs, or database records

### Requirement 7: User Management API

**User Story:** As an administrator, I want CRUD endpoints for managing user accounts, so that I can create, view, update, and remove support agents.

#### Acceptance Criteria

1. WHEN an ADMIN submits a valid user creation request, THE Controller_Layer SHALL create the User and return HTTP 201 with the user details excluding the password
2. WHEN an ADMIN requests the list of users, THE Controller_Layer SHALL return HTTP 200 with all User records excluding passwords
3. WHEN an ADMIN requests a specific user by ID, THE Controller_Layer SHALL return HTTP 200 with the User details excluding the password
4. WHEN an ADMIN submits a valid user update request, THE Controller_Layer SHALL update the User and return HTTP 200 with the updated details excluding the password
5. WHEN an ADMIN submits a user deletion request for a User who is not assigned to any Ticket, THE Controller_Layer SHALL delete the User and return HTTP 204
6. IF a user creation request contains a username or email that already exists, THEN THE Controller_Layer SHALL return HTTP 409 with an error message indicating which field caused the conflict
7. IF a user update request changes the username or email to a value that already exists for another User, THEN THE Controller_Layer SHALL return HTTP 409 with an error message indicating which field caused the conflict
8. IF a user GET, update, or deletion request references a user ID that does not exist, THEN THE Controller_Layer SHALL return HTTP 404 with an error message indicating the user was not found
9. IF a user deletion request targets a User who is currently assigned to one or more Tickets, THEN THE Controller_Layer SHALL return HTTP 409 with an error message indicating the user cannot be deleted due to active ticket assignments

### Requirement 8: Customer Management API

**User Story:** As a support agent, I want CRUD endpoints for managing customer records, so that I can maintain customer information for ticket tracking.

#### Acceptance Criteria

1. WHEN an authenticated User submits a valid customer creation request, THE Controller_Layer SHALL create the Customer and return HTTP 201 with the customer details
2. WHEN an authenticated User requests the list of customers, THE Controller_Layer SHALL return HTTP 200 with all Customer records
3. WHEN an authenticated User requests a specific customer by ID, THE Controller_Layer SHALL return HTTP 200 with the Customer details
4. WHEN an authenticated User submits a valid customer update request, THE Controller_Layer SHALL update the Customer and return HTTP 200 with the updated details
5. WHEN an authenticated User submits a customer deletion request for a Customer that has no associated Tickets, THE Controller_Layer SHALL delete the Customer and return HTTP 204
6. IF a customer creation request contains an email that already exists, THEN THE Controller_Layer SHALL return HTTP 409 with a descriptive error message
7. IF a customer update request contains an email that already belongs to a different Customer, THEN THE Controller_Layer SHALL return HTTP 409 with a descriptive error message
8. IF a customer request (get, update, or delete) references a customer ID that does not exist, THEN THE Controller_Layer SHALL return HTTP 404 with a descriptive error message
9. IF a customer deletion request targets a Customer that has associated Tickets, THEN THE Controller_Layer SHALL return HTTP 409 with a descriptive error message indicating the Customer cannot be deleted

### Requirement 9: Ticket Management API

**User Story:** As a support agent, I want endpoints to create, read, update, list, and assign tickets, so that I can manage the lifecycle of customer support requests.

#### Acceptance Criteria

1. WHEN an authenticated User submits a valid ticket creation request with a customer reference, THE Controller_Layer SHALL create the Ticket with status OPEN and return HTTP 201 with the created Ticket details
2. WHEN an authenticated User requests the list of tickets, THE Controller_Layer SHALL return HTTP 200 with all Ticket records including each ticket's current status, priority, and category
3. WHEN an authenticated User requests a specific ticket by ID, THE Controller_Layer SHALL return HTTP 200 with the Ticket details including customer and assigned agent information
4. WHEN an authenticated User submits a valid ticket update request, THE Controller_Layer SHALL update only the allowed mutable fields (title, description, status, priority, and category) and return HTTP 200 with the updated Ticket details
5. WHEN an authenticated User submits a ticket assignment request with a valid agent ID, THE Controller_Layer SHALL assign the Ticket to the specified agent and return HTTP 200 with the updated Ticket details
6. WHEN a Ticket status is changed to RESOLVED, THE Application SHALL set the resolvedAt timestamp to the current time
7. IF a ticket creation request references a non-existent customer, THEN THE Controller_Layer SHALL return HTTP 404 with a descriptive error message indicating the customer was not found
8. IF a ticket assignment request references a non-existent agent, THEN THE Controller_Layer SHALL return HTTP 404 with a descriptive error message indicating the agent was not found
9. IF a ticket retrieval, update, or assignment request references a non-existent ticket ID, THEN THE Controller_Layer SHALL return HTTP 404 with a descriptive error message indicating the ticket was not found

### Requirement 10: Input Validation

**User Story:** As a developer, I want Bean Validation applied to all API inputs, so that invalid data is rejected before reaching business logic.

#### Acceptance Criteria

1. THE Controller_Layer SHALL validate all incoming request DTOs using Bean Validation annotations before processing
2. IF a request body fails validation, THEN THE Global_Exception_Handler SHALL return HTTP 400 with a response listing each field name, the rejected value, and a description of the constraint violation
3. THE Controller_Layer SHALL validate that required fields (username, email, password for User; firstName, lastName, email for Customer; title, description, customerId for Ticket) are present and non-blank, with string fields enforcing a minimum length of 1 character and a maximum length of 255 characters, except password which SHALL enforce a minimum length of 8 characters and a maximum length of 100 characters
4. THE Controller_Layer SHALL validate that email fields conform to a valid email format and do not exceed 255 characters in length
5. IF a request contains a value for status, priority, or category that is not one of the allowed enum values defined in the Entity, THEN THE Controller_Layer SHALL reject the request with HTTP 400 indicating the invalid field and accepted values

### Requirement 11: Global Exception Handling

**User Story:** As an API consumer, I want consistent error responses across all endpoints, so that I can reliably parse and handle errors programmatically.

#### Acceptance Criteria

1. THE Global_Exception_Handler SHALL return error responses in a consistent JSON structure containing: timestamp (ISO 8601 format in UTC), status code (integer), error type (string), message (string, maximum 500 characters), and request path (string)
2. WHEN a requested resource is not found, THE Global_Exception_Handler SHALL return HTTP 404 with the consistent error structure
3. WHEN a validation error occurs, THE Global_Exception_Handler SHALL return HTTP 400 with the consistent error structure including a field-level details collection where each entry contains the field name and the violation message
4. WHEN a duplicate resource conflict occurs, THE Global_Exception_Handler SHALL return HTTP 409 with the consistent error structure
5. WHEN an unexpected server error occurs, THE Global_Exception_Handler SHALL return HTTP 500 with the consistent error structure containing a generic message that does not include internal stack traces, class names, or line numbers
6. WHEN an authentication failure occurs, THE Global_Exception_Handler SHALL return HTTP 401 with the consistent error structure
7. WHEN an authorization failure occurs, THE Global_Exception_Handler SHALL return HTTP 403 with the consistent error structure
8. WHEN a request is made with an unsupported HTTP method, THE Global_Exception_Handler SHALL return HTTP 405 with the consistent error structure
9. IF a request body cannot be parsed as valid JSON, THEN THE Global_Exception_Handler SHALL return HTTP 400 with the consistent error structure indicating a malformed request body

### Requirement 12: DTO and Mapper Layer

**User Story:** As a developer, I want DTOs at API boundaries with dedicated mappers, so that internal entities are decoupled from external representations and sensitive data is never leaked.

#### Acceptance Criteria

1. THE Controller_Layer SHALL accept separate request DTOs for create and update operations, and return response DTOs for all API operations, rather than exposing Entity objects directly
2. THE Mapper SHALL convert request DTOs to Entity objects and Entity objects to response DTOs for User, Customer, and Ticket
3. THE Mapper SHALL exclude the password field from all User response DTOs
4. THE Mapper SHALL map Ticket entities to response DTOs that include nested customer summary (id, firstName, lastName, email) and assigned agent summary (id, username, email) when those associations are present
5. IF a Ticket has no assigned agent, THEN THE Mapper SHALL return the assigned agent field as null in the Ticket response DTO

### Requirement 13: Testing Strategy

**User Story:** As a developer, I want comprehensive automated tests covering all layers, so that I can refactor and extend the codebase with confidence.

#### Acceptance Criteria

1. THE Application SHALL include unit tests for all Service_Layer classes using Mockito for dependency isolation, covering both success paths and error/exception handling paths
2. THE Application SHALL include integration tests for all Repository_Layer interfaces using Testcontainers with PostgreSQL, verifying CRUD operations and custom query methods
3. THE Application SHALL include controller tests for all Controller_Layer endpoints using MockMvc to verify valid and invalid request scenarios including request handling, validation, and error response structure
4. THE Application SHALL include security tests verifying authentication and authorization rules including unauthenticated access, expired/invalid tokens, and role-based access denial
5. THE Application SHALL ensure all tests run independently without shared mutable state between test methods

### Requirement 14: Configuration and Environment Management

**User Story:** As a developer, I want externalized configuration with no secrets in source code, so that the application can be deployed across environments securely.

#### Acceptance Criteria

1. THE Application SHALL read database connection parameters (host, port, database name, username, password) from environment variables or Spring configuration profiles
2. THE Application SHALL read JWT secret key and expiration settings from environment variables or Spring configuration profiles
3. THE Application SHALL never include secrets, passwords, or private keys in source-controlled files
4. THE Application SHALL include an example configuration file (e.g., application.yml with placeholder values) that documents all required configuration properties without containing real secret values
5. IF a required configuration property (database URL, JWT secret) is missing at startup, THEN THE Application SHALL fail to start with a descriptive error message identifying the missing property

### Requirement 15: Logging

**User Story:** As a developer, I want structured logging across the application, so that I can trace request flow and diagnose issues in production.

#### Acceptance Criteria

1. THE Application SHALL use SLF4J with JSON-formatted structured log output containing at minimum: timestamp, log level, logger name, message, and contextual key-value pairs for all logged events
2. WHEN a request is processed, THE Application SHALL log the HTTP method, path, response status, duration in milliseconds, and a correlation ID that uniquely identifies the request
3. WHEN an authentication failure occurs, THE Application SHALL log the event at WARN level with the attempted username and request source
4. WHEN an unexpected error occurs, THE Application SHALL log the full exception with stack trace at ERROR level
5. THE Application SHALL never log sensitive data including passwords, JWT tokens, or personal customer information (email addresses, phone numbers, and company names from Customer records)
6. WHEN a request is received, THE Application SHALL generate a unique correlation ID and include it in all log entries produced during the processing of that request
