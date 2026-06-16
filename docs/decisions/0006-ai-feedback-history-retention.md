# ADR 0006: AI Feedback History Retention

## Status

Accepted

## Context

LiftLog AI needs AI-powered fitness feedback that users can revisit later. The product also needs to avoid sending or storing unnecessary sensitive data. The approved product decision is to store AI feedback history in the database and keep it indefinitely for now.

## Decision

Store AI feedback history as user-owned database records. Each stored record must include at minimum:

- `id`
- `userId`
- `promptSummary`
- `feedback`
- `createdAt`

The system should store a safe prompt summary rather than full sensitive prompts. OpenAI is the first provider, but stored feedback records should use provider-neutral fields so future Gemini or Claude integrations do not change history APIs.

## Consequences

Positive:

- Users can review prior AI feedback.
- Dashboard and future coaching features can reference recent feedback.
- Provider-neutral storage keeps history independent from OpenAI-specific response formats.

Negative:

- Indefinite retention increases privacy, storage, export, and deletion obligations.
- Prompt summaries must be carefully designed to avoid storing unnecessary sensitive data.
- Future privacy requirements may require retention controls, user deletion workflows, or export features.

## Guardrails

- AI feedback history must be scoped by authenticated user.
- Do not store credentials, tokens, secrets, or full sensitive prompts.
- Do not store provider raw responses unless a future privacy and operational decision explicitly approves it.
- Automated tests must verify that users can only access their own AI feedback history.
