# Deployment Guide

## Local Development

Run the full stack:

```bash
docker compose up --build
```

Local URLs:

- Frontend: `http://localhost:8081`
- Backend health: `http://localhost:8080/api/health`
- Actuator health: `http://localhost:8080/actuator/health`
- MySQL: `localhost:3307`

Run backend tests:

```bash
MAVEN_USER_HOME=/tmp/m2 ./mvnw -pl backend test
```

## Production Deployment Shape

Recommended production components:

- Static frontend served by Nginx, CDN, or equivalent static host.
- Spring Boot backend container.
- Managed MySQL database.
- HTTPS termination at load balancer, reverse proxy, or platform edge.
- Environment variables or managed secrets for database credentials, auth token secret, and OpenAI API key.
- Centralized log aggregation.

## Production Configuration

Set at minimum:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `LIFTLOG_AUTH_TOKEN_SECRET`
- `LIFTLOG_COOKIE_SECURE=true`
- `LIFTLOG_AI_PROVIDER=openai`
- `OPENAI_API_KEY`

Use `OPENAI_MODEL` to change model selection without changing application code.

## Database Migrations

The current project uses Spring SQL initialization through `schema.sql` for local and test consistency. Before a real production release, introduce formal migration tooling such as Flyway or Liquibase and convert the current schema into versioned migrations.

Do not rely on ad hoc schema changes in production.

## Health Checks

Use:

- `/api/health` for basic API availability.
- `/actuator/health` for Spring Boot health reporting.

Production health checks should also verify database connectivity before routing traffic.

## HTTPS and Cookies

Production must run over HTTPS with:

- Secure HTTP-only auth cookies.
- `LIFTLOG_COOKIE_SECURE=true`.
- A SameSite policy appropriate to the deployed frontend/backend origin.
- CSRF headers on cookie-authenticated state-changing requests.

## AI Provider Operations

- Keep OpenAI credentials in environment variables.
- Monitor AI provider failures and latency.
- Add rate limiting before public production exposure.
- Keep automated tests on the stub provider.
- Do not log full prompts or provider raw responses.
