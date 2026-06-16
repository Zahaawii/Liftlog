# ADR 0008: Nutrition Tracking Architecture

## Status

Accepted

## Context

Milestone 3 adds nutrition tracking after authentication and workout tracking. The product requires nutrition log CRUD, paginated history, daily totals, user isolation, validation, and tests. The architecture rules require layered backend design, thin controllers, services for business logic, repositories for persistence, and DTOs at API boundaries.

## Decision

Implement nutrition tracking as a cohesive `nutrition` backend domain backed by a normalized `nutrition_logs` table.

Nutrition logs are user-owned through `nutrition_logs.user_id`. Each log stores a date, meal type, food name, optional serving quantity, calories, protein, carbohydrates, fat, notes, and timestamps. Daily totals are computed server-side from the authenticated user's logs for the requested date.

Backend code is split into controller, service, repository, DTO, and entity layers. Controllers accept validated DTOs and delegate to services. Services enforce ownership and domain validation. Repositories own persistence and aggregate queries. State-changing nutrition endpoints rely on the existing secure cookie authentication and CSRF protection.

## Consequences

Positive:

- User ownership is explicit and testable.
- Daily summaries are deterministic and reusable by the future dashboard.
- The data model is simple enough for the current product while supporting future reporting.
- Nutrition remains decoupled from workouts and goals.

Negative:

- Food entries are free-text for now, so there is no canonical food database.
- Daily totals depend on user-entered values and do not perform unit conversion.
- More advanced nutrition features may need food templates or a food library later.

## Guardrails

- Do not expose JPA entities from controllers.
- Keep nutrition queries scoped by authenticated user ID.
- Use pagination for nutrition history.
- Require calories or at least one macronutrient value for each log.
- Keep nutrition mutation tests under CSRF coverage.
- Add migration tooling before production deployment or the next schema-heavy milestone.
