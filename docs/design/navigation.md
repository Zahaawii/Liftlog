# Navigation Structure

## Navigation Goals

Navigation should support fast repeat use on mobile. Users should be able to log a workout or protein entry from the main app shell without hunting through menus.

Primary navigation should be stable. Context-specific actions should live close to the content they affect.

## Mobile Navigation

Use a bottom navigation bar for authenticated mobile views.

Recommended items:

- Dashboard
- Workouts
- Log
- Nutrition
- Coach

Behavior:

- Dashboard opens the overview.
- Workouts opens workout history.
- Log opens a compact action sheet with Log Workout and Log Food.
- Nutrition opens daily nutrition overview.
- Coach opens AI Coach.

Rationale:

- The center Log action supports the fastest repeat task.
- Progress remains reachable from Dashboard strength cards, Workouts exercise rows, and secondary navigation rather than competing with core logging destinations on small screens.

Mobile secondary navigation:

- Progress
- Goals
- Profile
- Settings

These can live in a menu opened from the app header or Dashboard profile affordance.

## Desktop Navigation

Use a left sidebar for authenticated desktop views.

Primary sidebar items:

- Dashboard
- Workouts
- Nutrition
- Progress
- Goals
- AI Coach

Footer sidebar items:

- Profile
- Settings
- Logout

Desktop quick actions:

- Log Workout
- Log Food
- Ask Coach

Quick actions should be visible in the Dashboard header and relevant section headers.

## Public Navigation

Public views should stay minimal:

- Login
- Register

There is no need for a marketing navigation structure inside the application shell.

## Context Navigation

### Workout Detail

Context actions:

- Edit
- Delete
- View Exercise Progress

Design rules:

- Edit is visible but not louder than the workout content.
- Delete requires confirmation.
- Exercise progression links sit on exercise rows.

### Create Workout

Context actions:

- Save
- Add Exercise
- Add Set
- Cancel

Design rules:

- Save should remain reachable on mobile after scrolling.
- Cancel should be secondary.

### Nutrition Overview

Context actions:

- Log Food
- Change Date
- Edit Entry
- Delete Entry

Design rules:

- Protein progress remains visible near the top.
- Meal entries should be grouped or visually separated for scanning.

### AI Coach

Context actions:

- Request Feedback
- View History Item
- Retry Failed Request

Design rules:

- Feedback history should not obscure the new request path.
- Advisory framing should be present but compact.

## Breadcrumbs and Back Behavior

Mobile:

- Use a simple back affordance on detail and form screens.
- Avoid multi-level breadcrumbs on small screens.

Desktop:

- Use lightweight breadcrumbs for nested views such as Workouts > Workout Detail and Progress > Exercise.

## Active State Rules

- Exactly one primary destination should be active.
- Detail views inherit the active state of their parent area.
- Form overlays or drawers retain the parent active state.

## Authentication State

- Unauthenticated users can only access Login and Register.
- If a session expires, redirect to Login with a generic message.
- Do not expose token state, refresh state, or cookie details in navigation or UI.
