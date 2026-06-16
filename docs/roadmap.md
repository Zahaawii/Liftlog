# Roadmap

## Planning Assumptions

- The first release targets a small-to-medium production application.
- The stack is Spring Boot, MySQL, Docker, HTML, CSS, and Vanilla JavaScript.
- Architecture must follow the repository rules in `/ai`.
- The initial product should prioritize maintainability, testing, and clear documentation over advanced features.

## Milestones

### Milestone 0: Architecture and Project Foundation

Deliverables:

- Product requirements document.
- Architecture overview.
- Database design.
- API specification.
- Testing strategy.
- ADRs for major decisions.
- Initial repository structure agreement.

Exit criteria:

- Approved authentication, AI provider, AI history, and exercise model decisions are recorded.
- Architecture, database, and API contracts are accepted.
- Implementation order is agreed.

### Milestone 1: Application Skeleton

Deliverables:

- Backend and frontend project skeletons.
- Docker local development setup.
- Environment variable documentation.
- Health check endpoint.
- Standard error response infrastructure.
- Initial auth table SQL initialization.
- User registration and login.
- BCrypt password hashing.
- Secure HTTP-only cookie authentication.
- 15-minute access token and 7-day refresh token behavior.
- CSRF foundation for protected state-changing auth requests.
- Authenticated request handling without `localStorage` or `sessionStorage` token storage.
- Current-user endpoint.
- Authentication integration tests.
- Initial CI test command.

Dependencies:

- Approved monorepo structure.
- Approved deployment assumptions.

### Milestone 2: Authorization Hardening

Deliverables:

- Expand CSRF protection coverage as new state-changing user-owned resource endpoints are added.
- User-owned resource authorization pattern.
- Cross-domain authorization integration tests.

Dependencies:

- Approved authentication strategy.
- Approved cookie, refresh, and CSRF strategy.
- User schema.
- Error response standard.

### Milestone 3: Workout and Exercise Tracking

Deliverables:

- Global shared exercise library.
- Workout CRUD with exercises and sets.
- Workout exercise references to shared exercise records.
- Paginated workout history.
- Exercise progression endpoint.
- Workout and progression tests.

Dependencies:

- Authentication.
- Workout schema.
- Exercise schema.

### Milestone 4: Nutrition Tracking

Deliverables:

- Nutrition log CRUD.
- Daily nutrition summary.
- Paginated nutrition history.
- Nutrition validation and integration tests.

Dependencies:

- Authentication.
- Nutrition schema.

### Milestone 5: Goal Tracking and Dashboard

Deliverables:

- Goal CRUD.
- Goal check-ins.
- Goal progress calculations.
- Dashboard summary endpoint.
- Dashboard frontend integration.
- Goal and dashboard tests.

Dependencies:

- Workout and nutrition domains.
- Goal schema.

### Milestone 6: AI Feedback

Deliverables:

- AI provider abstraction.
- OpenAI provider implementation.
- Prompt builder.
- Configurable provider selection for future Gemini and Claude support.
- AI feedback endpoint.
- AI feedback history stored indefinitely with `id`, `userId`, `promptSummary`, `feedback`, and `createdAt`.
- Graceful provider failure handling.
- Stub provider for automated tests.
- AI feedback frontend integration.

Dependencies:

- Authentication.
- Workout, nutrition, goal, and dashboard summaries.
- OpenAI environment configuration.

### Milestone 7: Production Readiness

Deliverables:

- Deployment guide.
- Environment variable guide.
- HTTPS deployment configuration.
- Logging review.
- Test coverage review.
- Security review.
- Known limitations and future improvements documentation.

Dependencies:

- Feature completion.
- Production hosting decision.

## Development Phases

### Phase 1: Foundations

Build structural pieces that all features depend on:

- Backend layering.
- Frontend module conventions.
- Shared API client.
- Error handling standard.
- Authentication infrastructure.
- Test framework setup.

### Phase 2: Core Tracking

Build the non-AI product value:

- Workouts.
- Exercises.
- Nutrition.
- Goals.
- Dashboard.

### Phase 3: AI Advisory Layer

Add AI after core data is reliable:

- Provider abstraction.
- Prompt building.
- Feedback request and response.
- Failure handling.
- Cost and rate-limit controls.

### Phase 4: Hardening

Prepare for production:

- Integration and end-to-end test coverage.
- Logging and observability review.
- Security review.
- Deployment documentation.
- Performance review for common queries.

## Estimated Implementation Order

1. Create monorepo skeleton.
2. Add Docker Compose for backend and MySQL.
3. Add backend common error handling and validation conventions.
4. Add secure cookie authentication, refresh flow, CSRF handling, and current-user flow.
5. Add frontend API client and auth module without browser token storage.
6. Add user profile support.
7. Add global exercise library model and endpoints.
8. Add workout model and endpoints referencing exercise records.
9. Add workout frontend workflows.
10. Add exercise progression calculations.
11. Add nutrition model and endpoints.
12. Add nutrition frontend workflows.
13. Add goal model, progress services, and endpoints.
14. Add dashboard summary endpoint and frontend dashboard.
15. Add AI provider abstraction and fake provider.
16. Add OpenAI provider implementation.
17. Add AI feedback history persistence and endpoints.
18. Add AI feedback frontend workflow.
19. Complete production deployment documentation.
20. Run hardening pass for security, tests, logging, and performance.

## Dependencies

- Authentication must precede user-owned resource features.
- Database design must be accepted before broader schema migrations are written.
- Formal migration tooling should be introduced before implementing workout, nutrition, goal, exercise, dashboard, or AI feedback tables.
- API contracts must be accepted before frontend implementation depends on them.
- Global shared exercises must exist before workout creation can reference exercise records.
- Dashboard depends on workout, nutrition, and goal services.
- AI feedback depends on stable domain summaries, OpenAI configuration, and approved privacy rules.
- End-to-end tests depend on representative frontend and backend flows.

## Risks

- Secure cookie authentication reduces token exposure but adds CSRF and cookie configuration complexity.
- Indefinite AI data retention improves user history but increases future privacy, export, and deletion obligations.
- OpenAI provider cost and latency must be controlled with bounded prompts, failure handling, and likely rate limits.
- Vanilla JavaScript can become difficult to maintain if modules are not enforced early.
- Dashboard aggregation may become expensive without proper indexes and bounded responses.
- Goal types can sprawl; start with a small approved set and extend through service-level abstractions.
