# API Specification

## API Conventions

- Base path: `/api`
- Format: JSON request and response bodies.
- Authentication required for all endpoints except registration and login.
- Controllers return DTOs, not persistence entities.
- Request DTOs should be validated server-side.
- Large collections must support pagination.

## Authentication Strategy

Approved strategy: secure HTTP-only cookie authentication for REST APIs.

Client behavior:

- Register or log in through `/api/auth`.
- Send authenticated requests with browser cookies through the shared API client.
- Do not store access tokens or refresh tokens in `localStorage` or `sessionStorage`.
- Include CSRF protection data for cookie-authenticated state-changing requests where required.
- Clear client-visible user state on logout.

Backend behavior:

- Hash passwords with BCrypt.
- Validate credentials without leaking whether an email exists.
- Set secure HTTP-only cookies after successful login or refresh.
- Use a 15-minute access token.
- Use a 7-day refresh token.
- Refresh short-lived access tokens without exposing long-lived tokens to browser JavaScript.
- Apply CSRF protection to cookie-authenticated state-changing requests where necessary.
- Validate tokens from cookies on protected requests.
- Resolve the authenticated user for service-layer authorization.
- Never log credentials, password hashes, or tokens.

Auth responses must not include token material in JSON bodies.

## Standard Error Response

All backend API errors should follow:

```json
{
  "timestamp": "2026-06-16T12:00:00Z",
  "status": 400,
  "errorCode": "WORKOUT_INVALID_INPUT",
  "message": "Workout date is required.",
  "path": "/api/workouts"
}
```

Rules:

- Do not expose stack traces.
- Use stable error codes.
- Use readable user-safe messages.
- Log technical details internally.
- Handle validation, authentication, and authorization errors consistently.

Common status codes:

- `200 OK`: Successful read or update.
- `201 Created`: Successful creation.
- `204 No Content`: Successful deletion.
- `400 Bad Request`: Invalid request.
- `401 Unauthorized`: Missing or invalid authentication.
- `403 Forbidden`: Authenticated user cannot access the resource.
- `404 Not Found`: Resource does not exist or should not be revealed.
- `409 Conflict`: Duplicate or conflicting state.
- `422 Unprocessable Entity`: Valid JSON with domain validation failure.
- `429 Too Many Requests`: Rate limit exceeded.
- `500 Internal Server Error`: Unexpected server failure.
- `502 Bad Gateway`: External provider failure.
- `503 Service Unavailable`: Temporarily unavailable dependency.

## Pagination Strategy

Default collection query parameters:

- `page`: zero-based page number, default `0`.
- `size`: page size, default `20`, maximum `100`.
- `sort`: optional sort field.
- `direction`: `asc` or `desc`.

Standard paginated response:

```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "totalItems": 125,
  "totalPages": 7,
  "hasNext": true,
  "hasPrevious": false
}
```

Cursor pagination can be introduced later for high-volume history endpoints without changing resource semantics.

## Request and Response Models

### Auth Models

Register request:

```json
{
  "email": "user@example.com",
  "password": "strong-password",
  "displayName": "Alex"
}
```

Login request:

```json
{
  "email": "user@example.com",
  "password": "strong-password"
}
```

Auth response:

```json
{
  "user": {
    "id": "public-user-id",
    "email": "user@example.com",
    "displayName": "Alex"
  }
}
```

The server sets secure HTTP-only cookies for authentication. Token material is not returned in the response body.

### Workout Models

Workout request:

```json
{
  "workoutDate": "2026-06-16",
  "title": "Upper Body",
  "notes": "Felt strong",
  "exercises": [
    {
      "exerciseId": "exercise-id",
      "notes": "Controlled tempo",
      "sets": [
        {
          "setNumber": 1,
          "reps": 8,
          "weight": 80.0,
          "completed": true,
          "notes": ""
        }
      ]
    }
  ]
}
```

Workout response:

```json
{
  "id": "workout-id",
  "workoutDate": "2026-06-16",
  "title": "Upper Body",
  "notes": "Felt strong",
  "exercises": [
    {
      "id": "workout-exercise-id",
      "exercise": {
        "id": "exercise-id",
        "name": "Bench Press"
      },
      "sets": [
        {
          "id": "set-id",
          "setNumber": 1,
          "reps": 8,
          "weight": 80.0,
          "completed": true
        }
      ]
    }
  ]
}
```

### Nutrition Models

Nutrition log request:

```json
{
  "logDate": "2026-06-16",
  "mealType": "lunch",
  "foodName": "Chicken rice bowl",
  "servingQuantity": 1.0,
  "calories": 650,
  "protein": 45.0,
  "carbohydrates": 70.0,
  "fat": 18.0,
  "notes": ""
}
```

Nutrition log response:

```json
{
  "id": "nutrition-log-id",
  "logDate": "2026-06-16",
  "mealType": "lunch",
  "foodName": "Chicken rice bowl",
  "calories": 650,
  "protein": 45.0,
  "carbohydrates": 70.0,
  "fat": 18.0
}
```

Daily nutrition summary response:

```json
{
  "date": "2026-06-16",
  "calories": 2100,
  "protein": 160.0,
  "carbohydrates": 220.0,
  "fat": 70.0
}
```

### Goal Models

Goal request:

```json
{
  "goalType": "workout_frequency",
  "title": "Train 4 times per week",
  "targetMetric": "weekly_workout_count",
  "targetValue": 4,
  "startDate": "2026-06-16",
  "targetDate": "2026-09-16"
}
```

Goal response:

```json
{
  "id": "goal-id",
  "goalType": "workout_frequency",
  "title": "Train 4 times per week",
  "targetMetric": "weekly_workout_count",
  "targetValue": 4,
  "currentValue": 3,
  "progressPercent": 75,
  "status": "active"
}
```

### Dashboard Models

Dashboard summary response:

```json
{
  "weeklyWorkoutCount": 3,
  "recentWorkouts": [],
  "nutritionToday": {
    "calories": 2100,
    "protein": 160.0,
    "carbohydrates": 220.0,
    "fat": 70.0
  },
  "activeGoals": [],
  "latestAiFeedback": null
}
```

### AI Feedback Models

AI feedback request:

```json
{
  "requestType": "weekly_review",
  "question": "What should I focus on next week?",
  "includeWorkouts": true,
  "includeNutrition": true,
  "includeGoals": true
}
```

AI feedback response:

```json
{
  "id": "feedback-id",
  "userId": "public-user-id",
  "promptSummary": "Weekly review using recent workouts, nutrition totals, and active goals.",
  "summary": "Your weekly consistency is improving.",
  "recommendations": [
    "Keep workout frequency stable before increasing volume.",
    "Prioritize protein consistency on rest days."
  ],
  "feedback": "Your weekly consistency is improving. Keep workout frequency stable before increasing volume.",
  "disclaimer": "This feedback is informational and is not medical advice.",
  "createdAt": "2026-06-16T12:00:00Z"
}
```

## REST Endpoints

### Authentication

- `POST /api/auth/register`: create account.
- `POST /api/auth/login`: authenticate and set secure HTTP-only auth cookies.
- `POST /api/auth/refresh`: refresh the short-lived access token using the 7-day refresh token cookie.
- `POST /api/auth/logout`: clear auth cookies and invalidate refresh state where server-side refresh tracking is implemented.
- `GET /api/auth/me`: return current authenticated user.
- `GET /api/auth/csrf`: provide CSRF data if the chosen CSRF mechanism requires a readable token for state-changing requests.

### User Profile

- `GET /api/users/me`: get current user profile.
- `PUT /api/users/me`: update current user profile.

### Exercises

- `GET /api/exercises`: list global shared exercises.
- `GET /api/exercises/{id}`: get exercise details.
- `GET /api/exercises/{id}/progression`: get progression metrics.

Future custom exercise endpoints:

- `POST /api/exercises/custom`: create custom exercise.
- `GET /api/exercises/custom`: list current user's custom exercises.
- `PUT /api/exercises/custom/{id}`: update custom exercise.
- `DELETE /api/exercises/custom/{id}`: delete custom exercise if not blocked by history rules.

### Workouts

- `GET /api/workouts`: paginated workout history.
- `POST /api/workouts`: create workout.
- `GET /api/workouts/{id}`: get workout details.
- `PUT /api/workouts/{id}`: update workout.
- `DELETE /api/workouts/{id}`: delete workout.

### Nutrition

- `GET /api/nutrition/logs`: paginated nutrition logs.
- `POST /api/nutrition/logs`: create nutrition log.
- `GET /api/nutrition/logs/{id}`: get nutrition log.
- `PUT /api/nutrition/logs/{id}`: update nutrition log.
- `DELETE /api/nutrition/logs/{id}`: delete nutrition log.
- `GET /api/nutrition/summary/daily?date=YYYY-MM-DD`: daily nutrition totals.

### Goals

- `GET /api/goals`: list goals.
- `POST /api/goals`: create goal.
- `GET /api/goals/{id}`: get goal with progress.
- `PUT /api/goals/{id}`: update goal.
- `DELETE /api/goals/{id}`: delete goal.
- `POST /api/goals/{id}/check-ins`: create goal check-in.
- `GET /api/goals/{id}/check-ins`: list goal check-ins.

### Dashboard

- `GET /api/dashboard/summary`: get current user's dashboard summary.

### AI Feedback

- `POST /api/ai/feedback`: request AI feedback.
- `GET /api/ai/feedback`: list current user's AI feedback history.
- `GET /api/ai/feedback/{id}`: get a feedback result.

## API Risks and Trade-Offs

- HTTP-only cookies reduce script access to tokens but require CSRF protections and careful SameSite, Secure, path, and expiry settings.
- Offset pagination is simple for initial development but may need cursor pagination for large histories.
- OpenAI-backed feedback endpoints may need rate limits to control cost and provider abuse.
- Indefinite AI feedback history improves continuity but requires future privacy, export, and deletion policy decisions.
- Public IDs or opaque IDs should be considered before exposing internal numeric IDs in APIs.
