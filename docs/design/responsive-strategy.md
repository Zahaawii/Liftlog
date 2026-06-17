# Responsive Layout Strategy

## Breakpoints

Recommended planning breakpoints:

- Small mobile: 320px-374px.
- Mobile: 375px-767px.
- Tablet: 768px-1023px.
- Desktop: 1024px and above.

Layouts should adapt fluidly between breakpoints rather than relying on many fixed widths.

## Mobile Strategy

Mobile is the primary design target.

Rules:

- Use a bottom navigation bar.
- Keep the primary action reachable by thumb.
- Stack content vertically.
- Put strength progression and protein intake at the top of Dashboard.
- Use single-column forms.
- Use compact section headers with direct actions.
- Keep destructive actions out of common thumb paths.

Workout logging:

- Keep date and title compact.
- Use large numeric inputs.
- Keep Add Set and Add Exercise close to active content.
- Consider sticky Save behavior in final UI implementation.

Nutrition logging:

- Put protein before optional macros.
- Use date and meal controls near the top.
- Keep the form short enough for one-handed entry.

AI Coach:

- Use one request panel followed by latest feedback.
- Put history below current feedback.

## Tablet Strategy

Tablet can introduce two-column layouts where useful.

Recommended patterns:

- Dashboard: two-column summary grid with lists below.
- Workout detail: exercise list and summary side by side when space allows.
- Nutrition: daily totals beside meal entries.
- AI Coach: request panel beside latest feedback.

Navigation:

- Bottom navigation remains acceptable in portrait.
- Sidebar may appear in landscape if width allows.

## Desktop Strategy

Desktop should use a persistent sidebar and denser information layout.

Recommended patterns:

- Dashboard: two or three columns with strength and protein summaries first.
- Workouts: history list with detail preview or full detail page.
- Nutrition: daily totals panel plus meal log table/list.
- Progress: chart area with stats panel.
- AI Coach: request form, latest feedback, and history columns.

Rules:

- Avoid overly wide text blocks.
- Keep forms constrained for readability.
- Use tables only where comparison is useful.
- Preserve mobile hierarchy; do not hide strength or protein behind desktop-only panels.

## Screen-Specific Adaptation

### Dashboard

Mobile:

- Strength card first.
- Protein card second.
- Quick actions third.
- Recent activity below.

Desktop:

- Strength and protein sit in the first row.
- Recent workouts, goals, and AI feedback fill secondary columns.

### Workouts

Mobile:

- List-first flow.
- Create workout opens a full-screen form.
- Exercise selection uses search and compact list rows.

Desktop:

- Use wider rows with more summary metadata.
- Workout detail may show summary and exercises side by side.

### Nutrition

Mobile:

- Daily protein summary remains topmost.
- Log entry form is single-column.

Desktop:

- Daily totals can sit beside the log list.
- Meal entries can use a denser row format.

### Progress

Mobile:

- One chart per screen section.
- Textual stats directly below each chart.

Desktop:

- Chart and stats can sit side by side.
- Exercise selector can persist in a side panel.

### AI Coach

Mobile:

- New request first.
- Latest feedback second.
- History third.

Desktop:

- Request panel can sit left.
- Feedback and history can sit right.

## Layout Stability

Rules:

- Reserve stable space for charts and metric cards.
- Avoid layout shifts when loading or when numbers change.
- Keep button text from wrapping awkwardly by using concise labels.
- Ensure long exercise and food names truncate or wrap cleanly.

## Accessibility Across Viewports

- Keep focus order matching visual order.
- Ensure bottom navigation does not cover form controls.
- Provide enough spacing around inputs for touch.
- Do not hide critical actions on small screens.
- Charts must remain understandable through adjacent metric text.
