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
  const latestWorkout = summary.recentWorkouts[0];
  const firstGoal = summary.activeGoals[0];
  const nutrition = summary.nutritionToday;

  dashboardSummary.innerHTML = `
    <div class="dashboard-grid">
      <article class="metric-card">
        <h3>Weekly workouts</h3>
        <p class="metric-value">${summary.weeklyWorkoutCount}</p>
      </article>
      <article class="metric-card">
        <h3>Today nutrition</h3>
        <p>${nutrition.calories} calories</p>
        <p>${nutrition.protein}g protein · ${nutrition.carbohydrates}g carbs · ${nutrition.fat}g fat</p>
      </article>
      <article class="metric-card">
        <h3>Latest workout</h3>
        ${latestWorkout ? `<p>${escapeHtml(latestWorkout.title || "Workout")}</p><p>${escapeHtml(latestWorkout.workoutDate)}</p>` : "<p>No workouts yet.</p>"}
      </article>
      <article class="metric-card">
        <h3>Active goal</h3>
        ${firstGoal ? `<p>${escapeHtml(firstGoal.title)}</p><p>${firstGoal.progressPercent}% complete</p>` : "<p>No active goals yet.</p>"}
      </article>
    </div>
  `;
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll("\"", "&quot;")
    .replaceAll("'", "&#039;");
}
