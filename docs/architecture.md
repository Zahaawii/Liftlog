# Architecture

## Overview

LiftLog AI uses a layered monorepo architecture with separate frontend and backend applications. The backend owns business rules, authorization, persistence, error handling, logging, and AI provider integration. The frontend owns browser UI, user interactions, responsive layout, and API integration through a shared client.

Primary layers:

- Frontend
- Backend API
- Service layer
- Repository layer
- Database
- External integrations

Dependencies should stay explicit and directional. Controllers delegate to services. Services coordinate business logic and depend on repositories or stable abstractions. Repositories own database access. External integrations are isolated behind interfaces.

## Architectural Principles

- Follow SOLID and GRASP pragmatically.
- Keep each domain cohesive and independently understandable.
- Avoid business logic in controllers.
- Avoid database access outside repositories.
- Never expose persistence entities directly from controllers.
- Protect unstable integrations, especially AI providers, behind stable abstractions.
- Favor maintainability, scalability, and readability over premature optimization.

## Monorepo Structure

Proposed top-level structure:

```text
backend/
frontend/
docs/
ai/
docker-compose.yml
```

Backend domain structure should follow the repository guidance:

```text
backend/src/main/java/com/liftlogai/
  auth/
  user/
  workout/
  exercise/
  nutrition/
  goal/
  dashboard/
  ai/
  common/
```

Frontend structure should remain modular Vanilla JavaScript:

```text
frontend/
  index.html
  pages/
  css/
  js/
    api/
    auth/
    workouts/
    nutrition/
    dashboard/
    ai/
    shared/
```

## System Components

### Browser Frontend

- Renders authentication, dashboard, workout, nutrition, goal, and AI feedback experiences.
- Calls backend APIs through a shared API client.
- Uses secure HTTP-only cookies for authentication and does not store auth tokens in `localStorage` or `sessionStorage`.
- Sends CSRF protection data for cookie-authenticated state-changing requests where required by the backend.
- Shows user-friendly errors and avoids exposing technical details.

### Backend API

- Exposes REST endpoints under `/api`.
- Performs request validation, authentication, authorization, and DTO mapping.
- Delegates business work to services.
- Uses global exception handling for consistent API errors.

### Service Layer

- Contains application and business logic.
- Coordinates domain workflows such as workout creation, goal progress calculation, dashboard summary generation, and AI feedback requests.
- Depends on repositories and abstractions rather than concrete external clients.

### Repository Layer

- Owns database access.
- Encapsulates query behavior.
- Prevents controllers and services from depending on raw database details beyond repository contracts.

### Database

- MySQL stores users, global shared exercises, workouts, nutrition logs, goals, AI feedback history, and related records.
- Tables should be normalized enough to preserve data integrity without overcomplicating initial delivery.
- User-owned records must include ownership references for authorization and query filtering.
- Workout exercise records reference shared exercise records instead of duplicating exercise names.

### External AI Provider

- Provides advisory feedback through a provider abstraction.
- OpenAI is the first provider implementation.
- Provider implementation can be switched by configuration so Gemini and Claude can be added later.
- Provider failures are converted into stable application errors.
- Successful feedback is stored as user-owned history.

## Frontend Architecture

The frontend should be built with HTML, CSS, and modular Vanilla JavaScript.

Recommended modules:

- `apiClient`: shared request handling, cookie credentials, CSRF headers where needed, pagination parameters, and error mapping.
- `auth`: login, registration, logout, and current user state.
- `workouts`: workout list, detail, creation, editing, and deletion workflows.
- `nutrition`: nutrition log workflows and daily totals.
- `dashboard`: summary rendering and refresh behavior.
- `aiFeedback`: feedback request and result display.
- `ui`: DOM helpers, form helpers, loading states, and shared components.
- `errors`: user-friendly error message mapping.

Frontend rules:

- API calls go through the shared API client.
- UI behavior and API transport logic should remain separate.
- Authentication tokens must not be stored in browser-accessible storage.
- Frontend validation improves usability but does not replace backend validation.
- Technical error details should stay in logs or developer tooling, not user-facing screens.
- Layouts must be responsive for desktop and mobile browsers.

## Backend Architecture

The backend follows:

```text
controller -> service -> repository -> database
```

Controller responsibilities:

- Accept HTTP requests.
- Validate request DTOs.
- Resolve the authenticated user context.
- Call services.
- Return response DTOs and status codes.

Service responsibilities:

- Enforce business rules.
- Enforce user ownership where appropriate.
- Coordinate persistence and domain calculations.
- Build dashboard summaries.
- Coordinate AI feedback requests through abstractions.

Repository responsibilities:

- Persist and retrieve entities.
- Implement query methods.
- Support pagination and filtering.
- Avoid business decision-making.

Common backend infrastructure:

- Global exception handler.
- Stable error response model.
- Validation error mapping.
- Authentication and authorization filters.
- Logging utilities or conventions.
- DTO mappers.

Milestone 2 workout implementation:

- `exercise` owns the global shared exercise library, exercise listing, exercise details, and basic progression read models.
- `workout` owns workout CRUD, nested workout exercise/set persistence, user ownership checks, and workout response mapping.
- Workout controllers remain thin and delegate ownership-aware behavior to services.
- Workout mutations rely on the existing cookie authentication and CSRF filter.
- Workout history uses bounded offset pagination with a maximum page size of 100.

Milestone 3 nutrition implementation:

- `nutrition` owns nutrition log CRUD, paginated nutrition history, daily nutrition totals, user ownership checks, validation, and response mapping.
- Nutrition controllers stay thin and delegate ownership-aware behavior to services.
- Nutrition history uses bounded offset pagination with a maximum page size of 100.
- Nutrition mutations rely on the existing secure cookie authentication and CSRF filter.
- Daily totals are calculated server-side from the authenticated user's logs for the requested date.

Milestone 4 goal and dashboard implementation:

- `goal` owns goal CRUD, goal check-ins, user ownership checks, validation, response mapping, and deterministic progress calculations.
- Goal progress is calculated server-side from workout counts, nutrition totals, or latest check-ins depending on the target metric.
- `dashboard` owns summary composition only; it delegates domain-specific calculations to workout, nutrition, and goal services or repositories.
- Dashboard summaries are bounded and user-scoped: recent workouts, weekly workout count, today's nutrition totals, active goals, and a placeholder for latest AI feedback.
- Goal and dashboard mutations and reads rely on the existing secure cookie authentication, authorization checks, and CSRF protection for state-changing requests.

Milestone 5 AI feedback implementation:

- `ai` owns AI feedback request handling, prompt construction, provider abstraction, persistence, history reads, and provider failure mapping.
- `AiFeedbackController` remains thin and delegates to `AiFeedbackService`.
- `AiFeedbackService` gathers bounded workout, nutrition, and goal summaries, builds prompts through `PromptBuilder`, calls `AiProvider`, persists successful feedback, and returns DTOs.
- `AiProvider` hides provider-specific behavior. The first production provider is OpenAI; tests and local non-AI workflows can use the stub provider.
- `OpenAiProvider` uses environment-driven configuration and maps provider failures into stable application errors.
- Prompt summaries are stored for history; full prompts and raw provider responses are not stored.
- Dashboard reads the latest persisted AI feedback through the AI service, while AI feedback does not depend on dashboard.

## Domain Boundaries

Primary backend domains:

- User
- Authentication
- Workout
- Exercise
- Nutrition
- Goals
- Dashboard
- AI Feedback
- Common infrastructure

Boundary rules:

- Workout logic should not depend on nutrition logic unless a specific feature requires it.
- Dashboard can read summarized data from multiple services but should not own their business rules.
- AI feedback may use summarized domain data but should not bypass service boundaries.
- Shared helpers belong in common only when they are genuinely reusable.

## Security Architecture

Security controls:

- BCrypt password hashing.
- Environment variables for secrets.
- Authenticated access for user-owned APIs.
- Backend authorization checks on every user-owned resource.
- Server-side validation for all inputs.
- Standard error responses without stack traces.
- Production HTTPS.
- Sensitive logging restrictions.

Authentication approach:

- Use secure HTTP-only cookies for REST API authentication.
- Do not store auth tokens in `localStorage` or `sessionStorage`.
- Use a 15-minute access token and a 7-day refresh token.
- Use refresh rotation or equivalent refresh-token hardening so users remain logged in without exposing long-lived access tokens.
- Apply CSRF protection to cookie-authenticated state-changing requests where necessary.
- Keep token creation, validation, refresh handling, CSRF handling, and password hashing in dedicated services or security infrastructure.
- Avoid logging tokens or credentials.

Authorization approach:

- All user-owned data queries must be scoped by authenticated user ID.
- Mutating operations must verify ownership before update or deletion.
- Authorization failures should be distinguishable from authentication failures through stable error codes and HTTP statuses.

## AI Integration Architecture

AI flow:

```text
AI controller -> AiFeedbackService -> PromptBuilder -> AiProvider -> OpenAI provider implementation
```

Responsibilities:

- AI controller validates the request and returns DTOs.
- `AiFeedbackService` selects relevant user data, applies privacy limits, and coordinates provider calls.
- Prompt builder constructs bounded prompts from safe, minimal data.
- Provider interface defines the internal contract for AI feedback generation.
- OpenAI provider implementation contains OpenAI-specific API details.
- Gemini and Claude implementations can be added later behind the same provider contract.
- AI feedback service persists successful feedback history with at least `id`, `userId`, `promptSummary`, `feedback`, and `createdAt`.

Rules:

- Controllers must not call AI providers directly.
- Prompt construction must be testable.
- Provider failures must be handled gracefully.
- Tests must use fake or stub providers.
- AI feedback must include appropriate advisory framing.
- Full prompts containing sensitive user data should not be logged.
- Prompt summaries may be stored for history, but full sensitive prompts should not be stored unless a future retention policy explicitly approves it.

## Deployment Architecture

Initial deployment should use Docker-managed services:

- Frontend static assets served by a web server or backend-compatible static host.
- Spring Boot backend container.
- MySQL database container or managed MySQL service.
- Environment-specific configuration through environment variables.

Recommended environments:

- Local development with Docker Compose.
- Test environment with isolated database and fake AI provider.
- Production environment with HTTPS, managed secrets, persistent MySQL storage, and log aggregation.

Deployment concerns:

- Milestone 1 initializes the auth schema with Spring SQL initialization so local, test, and Docker environments can start consistently.
- Milestone 2 extends the same SQL initialization approach for the exercise and workout tables to keep local Docker and H2 tests aligned.
- Milestone 3 extends SQL initialization with the nutrition log table for the same local/test consistency.
- Milestone 4 extends SQL initialization with goal and goal check-in tables for local/test consistency.
- Milestone 5 extends SQL initialization with the AI feedback history table for local/test consistency.
- Formal database migration tooling should be introduced before production deployment or the next schema-heavy milestone.
- Production secrets must not be committed.
- Health checks should cover backend availability and database connectivity.
- OpenAI credentials must come from environment variables.
- AI provider credentials should be optional in non-AI local workflows where a fake provider is configured.

## Risks and Trade-Offs

- Secure HTTP-only cookie authentication protects tokens from browser JavaScript but requires CSRF handling and careful cookie settings.
- Vanilla JavaScript avoids frontend framework complexity but needs discipline around module boundaries and state management.
- A normalized relational model with shared exercises supports reporting and integrity but requires careful DTO and query design.
- AI feedback can add user value but introduces OpenAI provider cost, latency, privacy, and reliability concerns.
- Indefinite AI feedback history improves user continuity but increases privacy and data retention risk.
- Dashboard aggregation can become query-heavy as data grows; initial indexes and bounded summaries reduce risk without premature caching.
