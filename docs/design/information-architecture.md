# Information Architecture

## Product Shape

LiftLog AI is an authenticated fitness tracking application. The primary user tasks are:

- Track workouts.
- Track nutrition.
- Review strength progression.
- Review protein intake.
- Receive AI Coach feedback.

The information architecture should keep strength progression and protein intake visible across the main product surfaces.

## Top-Level Areas

### Public Area

- Login
- Register

### Authenticated App

- Dashboard
- Workouts
- Nutrition
- Progress
- Goals
- AI Coach
- Profile
- Settings

## Content Hierarchy

### Dashboard

Primary content:

- Strength progression summary
- Today's protein intake summary

Secondary content:

- Recent workouts
- Today's nutrition totals
- Active goals
- Latest AI feedback

Actions:

- Log Workout
- Log Food
- Ask AI Coach

### Workouts

Primary content:

- Workout history
- Workout detail
- Create or edit workout

Supporting content:

- Shared exercise search and selection
- Exercise set entry
- Exercise notes
- Workout notes

Progress links:

- Exercise progression
- Recent workouts for an exercise

### Nutrition

Primary content:

- Today's protein progress
- Today's calorie and macro totals
- Nutrition log list

Supporting content:

- Meal type
- Food name
- Serving quantity
- Calories
- Protein
- Carbohydrates
- Fat
- Notes

### Progress

Primary content:

- Strength progression by exercise
- Best weight
- Best reps
- Total volume

Secondary content:

- Goal progress
- Recent trend indicators
- Nutrition consistency summaries when available

### Goals

Primary content:

- Active goals
- Goal progress
- Goal creation
- Goal check-ins

Preferred goal categories:

- Strength target
- Workout frequency
- Protein target
- Daily calorie target
- Custom measurable target

### AI Coach

Primary content:

- New feedback request
- Latest feedback
- Feedback history

Supporting content:

- Advisory disclaimer
- Request type
- Prompt summary
- Recommendations
- Provider failure state

### Profile

Primary content:

- Display name
- Email
- Optional fitness context when supported

### Settings

Primary content:

- Account settings
- Session actions
- Privacy and data expectations
- Logout

## Object Model for UI

The UI should mirror existing API resources without creating new domain concepts:

- User
- Exercise
- Workout
- Workout Exercise
- Workout Set
- Nutrition Log
- Goal
- Goal Check-In
- Dashboard Summary
- AI Feedback

## Cross-Linking Rules

- Dashboard strength card links to Progress.
- Dashboard protein card links to Nutrition.
- Recent workout rows link to Workout Detail.
- Exercise names in workout details link to Exercise Progression.
- Active goal rows link to Goal Detail or edit state.
- Latest AI feedback links to AI Coach history.

## Search and Filtering

Initial filters should stay simple:

- Workout history by pagination and date order.
- Exercise library search by name.
- Nutrition history by date.
- Goals by active or completed status.
- AI feedback history by reverse chronological order.

More complex filters should wait until real usage patterns justify them.
