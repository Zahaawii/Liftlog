# Environment Variables

## Backend

Required for production:

- `DB_URL`: JDBC URL for MySQL.
- `DB_USERNAME`: database user.
- `DB_PASSWORD`: database password.
- `LIFTLOG_AUTH_TOKEN_SECRET`: at least 32 characters; must be unique per environment.
- `LIFTLOG_COOKIE_SECURE`: `true` in production.
- `LIFTLOG_COOKIE_SAME_SITE`: normally `Lax` for same-site deployments.
- `LIFTLOG_AI_PROVIDER`: `openai` in production, `stub` for local/test.
- `OPENAI_API_KEY`: required when `LIFTLOG_AI_PROVIDER=openai`.

Optional:

- `SERVER_PORT`: backend port, default `8080`.
- `OPENAI_MODEL`: default `gpt-4o-mini`.
- `OPENAI_ENDPOINT`: default `https://api.openai.com/v1/chat/completions`.
- `OPENAI_TIMEOUT_SECONDS`: default `30`.

## Local Docker Defaults

`docker-compose.yml` uses local development defaults:

- MySQL is exposed on host port `3307`.
- Backend is exposed on host port `8080`.
- Frontend is exposed on host port `8081`.
- `LIFTLOG_COOKIE_SECURE=false` so cookies work over local HTTP.
- `LIFTLOG_AI_PROVIDER=stub` so no external AI call is required.

## Secret Handling

- Do not commit production secrets.
- Use a managed secret store or deployment-platform environment variables.
- Rotate `LIFTLOG_AUTH_TOKEN_SECRET` carefully because existing auth cookies become invalid.
- Never log database passwords, auth token secrets, API keys, access tokens, refresh tokens, or full AI prompts.
