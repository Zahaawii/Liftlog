# User Flows

## Design Priorities

LiftLog AI should feel fast, focused, and practical on a phone. The interface should help users answer two questions quickly:

1. Am I getting stronger?
2. Am I hitting my protein target?

Secondary flows support those outcomes: workout logging, nutrition logging, goal tracking, and AI Coach review.

## Flow 1: Register and Start

Intent: Create an account and reach the dashboard without exposing authentication details.

Entry points:

- Public register screen
- Login screen secondary link

Steps:

1. User opens Register.
2. User enters display name, email, and password.
3. User submits the form.
4. UI shows validation errors inline if the request fails.
5. On success, user lands on Dashboard.

Success state:

- Dashboard shows empty-state guidance for first workout and first nutrition log.
- No token details are shown or stored in browser-accessible storage.

UX priorities:

- Keep the form short.
- Avoid distracting onboarding before the first useful action.
- Make "Log workout" and "Log protein" visible immediately after registration.

## Flow 2: Login and Resume

Intent: Return users to their current training and nutrition status.

Entry points:

- Public login screen
- Expired session redirect

Steps:

1. User enters email and password.
2. User submits the form.
3. UI shows a generic authentication error if login fails.
4. On success, user lands on Dashboard.

Success state:

- Dashboard highlights the latest strength progression and today's protein intake.
- Session refresh is invisible to the user unless authentication fully fails.

UX priorities:

- Keep error copy user-safe and non-technical.
- Do not display separate errors that reveal whether an email exists.

## Flow 3: Fast Workout Logging

Intent: Record a workout with the fewest possible taps while preserving structured exercise data.

Entry points:

- Dashboard quick action
- Workouts tab primary action
- Mobile bottom navigation central log action

Steps:

1. User taps Log Workout.
2. User confirms or edits workout date and title.
3. User searches or selects from the shared exercise library.
4. User sees the selected exercise with set rows.
5. User enters reps and weight, or duration and distance when appropriate.
6. User adds another set or exercise if needed.
7. User saves the workout.

Success state:

- Workout detail opens with saved exercises and sets.
- Related exercise progression is available from the detail screen.

UX priorities:

- Default to today.
- Use large numeric inputs for reps, weight, duration, and distance.
- Keep previous/best context near the active exercise when available.
- Let users add repeated sets quickly.
- Avoid requiring notes.

## Flow 4: Review Workout History

Intent: Find recent sessions and inspect details.

Entry points:

- Workouts tab
- Dashboard recent workouts
- Exercise progression drill-down

Steps:

1. User opens Workouts.
2. User scans a reverse-chronological list.
3. User filters or paginates if history grows.
4. User opens a workout.
5. User reviews exercises, sets, notes, and date.

Success state:

- User can understand what happened in the workout without editing.
- Edit and delete actions are available but visually secondary.

UX priorities:

- Show workout title, date, exercise count, and total volume summary.
- Keep destructive actions out of the primary tap path.

## Flow 5: Exercise Progression

Intent: Understand whether a lift is improving.

Entry points:

- Dashboard strength progression card
- Workout detail exercise row
- Progress tab

Steps:

1. User selects an exercise.
2. User sees workout count, completed set count, best weight, best reps, and total volume.
3. User reviews a simple trend visualization.
4. User optionally opens recent workouts for that exercise.

Success state:

- User can identify whether performance is moving up, flat, or declining.

UX priorities:

- Lead with best weight and recent change.
- Use short labels and plain units.
- Prefer clear trend direction over dense analytics.

## Flow 6: Simple Nutrition Logging

Intent: Log food quickly enough that users will keep doing it.

Entry points:

- Dashboard protein card
- Nutrition tab primary action
- Mobile bottom navigation central log action

Steps:

1. User taps Log Food.
2. User selects meal type or keeps a default.
3. User enters food name.
4. User enters protein and optionally calories, carbs, fat, serving quantity, and notes.
5. User saves the log.

Success state:

- Nutrition overview updates today's protein and calorie totals.
- The saved entry appears in today's meal list.

UX priorities:

- Protein must be the most prominent nutrition input.
- Optional fields should not slow down protein logging.
- Totals should update immediately after save.

## Flow 7: Daily Nutrition Review

Intent: Check whether today's eating supports training.

Entry points:

- Dashboard protein card
- Nutrition tab

Steps:

1. User opens Nutrition.
2. User sees today's protein and calorie totals.
3. User reviews meals grouped by meal type or time.
4. User edits or deletes an entry if needed.
5. User changes date to review past logs.

Success state:

- User knows how much protein remains for the day.

UX priorities:

- Protein progress appears before calories.
- Show remaining protein in grams, not only percentage.
- Keep date controls reachable with one thumb.

## Flow 8: Goals

Intent: Connect training and nutrition behavior to measurable targets.

Entry points:

- Dashboard active goals
- Goals section
- Progress tab secondary action

Steps:

1. User opens Goals.
2. User creates a goal with title, metric, target value, and dates.
3. User reviews calculated progress or latest check-in.
4. User adds a check-in for non-derived goals.

Success state:

- Goal progress is visible and connected to workouts or nutrition where possible.

UX priorities:

- Favor strength and protein goal templates.
- Show goal status without requiring users to understand backend metric names.

## Flow 9: AI Coach

Intent: Get concise advisory feedback from existing workout, nutrition, and goal data.

Entry points:

- Dashboard latest feedback card
- AI Coach tab
- Empty state after logging enough data

Steps:

1. User opens AI Coach.
2. User chooses or enters a feedback request.
3. UI explains that feedback is advisory.
4. User submits the request.
5. UI shows loading state.
6. On success, feedback summary and recommendations appear.
7. The feedback is added to history.

Success state:

- User receives one or more practical next actions.
- Feedback history is available for later review.

UX priorities:

- Keep prompts short and guided.
- Make provider failure states recoverable.
- Do not show raw prompts, provider details, or technical errors.

## Flow 10: Error and Empty States

Intent: Preserve user confidence when data is missing or an operation fails.

Common states:

- No workouts yet
- No nutrition logs today
- No goals yet
- No AI feedback history
- Validation error
- Unauthenticated session
- Provider unavailable

UX priorities:

- Empty states should offer a direct next action.
- Errors should use readable copy and stable visual treatment.
- Technical details, stack traces, token details, and provider internals must never appear in the UI.
