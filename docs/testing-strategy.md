# Testing Strategy

## Testing Goals

- Protect critical user workflows.
- Verify business logic at the service layer.
- Verify API contracts and authorization behavior through integration tests.
- Keep tests deterministic and independent from real external AI providers.
- Use tests to support refactoring without breaking behavior.

## Coverage Expectations

Coverage should focus on meaningful behavior rather than raw percentages.

Required coverage areas:

- Authentication.
- Authorization.
- Cookie-based token handling.
- Refresh-token behavior.
- CSRF protection for state-changing requests where necessary.
- Workout creation and retrieval.
- Workout exercise references to shared exercise records.
- Exercise progression calculations.
- Nutrition logging and daily summaries.
- Goal calculations.
- Dashboard summaries.
- Error handling.
- AI provider abstraction, OpenAI provider mapping, fake provider behavior, prompt construction, and AI feedback history persistence.

Every new business rule should have a test. Every bug fix should include a regression test that would fail without the fix.

## Unit Testing Approach

Unit tests should cover service logic, validators, mappers, prompt builders, and calculations.

Backend unit test targets:

- Password validation policy.
- Access token and refresh token lifetime rules.
- CSRF validation behavior where implemented in application-level code.
- Workout creation rules.
- Workout volume and progression calculations.
- Nutrition total calculations.
- Goal progress calculations.
- Dashboard summary composition.
- AI prompt construction.
- AI provider response mapping.
- AI feedback history mapping.
- Error factory or error mapping behavior.

Unit test rules:

- Keep tests deterministic.
- Avoid real databases unless testing repository behavior.
- Avoid real AI APIs.
- Prefer fake providers for provider-contract tests.
- Test edge cases such as empty data, invalid input, boundary dates, and unauthorized ownership.

## Integration Testing Approach

Integration tests should verify API behavior across controller, service, repository, validation, security, and database boundaries.

Required integration test areas:

- Register and login.
- Secure HTTP-only auth cookies are set on login and do not appear in response bodies.
- Access token refresh succeeds with a valid 7-day refresh token.
- Expired or invalid refresh tokens are rejected.
- State-changing requests enforce CSRF protection where required.
- Authenticated current-user request.
- Failed login behavior without email existence leaks.
- Access denied for another user's resource.
- Workout CRUD with nested exercises and sets.
- Workout exercise entries reference global shared exercise records.
- Paginated workout history.
- Nutrition log CRUD and daily totals.
- Goal CRUD and progress response.
- Dashboard summary.
- Standard error response format.
- AI feedback success using a fake provider.
- AI feedback history is stored with `id`, `userId`, `promptSummary`, `feedback`, and `createdAt`.
- AI feedback history returns only the authenticated user's records.
- AI feedback provider failure handling.

Integration test rules:

- Use isolated test data.
- Reset or isolate database state between tests.
- Do not depend on test execution order.
- Use test configuration that never calls real external AI APIs.
- Verify HTTP status codes, response bodies, and stable error codes.

## Repository Testing

Repository tests are required for non-trivial queries, including:

- Paginated workout history by user and date.
- Exercise progression query inputs.
- Nutrition totals by date.
- Active goals by user.
- Dashboard summary source queries if custom queries are used.

Simple generated CRUD behavior does not need exhaustive repository tests unless it carries authorization or filtering risk.

## End-to-End Testing Approach

End-to-end tests should cover the most important user journeys in a browser-like environment after the frontend and backend are integrated.

Priority E2E flows:

- Register or log in.
- Stay logged in through access-token refresh without exposing tokens to browser storage.
- Create a workout with an exercise and set.
- View workout history.
- Create a nutrition log and view daily totals.
- Create a goal and see it on the dashboard.
- Request AI feedback with a fake provider.
- Verify user-friendly error handling for a failed request.

E2E rules:

- Use deterministic test accounts and data.
- Run against a test environment, not production.
- Avoid relying on external AI providers.
- Keep E2E coverage focused on critical workflows to reduce flakiness.

## Security Testing

Security-focused tests should verify:

- Passwords are stored hashed.
- Protected endpoints reject unauthenticated requests.
- Authentication tokens are delivered only through secure HTTP-only cookies.
- Authentication tokens are not available in `localStorage`, `sessionStorage`, or JSON response bodies.
- CSRF protections reject missing or invalid CSRF signals for protected state-changing requests where required.
- User-owned endpoints reject access to another user's resources.
- API responses do not expose password hashes, tokens, secrets, or internal fields.
- Validation rejects malformed or unsafe input.
- Error responses do not expose stack traces.

## AI Testing

AI behavior must be tested without real provider calls.

Required tests:

- `AiFeedbackService` calls the provider abstraction, not a concrete provider directly.
- OpenAI provider implementation maps OpenAI responses into the internal provider response model.
- Gemini and Claude can be represented by fake contract tests later without changing controller tests.
- Prompt builder excludes credentials, tokens, secrets, and unnecessary personal data.
- Prompt summary persistence excludes full sensitive prompts.
- Provider success maps into the internal feedback response model.
- Successful feedback persists AI feedback history indefinitely unless a future retention policy changes.
- Provider failure maps into a stable application error.
- Configuration can select a fake provider for tests.

## Regression Testing

Every bug fix should include:

- A failing test that reproduces the bug.
- The smallest implementation change needed to pass.
- A regression test committed with the fix.

Regression tests should be placed at the lowest level that catches the bug reliably. Service-level bugs usually need unit tests. API contract or security bugs usually need integration tests.

## Test Data Strategy

- Use builders or fixtures for users, workouts, nutrition logs, goals, and AI feedback requests.
- Keep fixture data realistic but small.
- Avoid copying large JSON bodies across tests.
- Make ownership explicit in test data.
- Use stable dates for deterministic calculations.

## CI Expectations

CI should run:

- Backend unit tests.
- Backend integration tests.
- Frontend checks once tooling exists.
- E2E smoke tests once the integrated app is available.
- Coverage reporting for backend business logic.

No CI test should require real AI credentials.
