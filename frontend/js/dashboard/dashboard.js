import { request } from "../api/apiClient.js";
import { showMessage } from "../shared/ui.js";

const dashboardSummary = document.querySelector("#dashboard-summary");
const loadDashboardButton = document.querySelector("#load-dashboard");

export function initDashboard() {
  loadDashboardButton.addEventListener("click", loadDashboardData);
  window.addEventListener("liftlog:data-changed", () => {
    loadDashboardData().catch(() => {});
  });
  clearDashboardData();
}

export async function loadDashboardData() {
  try {
    const summary = await request("/api/dashboard/summary");
    renderDashboard(summary);
  } catch (error) {
    dashboardSummary.textContent = "Sign in to view dashboard.";
    showMessage(error.message);
  }
}

export function clearDashboardData() {
  dashboardSummary.textContent = "Sign in to view dashboard.";
}

function renderDashboard(summary) {
  const recentWorkouts = summary.recentWorkouts || [];
  const activeGoals = summary.activeGoals || [];
  const latestWorkout = recentWorkouts[0];
  const firstGoal = activeGoals[0];
  const latestAiFeedback = summary.latestAiFeedback;
  const nutrition = summary.nutritionToday || {};
  const protein = Number(nutrition.protein) || 0;
  const calories = Number(nutrition.calories) || 0;
  const proteinDisplay = formatNumber(protein);
  const proteinMeterWidth = Math.min(100, Math.round((protein / 160) * 100));
  const calorieMeterWidth = Math.min(100, Math.round((calories / 2200) * 100));
  const weeklyWorkoutCount = Number(summary.weeklyWorkoutCount) || 0;
  const completedWorkoutLabel = weeklyWorkoutCount > 0 ? "Completed" : "Pending";
  const goalsComplete = Math.min(4, activeGoals.length || (firstGoal ? 1 : 0));

  dashboardSummary.innerHTML = `
    <div class="dashboard-grid">
      <article class="metric-card overview-card workout-overview">
        <div class="metric-card-header">
          <span class="metric-icon success-icon" aria-hidden="true"></span>
          <h3>Workout</h3>
        </div>
        <p class="metric-value compact-value">${escapeHtml(completedWorkoutLabel)}</p>
        <p class="metric-subtext">${weeklyWorkoutCount} this week</p>
      </article>
      <article class="metric-card overview-card protein-card">
        <div class="metric-card-header">
          <span class="metric-icon protein-icon" aria-hidden="true"></span>
          <h3>Protein today</h3>
        </div>
        <p class="metric-value compact-value">${proteinDisplay}g <span>/ 150g</span></p>
        <div class="progress-track" aria-label="Protein logged today">
          <span style="width: ${proteinMeterWidth}%"></span>
        </div>
        <p class="metric-subtext">${proteinMeterWidth}% of daily goal</p>
      </article>
      <article class="metric-card overview-card calories-card">
        <div class="metric-card-header">
          <span class="metric-icon calories-icon" aria-hidden="true"></span>
          <h3>Calories</h3>
        </div>
        <p class="metric-value compact-value">${formatNumber(calories)} <span>/ 2,200</span></p>
        <div class="progress-track" aria-label="Calories logged today">
          <span style="width: ${calorieMeterWidth}%"></span>
        </div>
        <p class="metric-subtext">${calorieMeterWidth}% of daily goal</p>
      </article>
      <article class="metric-card overview-card goals-card">
        <div class="metric-card-header">
          <span class="metric-icon goals-icon" aria-hidden="true"></span>
          <h3>Goals</h3>
        </div>
        <p class="metric-value compact-value">${goalsComplete} <span>/ 4</span></p>
        <div class="progress-track" aria-label="Goals progress">
          <span style="width: ${Math.min(100, goalsComplete * 25)}%"></span>
        </div>
        <p class="metric-subtext">${firstGoal ? escapeHtml(firstGoal.title) : "On track"}</p>
      </article>
    </div>

    <div class="dashboard-main-grid">
      <article class="metric-card recent-workouts-panel">
        <div class="metric-card-header">
          <h3>Recent Workouts</h3>
          <a class="ghost-link" href="#workouts">View all</a>
        </div>
        ${recentWorkouts.length ? recentWorkouts.slice(0, 4).map((workout) => `
          <p class="workout-mini-row">
            <span class="metric-icon strength-icon" aria-hidden="true"></span>
            <span>
              <strong>${escapeHtml(workout.title || "Workout")}</strong>
              <small>${escapeHtml(workout.workoutDate)}</small>
            </span>
            <span class="status-dot" aria-hidden="true"></span>
          </p>
        `).join("") : "<p class=\"metric-subtext\">No workouts yet.</p>"}
      </article>

      <article class="metric-card strength-progress-panel">
        <div class="metric-card-header">
          <h3>Strength Progress</h3>
          <span class="positive-change">+12%</span>
        </div>
        <p class="metric-subtext">${latestWorkout ? escapeHtml(latestWorkout.title || "Recent lift") : "Bench Press"}</p>
        <svg class="trend-chart" viewBox="0 0 560 260" aria-label="Strength trend">
          <defs>
            <linearGradient id="strengthFill" x1="0" x2="0" y1="0" y2="1">
              <stop offset="0%" stop-color="#7c4dff" stop-opacity="0.45"></stop>
              <stop offset="100%" stop-color="#7c4dff" stop-opacity="0"></stop>
            </linearGradient>
          </defs>
          <path class="chart-area" d="M24 204 L80 178 L138 188 L196 132 L252 154 L308 108 L364 116 L420 72 L476 94 L536 54 L536 232 L24 232 Z"></path>
          <polyline class="chart-line" points="24,204 80,178 138,188 196,132 252,154 308,108 364,116 420,72 476,94 536,54"></polyline>
        </svg>
        <div class="stat-strip">
          <span><strong>Top Lift</strong>${latestWorkout ? "80kg" : "0kg"}</span>
          <span><strong>Workouts</strong>${weeklyWorkoutCount}</span>
          <span><strong>Streak</strong>${weeklyWorkoutCount > 0 ? "7 days" : "0 days"}</span>
        </div>
      </article>

      <article class="metric-card nutrition-overview-panel">
        <div class="metric-card-header">
          <h3>Nutrition Summary</h3>
          <a class="ghost-link" href="#nutrition">View all</a>
        </div>
        <div class="donut-wrap">
          <div class="donut-meter" style="--percent: ${proteinMeterWidth};">
            <span>${proteinDisplay}g</span>
            <small>of 150g Protein</small>
          </div>
        </div>
        <div class="macro-list">
          <p><span class="legend-dot protein-dot"></span>Protein <strong>${proteinDisplay}g / 150g</strong></p>
          <p><span class="legend-dot carb-dot"></span>Carbs <strong>${formatNumber(nutrition.carbohydrates || 0)}g</strong></p>
          <p><span class="legend-dot fat-dot"></span>Fat <strong>${formatNumber(nutrition.fat || 0)}g</strong></p>
          <p><span class="legend-dot calorie-dot"></span>Calories <strong>${formatNumber(calories)} / 2,200</strong></p>
        </div>
      </article>

      <article class="metric-card ai-coach-panel">
        <div>
          <h3>AI Coach Recommendation</h3>
          <p class="ai-panel-title">Great work this week.</p>
          <p>${latestAiFeedback ? escapeHtml(latestAiFeedback.summary || latestAiFeedback.feedback || "Latest feedback is ready.") : "Your bench press strength is improving. Focus on protein consistency and recovery."}</p>
        </div>
        <a class="button-secondary" href="#ai-coach">View full analysis</a>
      </article>
    </div>
  `;
}

function boundedPercent(value) {
  return Math.max(0, Math.min(100, Number(value) || 0));
}

function formatNumber(value) {
  const number = Number(value) || 0;
  return Number.isInteger(number) ? String(number) : number.toFixed(1);
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll("\"", "&quot;")
    .replaceAll("'", "&#039;");
}
