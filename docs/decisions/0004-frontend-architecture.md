# ADR 0004: Frontend Architecture

## Status

Accepted

## Context

The project vision specifies a browser-based platform using HTML, CSS, and Vanilla JavaScript. The architecture principles require a modular Vanilla JavaScript frontend with shared API access and separation between API logic and UI logic.

The frontend must remain mobile-friendly and maintainable without introducing unnecessary framework complexity.

## Decision

Build the frontend with modular Vanilla JavaScript. Use a shared API client for backend communication and feature modules for auth, workouts, nutrition, dashboard, AI feedback, shared UI helpers, and errors.

Authentication is cookie-based. The frontend must not store access tokens or refresh tokens in `localStorage` or `sessionStorage`. The shared API client is responsible for sending cookie credentials and CSRF data for state-changing requests where required.

Recommended module areas:

- `api`
- `auth`
- `workouts`
- `nutrition`
- `dashboard`
- `ai`
- `shared`

## Consequences

Positive:

- Matches the approved technology vision.
- Avoids framework setup and dependency overhead.
- Keeps initial delivery simple.
- Makes API usage consistent through one client module.
- Keeps authentication token handling centralized and out of feature modules.

Negative:

- Requires discipline around module boundaries and shared state.
- Complex frontend interactions may eventually require stronger state patterns.
- Testing and build tooling must be chosen carefully if the frontend grows.
- Cookie authentication requires the frontend API client to handle CSRF concerns consistently.

## Guardrails

- API calls must go through the shared API client.
- Auth tokens must never be stored in browser-accessible storage.
- UI modules should not duplicate transport or error parsing logic.
- User-facing errors must be readable and non-technical.
- Backend validation remains authoritative.
