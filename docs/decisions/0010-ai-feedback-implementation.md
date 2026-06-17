# ADR 0010: AI Feedback Implementation

## Status

Accepted

## Context

Milestone 5 adds AI-powered fitness feedback after workout, nutrition, goals, and dashboard data are available. The product requires OpenAI as the first provider, an abstraction that can support Gemini and Claude later, stored AI feedback history, graceful provider failure handling, and deterministic tests that do not call real external AI APIs.

The architecture rules require controllers to stay thin, business logic to live in services, provider-specific code to stay isolated, prompts to avoid secrets and unnecessary personal data, and public APIs to use DTOs.

## Decision

Implement AI feedback as a cohesive `ai` backend domain:

- `AiFeedbackController` exposes feedback request and history endpoints.
- `AiFeedbackService` gathers bounded domain summaries, builds prompts, calls the provider abstraction, persists successful feedback, maps provider failures, and enforces user scoping.
- `PromptBuilder` owns prompt construction and safe prompt summaries.
- `AiProvider` defines the provider contract.
- `OpenAiProvider` owns OpenAI-specific HTTP request, response parsing, configuration, and failure mapping.
- `StubAiProvider` supports automated tests and local workflows without external AI calls.
- `AiFeedbackRepository` persists and queries user-owned history records.

AI feedback history is stored in `ai_feedback` indefinitely for now. Stored records include the required `id`, `userId`, `promptSummary`, `feedback`, and `createdAt` fields plus provider-neutral metadata such as request type, provider, status, summary, recommendations, completed time, and error code.

Full prompts and provider raw responses are not stored. Prompt summaries describe the data sources and request type without preserving raw custom questions.

## Consequences

Positive:

- Controllers and core business logic do not depend on OpenAI.
- Tests are deterministic through the stub provider.
- Future Gemini or Claude implementations can be added behind the same provider contract.
- Stored history supports user review and dashboard latest-feedback summaries.
- Provider failures return stable API errors without exposing provider internals.

Negative:

- The OpenAI response parser is intentionally simple and expects either structured JSON content or falls back to the returned text.
- Real production usage still needs rate limiting and cost controls.
- Indefinite retention increases privacy, export, and deletion obligations.
- Provider-neutral response fields may not capture every provider-specific feature.

## Guardrails

- Do not log credentials, API keys, full prompts, provider raw responses, or token material.
- Do not store full prompts containing sensitive data.
- Keep AI feedback scoped by authenticated user ID.
- Keep automated tests on the stub provider unless explicitly testing local failure mapping.
- Keep OpenAI credentials in environment variables.
- Add rate limiting before production exposure.
