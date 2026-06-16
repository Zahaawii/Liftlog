# ADR 0005: Database Design Approach

## Status

Accepted

## Context

LiftLog AI uses MySQL and needs to support authentication, workout tracking, exercise progression, nutrition tracking, goal tracking, dashboard summaries, and AI feedback. User data must be isolated by ownership, and common history views must be paginated.

The design should be realistic for a small-to-medium production application and should not over-optimize before usage patterns are known.

## Decision

Use a normalized relational schema centered on the user as the ownership root for user-owned data. Workouts are modeled as workout, workout exercise, and workout set records. Workout exercise rows reference a global shared exercise library instead of duplicating exercise names. Nutrition logs and goals are user-owned records. AI feedback history is stored as user-owned records.

User-owned tables should include owner references and indexes that support common access patterns such as history by date, active goals, daily nutrition totals, AI feedback history, and dashboard summaries.

Optional custom user-created exercises are a future extension point. The model should support adding them later without forcing workout history to store denormalized exercise names.

## Consequences

Positive:

- Clear ownership supports authorization.
- Normalized workout data supports progression calculations.
- Shared exercise records keep progression reporting consistent.
- MySQL indexing can handle initial reporting needs.
- The model remains understandable and maintainable.

Negative:

- Dashboard queries may need optimization as data grows.
- Highly flexible goal types can complicate validation and progress calculations.
- Indefinite AI feedback history increases privacy and future retention obligations.
- Custom exercises later may require additional reference rules or polymorphic association decisions.

## Guardrails

- Use DTOs for API input and output; do not expose entities directly.
- Add pagination to large history endpoints.
- Prefer indexes for common user/date/status access paths.
- Workout exercise data must reference exercise records and avoid duplicated exercise names.
- Avoid summary tables until query behavior proves they are needed.
