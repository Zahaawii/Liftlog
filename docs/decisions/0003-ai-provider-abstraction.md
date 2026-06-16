# ADR 0003: AI Provider Abstraction

## Status

Accepted

## Context

LiftLog AI includes AI-powered fitness feedback. LLM provider APIs can change, fail, become expensive, or need replacement. The architecture principles require AI logic to be isolated behind a provider interface and state that controllers must not call AI providers directly.

AI feedback must be advisory, privacy-conscious, and testable without real external calls.

## Decision

Introduce an AI integration boundary:

```text
AI controller -> AiFeedbackService -> PromptBuilder -> AiProvider -> provider implementation
```

The application service depends on an `AiProvider` abstraction, not on a concrete external SDK. Provider-specific request formats, credentials, error mapping, and response parsing stay inside provider implementations. A fake provider must be available for automated tests and local workflows that do not require real AI calls.

OpenAI is the first production provider implementation. Gemini and Claude may be added later by implementing the same provider contract without changing controllers or core business logic.

## Consequences

Positive:

- Supports switching providers through configuration.
- Keeps controllers and services insulated from provider SDK changes.
- Enables deterministic tests.
- Reduces privacy risk by centralizing prompt construction and data minimization.
- Allows OpenAI-specific behavior to stay out of controllers and domain services.

Negative:

- Adds a small abstraction cost before multiple providers exist.
- The internal response model may not expose every provider-specific feature.
- Prompt design must be maintained as product behavior evolves.
- OpenAI cost, latency, and availability become production concerns for the first provider rollout.

## Guardrails

- Do not send passwords, tokens, secrets, or unnecessary personal data to AI providers.
- Do not log full prompts containing sensitive user data.
- Treat feedback as advisory, not medical truth.
- Handle provider failures with stable application errors.
- OpenAI credentials must come from environment variables.
- Automated tests must use fake or stub providers, not real OpenAI calls.
