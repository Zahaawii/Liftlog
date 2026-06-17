# Figma Project Structure

## File Name

`LiftLog AI - Product Design`

## Pages

### 00 Cover

Purpose:

- Project title.
- Current design status.
- Owner and date.
- Links to product docs.

Recommended sections:

- Product priorities: strength progression and protein intake.
- Platform: responsive browser app.
- Technology context: HTML, CSS, Vanilla JavaScript frontend.

### 01 Foundations

Purpose:

- Design tokens and global styles.

Frames:

- Color styles
- Typography styles
- Spacing scale
- Radius and elevation
- Icon guidance
- Accessibility notes

### 02 Components

Purpose:

- Reusable UI components and variants.

Component groups:

- Buttons
- Inputs
- Selects
- Numeric fields with units
- Date controls
- Cards
- List rows
- Progress bars
- Chart containers
- Navigation bars
- Sidebar
- Modals and confirmations
- Toasts and inline alerts
- Empty states
- Loading states

Variant recommendations:

- Default
- Hover
- Focus
- Disabled
- Loading
- Error
- Success

### 03 Mobile Wireframes

Purpose:

- Mobile-first low-fidelity screen planning.

Frames:

- Login
- Register
- Dashboard
- Workout List
- Create Workout
- Workout Detail
- Exercise Progress
- Nutrition Overview
- Nutrition Log
- Goals
- AI Coach
- Profile
- Settings

Frame width:

- 375px for default mobile.
- Include 320px stress-test frames for dense forms.

### 04 Desktop Wireframes

Purpose:

- Desktop adaptation of the same hierarchy.

Frames:

- Dashboard
- Workouts
- Workout Detail
- Exercise Progress
- Nutrition
- Goals
- AI Coach
- Profile
- Settings

Frame width:

- 1440px default.
- 1024px compact desktop check.

### 05 Prototype Flows

Purpose:

- Clickable flow planning for core journeys.

Flows:

- Register to first dashboard
- Login to dashboard
- Log workout
- Review exercise progression
- Log food and check protein
- Create goal
- Request AI Coach feedback

### 06 States

Purpose:

- Non-happy-path behavior.

Frames:

- No workouts
- No nutrition logs today
- No goals
- No AI feedback history
- Validation error
- Session expired
- Provider unavailable
- Save loading
- Delete confirmation

### 07 Handoff

Purpose:

- Implementation guidance without changing API contracts.

Sections:

- Screen inventory
- Component inventory
- Interaction notes
- Responsive notes
- Accessibility checklist
- API resource mapping

## Naming Conventions

Frame naming:

- `Mobile / Dashboard / Default`
- `Mobile / Workout / Create`
- `Desktop / Nutrition / Overview`
- `State / AI Coach / Provider Unavailable`

Component naming:

- `Button / Primary / Default`
- `Input / Text / Error`
- `Card / Metric / Strength`
- `Card / Metric / Protein`
- `Navigation / Mobile Bottom`
- `Navigation / Desktop Sidebar`

## Component Organization

Use component sets for variants where possible:

- Buttons by hierarchy and state.
- Inputs by type and state.
- Metric cards by metric type.
- List rows by domain.
- Alerts by severity.

Metric card variants:

- Strength progression
- Protein intake
- Goal progress
- AI feedback summary

## Prototype Rules

- Keep prototypes focused on core flows rather than every possible branch.
- Use realistic sample data.
- Include at least one validation failure path.
- Include at least one empty-state path.
- Include a provider failure path for AI Coach.

## Handoff Notes

Design annotations should state:

- Which API resource a screen depends on.
- Which fields are required.
- Which states are loading, empty, error, or success.
- Which values are calculated by the backend.
- Which interactions are local UI behavior only.

Do not annotate designs with new API contracts. If a design needs data not currently available, mark it as a future product question rather than an implementation requirement.
