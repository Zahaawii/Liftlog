# Database Design

## Design Goals

- Represent core fitness domains clearly.
- Keep user-owned data easy to authorize and query.
- Support workout history, exercise progression, nutrition totals, goal progress, dashboard summaries, and AI feedback.
- Avoid premature complexity while leaving room for future expansion.
- Use MySQL as the system of record.

## Proposed Entities

### User

Represents an application account.

Key attributes:

- id
- email
- password_hash
- display_name
- created_at
- updated_at
- last_login_at
- status

Notes:

- Email must be unique.
- Password hash must use BCrypt.
- Password hashes must never be returned by APIs.

### User Profile

Stores optional user fitness context separately from authentication credentials.

Key attributes:

- id
- user_id
- height
- weight
- birth_year
- activity_level
- fitness_experience
- created_at
- updated_at

Notes:

- Profile fields should be optional in the initial version.
- AI prompts should use only necessary profile fields.

### Exercise

Represents an exercise in the global shared exercise library.

Key attributes:

- id
- name
- category
- primary_muscle_group
- measurement_type
- is_active
- source
- created_at
- updated_at

Notes:

- Workout data references exercise records instead of duplicating exercise names.
- The initial version uses global shared exercises.
- Optional custom user-created exercises can be added later without changing workout history references.
- `measurement_type` helps distinguish weight/reps, duration, distance, or mixed tracking.

### Custom Exercise

Represents a future optional user-created exercise while preserving the same reference pattern as global exercises.

Key attributes:

- id
- user_id
- base_exercise_id
- name
- category
- primary_muscle_group
- measurement_type
- is_custom
- created_at
- updated_at

Notes:

- This entity is a future extension point, not required for the first implementation milestone.
- If introduced, workout exercise references should support either a global exercise or an approved custom exercise without duplicating names in workout rows.

### Workout

Represents one completed or planned workout session.

Key attributes:

- id
- user_id
- workout_date
- title
- notes
- created_at
- updated_at

### Workout Exercise

Represents an exercise performed within a workout.

Key attributes:

- id
- workout_id
- exercise_id
- display_order
- notes
- created_at
- updated_at

### Workout Set

Represents a set or effort entry for a workout exercise.

Key attributes:

- id
- workout_exercise_id
- set_number
- reps
- weight
- duration_seconds
- distance
- completed
- notes
- created_at
- updated_at

Notes:

- Different exercises may use different metric combinations.
- Progress calculations should use completed sets only.

### Nutrition Log

Represents a food or meal entry.

Key attributes:

- id
- user_id
- log_date
- meal_type
- food_name
- serving_quantity
- calories
- protein
- carbohydrates
- fat
- notes
- created_at
- updated_at

### Goal

Represents a measurable user goal.

Key attributes:

- id
- user_id
- goal_type
- title
- target_metric
- target_value
- current_baseline
- start_date
- target_date
- status
- created_at
- updated_at

Notes:

- Goal types may include workout frequency, strength target, nutrition target, body weight, or custom.
- Goal progress should be calculated by services from source data where possible.

### Goal Check-In

Represents optional user-entered or system-derived snapshots for goals.

Key attributes:

- id
- goal_id
- check_in_date
- value
- notes
- created_at

Notes:

- Useful for goals that are not fully derivable from workout or nutrition records.

### AI Feedback Request

Stores user-owned AI feedback history.

Key attributes:

- id
- user_id
- prompt_summary
- feedback
- created_at

Additional recommended attributes:

- request_type
- provider
- status
- completed_at
- error_code

Notes:

- AI feedback history is stored indefinitely for now.
- Minimum required fields are `id`, `userId`, `promptSummary`, `feedback`, and `createdAt`.
- OpenAI is the first provider, but provider-specific raw responses should not be stored unless a clear operational need and privacy policy exist.
- Avoid storing full sensitive prompts; store a safe prompt summary for history.

## Relationships

- User has one optional User Profile.
- User can have many Custom Exercises in a future extension.
- User has many Workouts.
- User has many Nutrition Logs.
- User has many Goals.
- User has many AI Feedback Requests.
- Workout has many Workout Exercises.
- Workout Exercise belongs to one Workout.
- Workout Exercise references one global Exercise in the initial version.
- Workout Exercise may reference a Custom Exercise in a future extension.
- Workout Exercise has many Workout Sets.
- Goal has many Goal Check-Ins.

## ERD Description

The `user` table is the ownership root for user-specific records. Workouts, nutrition logs, goals, future custom exercises, and AI feedback requests include a `user_id` so application queries can enforce user isolation directly.

The global `exercise` table stores the shared exercise library. Workout details are modeled in three levels: `workout`, `workout_exercise`, and `workout_set`. `workout_exercise` references an exercise record instead of duplicating exercise names, which keeps progression reporting consistent if display names or metadata change.

Nutrition logs are date-based entries owned by a user. Dashboard and daily total views aggregate these records by user and date.

Goals are user-owned records that define target intent. Goal progress may be calculated from workouts, nutrition logs, check-ins, or goal-specific source data depending on goal type.

AI feedback records are linked to users and store feedback history indefinitely for now. Each record must include a safe prompt summary and feedback content. Provider metadata can be stored to support OpenAI-first operations while preserving the provider abstraction for Gemini and Claude later.

## Index Recommendations

### User

- Unique index on `email`.
- Index on `status` if account state filtering becomes common.

### Exercise

- Unique or lookup index on `name`.
- Index on `category`.
- Index on `primary_muscle_group`.
- Index on `is_active`.

### Custom Exercise

- Composite index on `user_id, name`.
- Index on `base_exercise_id`.

### Workout

- Composite index on `user_id, workout_date`.
- Composite index on `user_id, created_at` for paginated history.

### Workout Exercise

- Index on `workout_id`.
- Index on `exercise_id`.

### Workout Set

- Index on `workout_exercise_id`.

### Nutrition Log

- Composite index on `user_id, log_date`.
- Composite index on `user_id, meal_type, log_date` if meal filtering is common.

### Goal

- Composite index on `user_id, status`.
- Composite index on `user_id, goal_type`.
- Index on `target_date` if reminders or scheduled processing are added later.

### Goal Check-In

- Composite index on `goal_id, check_in_date`.

### AI Feedback Request

- Composite index on `user_id, created_at`.
- Composite index on `provider, status` for operational review.

## Data Integrity Rules

- User-owned child records should use foreign keys to preserve ownership relationships.
- Workout exercise records must reference exercise records rather than storing duplicated exercise names.
- Deleting a user should require an explicit data retention policy before implementation.
- Deleting a workout should delete related workout exercises and sets.
- Deleting a goal should delete related check-ins unless retention requirements say otherwise.
- Numeric fitness values should reject impossible negative values.
- Dates should be validated at the API and service layer.

## Milestone 1 Implementation Note

Milestone 1 creates only the authentication tables needed for registration, login, and refresh-token tracking. The implementation uses Spring SQL initialization for the initial `users` and `refresh_tokens` tables. Before workout, nutrition, goals, AI history, or exercise library tables are implemented, the project should adopt formal migration tooling so schema changes are versioned consistently.

## Future Scalability Considerations

- Add custom user-created exercises after the global shared exercise library and workout references are stable.
- Introduce read-optimized summary tables if dashboard queries become expensive.
- Revisit indefinite AI feedback retention once privacy, export, and deletion requirements are formalized.
- Consider event-based analytics only after core CRUD and reporting needs are stable.
- Use cursor pagination for high-volume history endpoints if offset pagination becomes slow.
- Keep provider raw response storage out of scope unless retention, privacy, and operational requirements explicitly justify it.
