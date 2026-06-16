# ADR 0002: Monorepo Structure

## Status

Accepted

## Context

The project vision specifies Spring Boot, MySQL, Docker, HTML, CSS, and Vanilla JavaScript. The architecture rules define a layered monorepo architecture with frontend, backend API, service layer, repository layer, database, and external integrations.

The repository should make responsibility boundaries visible while remaining simple for a small-to-medium application.

## Decision

Use a monorepo with separate top-level backend, frontend, documentation, and architecture guidance areas:

```text
backend/
frontend/
docs/
ai/
```

The backend will be organized by domain packages such as auth, user, workout, exercise, nutrition, goal, dashboard, ai, and common. Each domain may contain controller, service, repository, dto, entity, mapper, and exception areas as needed.

The frontend will be organized as modular Vanilla JavaScript with a shared API client and feature modules.

## Consequences

Positive:

- Keeps API, frontend, and docs together for coordinated changes.
- Makes architecture rules easy to enforce.
- Avoids distributed repository overhead at the current product size.
- Supports shared Docker and local development setup.

Negative:

- Requires discipline to avoid cross-domain coupling.
- CI may need path-aware steps as the project grows.
- Larger history can make repository operations slower over time.

## Guardrails

- Backend controllers must not contain business logic.
- Frontend modules must use the shared API client.
- Documentation must be updated when contracts or architecture change.

