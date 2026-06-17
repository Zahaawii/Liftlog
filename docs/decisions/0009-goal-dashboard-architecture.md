# ADR 0009: Goal and Dashboard Architecture

## Status

Accepted

## Context

Milestone 4 adds goal tracking and dashboard summaries after workout and nutrition tracking. The product requires goal CRUD, check-ins, deterministic progress, a dashboard summary endpoint, frontend integration, user isolation, CSRF protection for mutations, and tests. The architecture rules require thin controllers, service-owned business logic, repositories for persistence, DTOs at API boundaries, and documented decisions.

## Decision

Implement goals as a cohesive `goal` backend domain backed by normalized `goals` and `goal_check_ins` tables. Goals are user-owned through `goals.user_id`; check-ins belong to goals and are deleted with their parent goal. The physical check-in value column is `check_in_value` to avoid reserved-word conflicts across MySQL and H2.

Goal progress is calculated in `GoalService`:

- Workout-count metrics read authenticated-user workouts in the goal date window.
- Daily-calorie metrics read authenticated-user nutrition totals.
- Generic metrics use the latest check-in value, then the current baseline, then zero.

Implement dashboard summaries as a separate `dashboard` backend domain. `DashboardService` composes bounded summaries from existing workout, nutrition, and goal services/repositories. Dashboard does not own workout, nutrition, or goal business rules.

Frontend support is implemented with modular Vanilla JavaScript:

- `dashboard` renders server-computed dashboard summaries.
- `goals` handles goal creation, check-ins, and goal list rendering.
- `auth` coordinates module initialization and clearing.
- All API calls continue through the shared API client.

## Consequences

Positive:

- Goal ownership and dashboard scoping are explicit and testable.
- Goal progress rules are centralized and deterministic.
- Dashboard composition can evolve without moving domain rules into the dashboard layer.
- The frontend remains aligned with the approved modular Vanilla JavaScript approach.

Negative:

- Goal metric handling is intentionally simple and will need extension as product goal types become richer.
- Dashboard currently performs live reads rather than using precomputed summaries.
- Goal status is stored independently from progress percentage, so future auto-completion rules need a clear product decision.

## Guardrails

- Do not expose JPA entities from controllers.
- Keep all goal and dashboard reads scoped by authenticated user ID.
- Keep dashboard responses bounded.
- Keep state-changing goal endpoints under CSRF coverage.
- Add migration tooling before production deployment or the next schema-heavy milestone.
