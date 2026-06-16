# Product Requirements Document

## Product Summary

LiftLog AI is a browser-based fitness tracking platform for desktop and mobile users. It helps users log workouts, monitor exercise progression, track nutrition, manage fitness goals, review dashboard summaries, and receive AI-powered fitness feedback.

The first production version should be a small-to-medium application with clear architecture, reliable authentication, strong tests, and enough extensibility to add richer coaching, analytics, and integrations later.

## Goals

- Provide a simple way for users to track workouts, exercises, nutrition, and goals.
- Show progress over time through dashboard summaries and exercise progression views.
- Provide AI-generated advisory feedback based on user-entered fitness data.
- Keep the product maintainable through layered backend design, modular frontend design, documented APIs, and automated tests.
- Support future expansion without rewriting core domains.

## Non-Goals

- Medical diagnosis, treatment advice, or clinical nutrition plans.
- Social networking, public leaderboards, or trainer marketplaces.
- Native mobile apps in the initial version.
- Direct wearable integrations in the initial version.
- Real-time collaborative features.

## User Roles

- Anonymous visitor: can access public entry points such as login and registration.
- Authenticated user: can manage only their own fitness data.
- System operator: manages configuration, deployment, logs, and production health outside the application UI.

## Functional Requirements

### Authentication and Authorization

- Users can register with email and password.
- Users can log in with email and password.
- Users can log out from the browser client.
- Passwords must be hashed with BCrypt.
- Authenticated API requests must identify the current user through secure HTTP-only cookies.
- Authentication tokens must not be stored in `localStorage` or `sessionStorage`.
- Access tokens must expire after 15 minutes.
- Refresh tokens must expire after 7 days.
- The refresh strategy must keep users logged in without exposing long-lived access tokens to browser JavaScript.
- CSRF protection must be included for cookie-authenticated state-changing requests where necessary.
- Users must only access their own workouts, nutrition logs, goals, dashboard data, and AI feedback history.
- Login and reset-style flows must not leak whether an email exists.

### User Profile

- Users can view and update basic profile information needed for fitness tracking.
- Profile data should support future personalization of dashboard summaries and AI feedback.
- Sensitive values must not be exposed unnecessarily through API responses.

### Workout Tracking

- Users can create, view, update, and delete workout logs.
- A workout can contain one or more exercises.
- Each workout exercise can contain one or more sets with reps, weight, duration, distance, or notes as applicable.
- Users can assign workout dates and optional notes.
- Users can browse paginated workout history.
- Users can retrieve a single workout with its exercise and set details.

### Exercise Progression

- Users can select exercises from a global shared exercise library.
- User workout data must reference exercise records instead of duplicating exercise names.
- Optional custom user-created exercises should be supported by the data model for future expansion.
- Users can view progression for an exercise over time.
- Progression metrics may include best weight, best reps, estimated volume, workout frequency, and recent trend.
- Progression views must be based only on the authenticated user's data.

### Nutrition Tracking

- Users can create, view, update, and delete nutrition logs.
- Nutrition logs can include date, meal type, food name, calories, protein, carbohydrates, fat, serving quantity, and notes.
- Users can browse paginated nutrition history.
- Users can view daily nutrition totals.

### Goal Tracking

- Users can create, view, update, and delete fitness goals.
- Goals can cover workout frequency, body weight targets, strength targets, nutrition targets, or custom habits.
- Goals must have a measurable target when possible.
- Users can view goal progress and completion status.
- Goal calculations must be deterministic and testable.

### Dashboard

- Users can view a dashboard summary after login.
- Dashboard summaries can include recent workouts, weekly workout count, nutrition totals, active goal progress, and AI feedback highlights.
- Dashboard data must be computed server-side from the authenticated user's records.
- Dashboard responses should avoid unnecessary large nested data.

### AI-Powered Feedback

- Users can request AI feedback based on recent workouts, nutrition logs, goals, or a specific question.
- AI feedback must be advisory and not framed as medical truth.
- AI prompts must exclude passwords, tokens, secrets, and unnecessary personal data.
- AI provider failures must be handled gracefully with stable error responses.
- OpenAI is the first AI provider.
- AI provider choice must remain configurable behind a provider abstraction so Gemini and Claude can be added later without changing controllers or core business logic.
- AI feedback history must be stored in the database indefinitely for now.
- AI feedback history must store at minimum `id`, `userId`, `promptSummary`, `feedback`, and `createdAt`.
- Automated tests must not call real external AI APIs.

## Non-Functional Requirements

### Maintainability

- Backend must follow controller -> service -> repository -> database layering.
- Business logic belongs in services or domain helpers, not controllers.
- Database access belongs in repositories only.
- Public API responses must use DTOs, not persistence entities.
- Frontend must use modular Vanilla JavaScript with a shared API client.
- Significant architecture decisions must be captured as ADRs.

### Security

- Passwords must be BCrypt hashed.
- Secrets and API keys must come from environment variables.
- Credentials, password hashes, tokens, secrets, and full sensitive AI prompts must never be logged.
- Auth tokens must be delivered through secure HTTP-only cookies and must not be persisted in browser storage.
- CSRF protection must be applied to cookie-authenticated state-changing requests where necessary.
- Backend authorization checks are mandatory for user-owned resources.
- Production deployments must use HTTPS.
- Input must be validated server-side.

### Reliability

- API errors must use the documented error response format.
- AI provider outages must not break non-AI product features.
- Tests must be deterministic and not depend on external AI APIs.
- Critical user paths must have integration coverage.

### Performance

- Large result sets must use pagination.
- Common user-specific query paths should be indexed.
- Dashboard queries should avoid unnecessary full-table scans.
- Initial optimization should focus on schema design, indexes, and bounded responses rather than premature caching.

### Usability

- The frontend must be mobile-friendly.
- Error messages shown to users must be readable and non-technical.
- Technical details should be logged internally, not exposed in the UI.
- Core workflows should be usable on desktop and mobile browsers.

### Observability

- Log authentication events, failed login attempts, important create/update/delete actions, external AI request failures, and unexpected exceptions.
- Logs must include enough context for diagnosis without exposing private or secret data.
- Use appropriate log levels for normal events, suspicious behavior, recoverable problems, and unexpected failures.

## User Stories

### Authentication

- As a new user, I want to register with my email and password so that I can create a private fitness account.
- As a returning user, I want to log in securely so that I can access my fitness data.
- As a user, I want my data protected so that no other user can view or modify it.

### Workout Tracking

- As a user, I want to log a workout with exercises and sets so that I can record what I did.
- As a user, I want to review past workouts so that I can understand my training history.
- As a user, I want to edit or delete a workout so that I can correct mistakes.

### Exercise Progression

- As a user, I want to see my progress for a specific exercise so that I can evaluate strength or endurance trends.
- As a user, I want progression metrics to be based on my completed workout data so that they reflect real activity.

### Nutrition Tracking

- As a user, I want to log meals and macros so that I can track nutrition.
- As a user, I want to view daily nutrition totals so that I can compare intake against goals.

### Goal Tracking

- As a user, I want to create fitness goals so that I can track progress toward measurable outcomes.
- As a user, I want to see goal progress on my dashboard so that I know where to focus.

### AI Feedback

- As a user, I want AI feedback on my recent training and nutrition so that I can identify practical next steps.
- As a user, I want AI feedback to be understandable and clearly advisory so that I can use it responsibly.

## Acceptance Criteria

### Authentication and Security

- Given a registered user, when they log in with valid credentials, then the server sets secure HTTP-only auth cookies and returns safe user information without token material in the response body.
- Given browser JavaScript, when auth state is inspected, then access and refresh tokens are not available through `localStorage`, `sessionStorage`, or response JSON.
- Given an access token older than 15 minutes, when a protected request is made, then the refresh flow can issue a new short-lived access token if the 7-day refresh token is still valid.
- Given a cookie-authenticated state-changing request, when CSRF protection is required and the request lacks a valid CSRF signal, then the API rejects the request.
- Given invalid login credentials, when login fails, then the response does not reveal whether the email exists.
- Given any stored password, when inspected in the database, then it is a BCrypt hash and not plaintext.
- Given one authenticated user, when they request another user's resource, then the API returns an authorization error.

### Workout Tracking

- Given a valid workout request, when the user creates a workout, then the API persists it for that user and returns a safe response DTO.
- Given a workout exercise entry, when it is persisted, then it references an exercise record from the shared exercise library rather than storing a duplicated exercise name.
- Given invalid workout input, when the request is submitted, then the API returns a validation error using the standard error format.
- Given a user with many workouts, when they request workout history, then the response is paginated.

### Nutrition Tracking

- Given a valid nutrition log request, when the user creates a log, then it is associated with the authenticated user.
- Given a date range, when the user requests nutrition totals, then totals are calculated only from that user's logs.

### Goal Tracking

- Given an active measurable goal, when dashboard data is requested, then current progress and status are included.
- Given a goal calculation, when source data changes, then progress reflects the updated source data deterministically.

### Dashboard

- Given an authenticated user, when they open the dashboard, then summary data includes recent workouts, nutrition totals, and active goal progress.
- Given no fitness data, when dashboard summary is requested, then the API returns empty or zero-state data without errors.

### AI Feedback

- Given a valid feedback request, when the AI provider succeeds, then the API returns advisory feedback in a consistent response model.
- Given successful AI feedback, when persistence completes, then the database stores at minimum `id`, `userId`, `promptSummary`, `feedback`, and `createdAt`.
- Given a user with prior AI feedback, when they request feedback history, then the API returns only that user's stored feedback history.
- Given an AI provider failure, when feedback is requested, then the API returns a graceful error without exposing provider internals.
- Given automated tests, when AI feedback behavior is tested, then tests use fake or stub providers rather than real external APIs.
