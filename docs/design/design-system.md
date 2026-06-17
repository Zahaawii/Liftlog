# Design System Proposal

## Design Principles

- Mobile-first: core actions must work comfortably on small screens.
- Fast logging: workout and nutrition entry should require minimal scrolling and typing.
- Progress visible: strength progression and protein intake should appear before secondary metrics.
- Calm and utilitarian: avoid decorative layouts that compete with repeated daily use.
- Accessible by default: contrast, focus states, labels, and touch targets must be deliberate.

## Visual Tone

The product should feel like a focused training log, not a marketing site. Use restrained surfaces, clear hierarchy, compact spacing, and practical data visualization.

Avoid:

- Decorative gradients as primary structure.
- Oversized hero sections inside the app.
- Nested cards.
- Animation that delays logging.
- Color palettes dominated by one hue.

## Color Palette

Recommended semantic palette:

- Background: near-white or very dark neutral, depending on theme.
- Surface: subtle neutral contrast from background.
- Text primary: high-contrast neutral.
- Text secondary: muted neutral.
- Border: low-contrast neutral.
- Strength accent: blue or teal.
- Protein accent: green.
- Warning: amber.
- Error: red.
- Success: green.

Usage rules:

- Strength progression charts and badges should use the strength accent.
- Protein progress should consistently use the protein accent.
- Error colors are reserved for actual errors, not missed goals.
- Goal status may use neutral, success, warning, and error semantics.

## Typography

Recommended scale:

- Screen title: 24-28px, strong weight.
- Section title: 18-20px, strong weight.
- Card metric: 28-36px, strong weight.
- Body: 15-16px.
- Secondary text: 13-14px.
- Labels: 12-14px, medium weight.

Rules:

- Do not scale type by viewport width.
- Keep letter spacing at normal.
- Use metric-sized text only for primary data, not every card heading.

## Spacing

Recommended spacing tokens:

- 4px: tight icon and label spacing.
- 8px: compact component gaps.
- 12px: form field grouping.
- 16px: standard section spacing.
- 24px: major screen spacing.
- 32px: desktop layout spacing.

Touch target minimum:

- 44px by 44px for interactive controls.

## Shape and Elevation

Recommended radius:

- Small controls: 6px.
- Cards and panels: 8px maximum.
- Pills and chips: full radius only for compact labels and filters.

Elevation:

- Use borders and subtle background contrast first.
- Reserve shadows for overlays, menus, and modals.

## Icons

Use familiar fitness and utility metaphors when implementation begins:

- Dumbbell or activity for Workouts.
- Utensils or protein indicator for Nutrition.
- Line chart for Progress.
- Message or spark for AI Coach.
- Plus for create.
- Trash for delete.
- Pencil for edit.

Icons must have accessible labels or visible text when meaning is not obvious.

## Buttons

Button hierarchy:

- Primary: main screen action such as Save, Log Workout, Log Food.
- Secondary: supportive action such as Add Set, Add Exercise.
- Tertiary: low-emphasis text action.
- Destructive: delete and logout confirmations.

Rules:

- Use one primary action per form view.
- Keep destructive actions visually separated.
- Use loading and disabled states for requests in progress.

## Inputs

Form field rules:

- Always provide visible labels.
- Use numeric keyboards for numeric nutrition and set fields on mobile.
- Show unit labels next to numeric fields: kg, reps, g, kcal, seconds, km.
- Use inline validation messages near the field.
- Preserve entered values after validation failure where possible.

Workout input priority:

- Reps
- Weight
- Completed
- Duration or distance when applicable
- Notes

Nutrition input priority:

- Food name
- Protein
- Calories
- Carbohydrates
- Fat
- Serving quantity
- Notes

## Cards and Lists

Use cards for individual repeated items and compact summary panels. Do not place cards inside other cards.

Workout list item should show:

- Title
- Date
- Exercise count
- Volume or set count

Nutrition list item should show:

- Food name
- Meal type
- Protein
- Calories when available

Goal list item should show:

- Goal title
- Target metric
- Progress percentage
- Status

AI history item should show:

- Created date
- Prompt summary
- Short feedback preview

## Charts and Data Visualization

Strength progression:

- Prefer a simple line chart for trend.
- Show best weight, best reps, total volume, and recent change.
- Use completed sets only in visible calculations.
- Avoid dense chart controls in the first version.

Protein intake:

- Use a horizontal progress bar or compact ring.
- Show consumed grams, target grams, and remaining grams.
- Put protein before calories in dashboard and nutrition overview.

Goal progress:

- Use linear progress bars.
- Include readable labels and percentages.

## Feedback States

Loading:

- Use skeletons for dashboard summaries.
- Use inline loading for submit buttons.

Empty:

- Provide one direct action.
- Keep copy short.

Error:

- Show readable messages.
- Never expose stack traces, tokens, provider internals, SQL errors, or raw API details.

Success:

- Use short confirmation messages for saves.
- Avoid blocking modals after routine logging.

## Accessibility

Requirements:

- Meet WCAG AA contrast for text and controls.
- Preserve visible keyboard focus states.
- Use semantic labels for all controls.
- Do not rely on color alone for status.
- Keep charts paired with textual values.
- Keep touch targets at least 44px.
- Confirm destructive actions.
