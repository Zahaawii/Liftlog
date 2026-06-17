# Figma Blueprint

Figma file:

- `LiftLog AI - UX Blueprint`
- https://www.figma.com/design/j3x3GPGNAU2LdRsKIo45y3

Status:

- The file was created through the Figma connector.
- Canvas population was blocked by the Figma Starter plan MCP tool-call limit.
- This document is the Figma-ready blueprint to apply when Figma write calls are available again.

## Blueprint Goals

Create an editable Figma planning file for LiftLog AI that translates the design documentation into a practical product blueprint.

The blueprint should prioritize:

1. Strength progression
2. Protein intake
3. Fast workout logging
4. Simple nutrition logging
5. Visible progress
6. Usable AI Coach feedback

## File Pages

### 00 Cover

Purpose:

- Orient stakeholders.
- State product priorities.
- Link the design file to repository documentation.

Frames:

- `Cover / Product Summary`
- `Cover / Design Priorities`
- `Cover / Source Docs`

Content:

- Product name: `LiftLog AI`
- Product type: mobile-first fitness tracking app.
- Primary metrics: strength progression and protein intake.
- Stack context: Spring Boot, MySQL, Docker, HTML, CSS, Vanilla JavaScript.

### 01 User Flows

Purpose:

- Show the main user journeys before screen-level design.

Frames:

- `Flow / Register and Start`
- `Flow / Login and Resume`
- `Flow / Fast Workout Logging`
- `Flow / Exercise Progression`
- `Flow / Simple Nutrition Logging`
- `Flow / Daily Nutrition Review`
- `Flow / Goals`
- `Flow / AI Coach`

Layout:

- Use left-to-right flow cards.
- Use one accent color for primary success moments.
- Mark strength progression and protein intake as the two key outcome nodes.

### 02 Information Architecture

Purpose:

- Define the application structure.

Frames:

- `IA / Public Area`
- `IA / Authenticated App`
- `IA / Resource Map`

Public area:

- Login
- Register

Authenticated app:

- Dashboard
- Workouts
- Nutrition
- Progress
- Goals
- AI Coach
- Profile
- Settings

Resource map:

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

### 03 Navigation

Purpose:

- Define mobile and desktop navigation models.

Frames:

- `Navigation / Mobile Bottom Bar`
- `Navigation / Mobile Log Action Sheet`
- `Navigation / Desktop Sidebar`
- `Navigation / Context Navigation`

Mobile bottom navigation:

- Dashboard
- Workouts
- Log
- Nutrition
- Coach

Desktop sidebar:

- Dashboard
- Workouts
- Nutrition
- Progress
- Goals
- AI Coach
- Profile
- Settings
- Logout

### 04 Mobile Wireframes

Purpose:

- Represent the primary mobile UX.

Frame size:

- 375 x 812

Frames:

- `Mobile / Login`
- `Mobile / Register`
- `Mobile / Dashboard`
- `Mobile / Workout List`
- `Mobile / Create Workout`
- `Mobile / Workout Detail`
- `Mobile / Exercise Progress`
- `Mobile / Nutrition Overview`
- `Mobile / Nutrition Log`
- `Mobile / Goals`
- `Mobile / AI Coach`
- `Mobile / Profile`
- `Mobile / Settings`

Dashboard hierarchy:

1. Strength Progression card
2. Protein Today card
3. Quick actions
4. Recent workouts
5. Active goals
6. Latest AI Coach feedback

Create Workout hierarchy:

1. Date and title
2. Exercise selector
3. Active exercise
4. Previous/best context
5. Set rows
6. Add Set
7. Save

Nutrition hierarchy:

1. Date
2. Protein progress
3. Calories and macros
4. Meal entries
5. Log Food action

AI Coach hierarchy:

1. Request panel
2. Latest feedback
3. Recommendations
4. History
5. Provider unavailable state

### 05 Desktop Wireframes

Purpose:

- Adapt the same product hierarchy to wider screens.

Frame size:

- 1440 x 1024

Frames:

- `Desktop / Dashboard`
- `Desktop / Workouts`
- `Desktop / Workout Detail`
- `Desktop / Progress`
- `Desktop / Nutrition`
- `Desktop / Goals`
- `Desktop / AI Coach`

Desktop dashboard layout:

- Left sidebar.
- Top header with quick actions.
- First row: Strength Progression and Protein Today.
- Second row: Recent Workouts, Goals, AI Coach.

### 06 Design System

Purpose:

- Create reusable visual foundations and components.

Frames:

- `Tokens / Colors`
- `Tokens / Typography`
- `Tokens / Spacing`
- `Components / Buttons`
- `Components / Inputs`
- `Components / Metric Cards`
- `Components / Lists`
- `Components / Navigation`
- `Components / Progress Bars`
- `Components / Alerts`
- `Components / Empty States`

Semantic colors:

- Strength accent: blue or teal.
- Protein accent: green.
- Neutral surfaces: quiet app background and cards.
- Error: red.
- Warning: amber.
- Success: green.

Component requirements:

- Buttons have primary, secondary, tertiary, destructive, disabled, and loading states.
- Inputs have default, focus, error, and disabled states.
- Metric cards include strength, protein, goal, and AI variants.
- Navigation includes mobile bottom bar and desktop sidebar variants.

### 07 Responsive Strategy

Purpose:

- Show how layouts adapt across viewport sizes.

Frames:

- `Responsive / Mobile 375`
- `Responsive / Small Mobile 320`
- `Responsive / Tablet 768`
- `Responsive / Desktop 1440`

Rules:

- Mobile stacks content.
- Tablet can use two-column layouts.
- Desktop uses sidebar and wider summary grids.
- Strength and protein remain above secondary content at every size.

### 08 States and Edge Cases

Purpose:

- Make non-happy paths explicit.

Frames:

- `State / No Workouts`
- `State / No Nutrition Logs`
- `State / No Goals`
- `State / No AI Feedback`
- `State / Validation Error`
- `State / Session Expired`
- `State / Provider Unavailable`
- `State / Delete Confirmation`

Rules:

- Empty states include one direct action.
- Errors use user-safe text.
- Technical details, tokens, provider internals, and stack traces are never shown.

### 09 Handoff

Purpose:

- Prepare design for frontend implementation without changing API contracts.

Frames:

- `Handoff / Screen Inventory`
- `Handoff / Component Inventory`
- `Handoff / API Resource Mapping`
- `Handoff / Accessibility Checklist`
- `Handoff / Open Product Questions`

API resource mapping:

- Dashboard screen uses dashboard summary.
- Workouts screens use workout and exercise resources.
- Nutrition screens use nutrition log and daily total resources.
- Progress screens use exercise progression resources.
- Goals screens use goal and goal check-in resources.
- AI Coach screens use AI feedback request and history resources.

## Visual Composition Guidance

Use a clean product design style:

- Neutral app background.
- Compact panels with 8px radius.
- No nested cards.
- No decorative gradients.
- No marketing hero layout.
- Metric-first hierarchy.
- Strong, readable numbers.
- Clear form labels and unit labels.

## Suggested Canvas Layout

Place pages from left to right in this order:

1. Cover
2. User Flows
3. Information Architecture
4. Navigation
5. Mobile Wireframes
6. Desktop Wireframes
7. Design System
8. Responsive Strategy
9. States
10. Handoff

Within each page:

- Use section headers at the top.
- Keep frames aligned to an 8px grid.
- Keep related frames in rows.
- Use annotations to explain UX decisions.

## Open Figma Blocker

The Figma connector returned:

`You've reached the Figma MCP tool call limit on the Starter plan. Upgrade your plan for more tool calls.`

Once the limit resets or the plan allows more MCP calls, this blueprint can be pushed into the existing Figma file as editable frames.
