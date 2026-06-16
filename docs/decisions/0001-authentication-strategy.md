# ADR 0001: Authentication Strategy

## Status

Accepted

## Context

LiftLog AI needs secure authentication for a browser-based REST application. Users must only access their own workouts, nutrition logs, goals, dashboard data, and AI feedback. Passwords must be hashed with BCrypt, secrets must come from environment variables, and credentials or tokens must not be logged.

The project needs an approach that is realistic for a small-to-medium production application and works cleanly with a Spring Boot backend and Vanilla JavaScript frontend.

## Decision

Use token-based authentication delivered through secure HTTP-only cookies for protected REST APIs, with dedicated authentication infrastructure responsible for credential validation, BCrypt password hashing, token creation, token validation, refresh behavior, and CSRF handling.

Token rules:

- Access token lifetime is 15 minutes.
- Refresh token lifetime is 7 days.
- Access and refresh tokens must not be stored in `localStorage` or `sessionStorage`.
- Auth response bodies must not include token material.
- The refresh strategy must keep users logged in without exposing long-lived access tokens to browser JavaScript.
- CSRF protection must be applied to cookie-authenticated state-changing requests where necessary.

The backend will treat authentication and authorization as separate concerns. Authentication identifies the user. Authorization verifies ownership for user-owned resources.

## Consequences

Positive:

- Fits REST API boundaries.
- Keeps access tokens short-lived while preserving user sessions through refresh.
- Makes frontend API integration straightforward.
- Keeps authentication logic isolated from domain services.
- Reduces token exposure to browser JavaScript.

Negative:

- Cookie-based tokens require CSRF controls.
- Cookie settings must be carefully configured for Secure, HttpOnly, SameSite, path, and expiry behavior.
- Refresh-token invalidation and rotation need explicit implementation discipline.

## Implementation Guardrails

- Never log credentials, password hashes, access tokens, or refresh tokens.
- Prefer server-side refresh-token tracking or rotation so logout and suspicious activity can invalidate refresh state.
- Integration tests must verify cookie flags, token lifetimes, refresh behavior, and CSRF behavior.
