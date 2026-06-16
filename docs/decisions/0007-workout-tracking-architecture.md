# ADR 0007: Workout Tracking Architecture

## Status

Accepted

## Context

Milestone 2 adds workout tracking after secure cookie authentication. The product requires workout logs with exercises and sets, shared exercise references, paginated history, user isolation, and tests. The architecture rules require layered backend design, thin controllers, services for business logic, repositories for persistence, and DTOs at API boundaries.

## Decision

Implement workout tracking with normalized relational tables:

- `exercises`
- `workouts`
- `workout_exercises`
- `workout_sets`

Workout records are user-owned through `workouts.user_id`. Workout exercise rows reference the global shared `exercises` table instead of duplicating exercise names. Workout set rows store flexible performance metrics for reps, weight, duration, and distance.

Backend code is split by domain:

- `exercise` owns shared exercise listing, details, seeding, and progression reads.
- `workout` owns workout CRUD, nested exercise/set persistence, ownership checks, validation, and response mapping.

Controllers accept validated DTOs and delegate to services. Services enforce ownership and domain validation. Repositories own persistence queries. State-changing workout endpoints rely on the existing secure cookie authentication and CSRF protection.

## Consequences

Positive:

- User ownership is explicit and testable.
- Shared exercise references support future progression and dashboard features.
- Nested workout details remain normalized and maintainable.
- The frontend can use one stable workout request/response shape.

Negative:

- Nested workout updates currently replace exercise/set children, which is simple but less granular than patching individual sets.
- Listing workouts with nested details can become query-heavy as history grows.
- The global exercise seed list is intentionally small and will need product curation later.

## Guardrails

- Do not expose JPA entities from controllers.
- Do not store duplicated exercise names in workout rows.
- Keep workout queries scoped by authenticated user ID.
- Use pagination for workout history.
- Keep workout mutation tests under CSRF coverage.
- Add migration tooling before production deployment or the next schema-heavy milestone.
