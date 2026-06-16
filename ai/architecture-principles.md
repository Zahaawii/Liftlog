# Architecture Principles

## 1. Core Architecture

LiftLog AI uses a layered monorepo architecture.

The main layers are:

* Frontend
* Backend API
* Service layer
* Repository layer
* Database
* External integrations

Each layer must have a clear responsibility. Dependencies should point inward toward business logic, not outward toward frameworks or implementation details.

---

## 2. Backend Layering

The backend must follow this structure:

```text
controller → service → repository → database
```

Rules:

* Controllers handle HTTP requests and responses only.
* Controllers must not contain business logic.
* Services contain business rules and application logic.
* Repositories handle database access only.
* Entities represent persisted database models.
* DTOs represent data entering or leaving the API.
* Controllers should not expose JPA entities directly.

---

## 3. Frontend Structure

The frontend must be modular Vanilla JavaScript.

Recommended modules:

* `apiClient`
* `auth`
* `workouts`
* `nutrition`
* `dashboard`
* `aiFeedback`
* `ui`
* `errors`

Rules:

* API calls should go through one shared API client.
* UI logic and API logic should not be mixed unnecessarily.
* Reusable DOM helpers should live in shared UI utilities.
* Frontend error messages should be user-friendly.
* Technical error details should stay in logs, not in the UI.

---

## 4. SOLID Principles

Apply SOLID pragmatically.

### Single Responsibility

Each class, module, and function should have one main reason to change.

Bad:

* A service that validates input, sends emails, queries the database, and builds HTML.

Good:

* A service that coordinates workout logging.
* A repository that only handles workout persistence.
* A validator that only validates workout input.

### Open/Closed

Code should be extendable without rewriting core logic.

Example:

* AI providers should be hidden behind an interface.
* Adding Claude later should not require rewriting controllers.

### Liskov Substitution

Implementations should behave consistently with their interfaces.

Example:

* All AI providers should return feedback in the same internal response format.

### Interface Segregation

Avoid large interfaces with unrelated methods.

Bad:

* `FitnessService` with workout, nutrition, AI, dashboard, and user methods.

Good:

* `WorkoutService`
* `NutritionService`
* `AiFeedbackService`
* `DashboardService`

### Dependency Inversion

High-level business logic should depend on abstractions, not concrete implementations.

Example:

* `AiFeedbackService` depends on `AiProvider`.
* It should not directly depend on `OpenAiClient`.

---

## 5. GRASP Principles

Use GRASP to decide where responsibilities belong.

### Information Expert

Put behavior close to the data it needs.

Example:

* A workout domain object can calculate total volume if it has sets, reps, and weight.

### Creator

A class should create objects it closely owns.

Example:

* `WorkoutService` can create workout log entries.

### Controller

HTTP controllers should delegate work to services.

They should not make business decisions.

### Low Coupling

Avoid unnecessary dependencies between modules.

Example:

* Nutrition code should not depend on workout code unless there is a clear business reason.

### High Cohesion

Keep related logic together.

Example:

* Nutrition goal logic belongs in nutrition services, not in dashboard services.

### Polymorphism

Use interfaces when behavior varies.

Example:

* OpenAI, Gemini, and Claude should each implement the same AI provider contract.

### Pure Fabrication

Create service/helper classes when no domain object naturally owns the responsibility.

Example:

* `TokenService`
* `PasswordHashingService`
* `ApiErrorFactory`

### Indirection

Use an intermediate abstraction to reduce coupling.

Example:

* Controllers call `AiFeedbackService`, not external AI SDKs.

### Protected Variations

Hide unstable parts behind stable interfaces.

Example:

* LLM APIs change often, so isolate provider-specific logic.

---

## 6. Domain Boundaries

Main domains:

* User
* Authentication
* Workout
* Exercise
* Nutrition
* Goals
* Dashboard
* AI Feedback

Rules:

* Keep domain logic inside the correct domain.
* Avoid one large generic service.
* Avoid circular dependencies between domains.
* Shared logic should go into a shared/common package only when truly reusable.

---

## 7. DTO and Entity Rules

Entities:

* Represent database tables.
* Used internally by backend persistence logic.
* Should not be returned directly from controllers.

DTOs:

* Represent API input and output.
* Used to protect internal database structure.
* Used to validate request data.

Rules:

* Request DTOs should contain validation annotations.
* Response DTOs should only expose safe client-facing fields.
* Never expose password hashes, internal IDs where unnecessary, tokens, or secrets.

---

## 8. API Design

API endpoints should be consistent and resource-oriented.

Examples:

```text
POST   /api/auth/register
POST   /api/auth/login
GET    /api/workouts
POST   /api/workouts
GET    /api/workouts/{id}
PUT    /api/workouts/{id}
DELETE /api/workouts/{id}

GET    /api/nutrition/logs
POST   /api/nutrition/logs

GET    /api/dashboard/summary
POST   /api/ai/feedback
```

Rules:

* Use nouns for resources.
* Use HTTP methods correctly.
* Use consistent response formats.
* Use pagination for large result sets.
* Validate all incoming data.
* Return meaningful status codes.

---

## 9. Error Handling Standard

All backend API errors should follow one format:

```json
{
  "timestamp": "2026-06-16T12:00:00Z",
  "status": 400,
  "errorCode": "WORKOUT_INVALID_INPUT",
  "message": "Workout date is required.",
  "path": "/api/workouts"
}
```

Rules:

* Do not expose stack traces to users.
* Log technical details internally.
* Return readable messages to the frontend.
* Use stable error codes.
* Handle validation errors consistently.
* Handle authentication and authorization errors separately.

---

## 10. Logging Principles

Backend logs should help diagnose production issues.

Log:

* Authentication events
* Failed login attempts
* Important create/update/delete actions
* External AI request failures
* Unexpected exceptions

Do not log:

* Passwords
* Password hashes
* Tokens
* API keys
* Full LLM prompts containing sensitive user data
* Private user information unless necessary

Rules:

* Logs should include enough context to debug.
* Logs should not leak secrets.
* Use appropriate log levels:

    * INFO for important normal events
    * WARN for suspicious or recoverable problems
    * ERROR for failures requiring attention
    * DEBUG for local development details

---

## 11. Security Principles

Security is not optional.

Rules:

* Hash passwords with BCrypt.
* Never store plaintext passwords.
* Use environment variables for secrets.
* Validate all input.
* Protect user-specific resources.
* Users must only access their own data.
* Do not trust frontend validation.
* Backend authorization is mandatory.
* Avoid leaking whether an email exists during login/reset flows.
* Use HTTPS in production.

---

## 12. AI Integration Principles

AI logic must be isolated.

Structure:

```text
AiFeedbackService → AiProvider interface → OpenAI/Gemini/Claude implementation
```

Rules:

* Controllers must not call AI providers directly.
* Provider-specific code must stay inside provider implementations.
* AI prompts should be built in a dedicated prompt builder.
* Do not send passwords, tokens, or secrets to the LLM.
* Minimize personal data sent to the LLM.
* AI feedback should be treated as advisory, not medical truth.
* Handle AI provider failures gracefully.
* Support switching providers through configuration.

---

## 13. Testing Principles

Tests should protect important behavior, not just increase coverage numbers.

Required test areas:

* Authentication
* Authorization
* Workout creation and retrieval
* Nutrition logging
* Goal calculation
* Dashboard summaries
* Error handling
* AI provider abstraction

Rules:

* New business logic requires tests.
* Bug fixes require regression tests.
* Services should have unit tests.
* API endpoints should have integration tests.
* Repository behavior should be tested where queries are non-trivial.
* Tests should be deterministic.
* Do not depend on real external AI APIs in automated tests.

---

## 14. TDD Rules

Use TDD where practical.

Preferred cycle:

```text
write failing test → implement smallest solution → refactor
```

TDD is especially useful for:

* Service logic
* Calculations
* Validation
* Goal progress
* Error handling
* AI prompt construction

TDD is less useful for:

* Simple getters/setters
* Static HTML layout
* Basic CSS styling

---

## 15. Documentation Rules

Documentation must explain the system, not repeat the code.

Required docs:

* Project overview
* Setup guide
* Environment variables
* Architecture overview
* API documentation
* Database design
* Testing guide
* Deployment guide
* Known limitations
* Future improvements

Rules:

* Update documentation when architecture changes.
* Document major decisions.
* Use comments for non-obvious logic.
* Do not add useless comments explaining obvious code.

Bad comment:

```java
// Increment i by 1
i++;
```

Good comment:

```java
// Weekly volume is calculated from completed sets only,
// because planned sets should not affect progress statistics.
```

---

## 16. Package and Folder Principles

Backend package example:

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

Each domain package may contain:

```text
controller/
service/
repository/
dto/
entity/
mapper/
exception/
```

Frontend folder example:

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

Rules:

* Organize by feature/domain when possible.
* Avoid dumping everything into generic folders.
* Shared code must be truly shared.

---

## 17. Database Principles

Rules:

* Use clear table names.
* Use foreign keys.
* Use indexes for frequent lookups.
* Store timestamps where useful.
* Avoid duplicated data unless there is a reason.
* Do not store calculated summaries unless performance requires it.
* Use migrations if the project introduces schema versioning.

Important relationships:

* One user has many workouts.
* One workout has many workout sets.
* One exercise can appear in many workout sets.
* One user has many nutrition logs.
* One user has nutrition goals.
* One user has many AI feedback records.

---

## 18. Performance Principles

Performance should be reasonable, not over-engineered.

Rules:

* Paginate large lists.
* Avoid loading all user history unnecessarily.
* Avoid N+1 query problems.
* Use indexes for user-based lookups.
* Cache only when there is a proven need.
* Keep frontend JavaScript lightweight.
* Avoid large unnecessary dependencies.

---

## 19. Maintainability Principles

Code should be easy to change.

Rules:

* Prefer simple solutions.
* Avoid premature abstraction.
* Avoid large classes.
* Avoid duplicated business rules.
* Keep naming consistent.
* Keep formatting consistent.
* Refactor when complexity grows.
* Every module should have a clear reason to exist.

---

## 20. Final Rule

When there is a conflict between speed and maintainability, choose maintainability unless the manager agent explicitly approves a shortcut.
