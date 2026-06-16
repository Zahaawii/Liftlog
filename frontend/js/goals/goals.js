import { request } from "../api/apiClient.js";
import { showMessage } from "../shared/ui.js";

const goalForm = document.querySelector("#goal-form");
const goalCheckInForm = document.querySelector("#goal-checkin-form");
const goalCheckInSelect = document.querySelector("#goal-checkin-select");
const goalList = document.querySelector("#goal-list");
const loadGoalsButton = document.querySelector("#load-goals");

export function initGoals() {
  goalForm.addEventListener("submit", handleGoalSubmit);
  goalCheckInForm.addEventListener("submit", handleCheckInSubmit);
  loadGoalsButton.addEventListener("click", loadGoalData);
  setDefaultDates();
  clearGoalData();
}

export async function loadGoalData() {
  try {
    const response = await request("/api/goals?page=0&size=20");
    renderGoals(response.items);
    populateCheckInGoals(response.items);
  } catch (error) {
    clearGoalData();
    showMessage(error.message);
  }
}

export function clearGoalData() {
  goalCheckInSelect.innerHTML = "";
  goalList.textContent = "Sign in to view goals.";
}

async function handleGoalSubmit(event) {
  event.preventDefault();
  const formData = new FormData(goalForm);
  const payload = {
    goalType: formData.get("goalType"),
    title: formData.get("title"),
    targetMetric: formData.get("targetMetric"),
    targetValue: Number(formData.get("targetValue")),
    currentBaseline: numberOrNull(formData.get("currentBaseline")),
    startDate: formData.get("startDate"),
    targetDate: formData.get("targetDate") || null
  };

  try {
    await request("/api/goals", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    goalForm.reset();
    setDefaultDates();
    showMessage("Goal created.");
    await loadGoalData();
    notifyFitnessDataChanged();
  } catch (error) {
    showMessage(error.message);
  }
}

async function handleCheckInSubmit(event) {
  event.preventDefault();
  const formData = new FormData(goalCheckInForm);
  const goalId = formData.get("goalId");

  if (!goalId) {
    showMessage("Create a goal before logging a check-in.");
    return;
  }

  const payload = {
    checkInDate: formData.get("checkInDate"),
    value: Number(formData.get("value")),
    notes: formData.get("notes")
  };

  try {
    await request(`/api/goals/${encodeURIComponent(goalId)}/check-ins`, {
      method: "POST",
      body: JSON.stringify(payload)
    });
    goalCheckInForm.reset();
    setDefaultDates();
    showMessage("Goal check-in logged.");
    await loadGoalData();
    notifyFitnessDataChanged();
  } catch (error) {
    showMessage(error.message);
  }
}

function renderGoals(goals) {
  if (!goals.length) {
    goalList.textContent = "No goals created yet.";
    return;
  }

  goalList.innerHTML = goals.map(renderGoal).join("");
}

function renderGoal(goal) {
  return `
    <article class="goal-item">
      <div class="goal-item-header">
        <h3>${escapeHtml(goal.title)}</h3>
        <span>${escapeHtml(goal.status)}</span>
      </div>
      <p>${escapeHtml(goal.goalType)} · ${escapeHtml(goal.targetMetric)}</p>
      <p>${goal.currentValue} of ${goal.targetValue} · ${goal.progressPercent}% complete</p>
      <div class="progress-track" aria-label="${escapeHtml(goal.title)} progress">
        <span style="width: ${boundedPercent(goal.progressPercent)}%"></span>
      </div>
      <p>Start ${escapeHtml(goal.startDate)}${goal.targetDate ? ` · Target ${escapeHtml(goal.targetDate)}` : ""}</p>
    </article>
  `;
}

function populateCheckInGoals(goals) {
  goalCheckInSelect.innerHTML = goals
    .map((goal) => `<option value="${goal.id}">${escapeHtml(goal.title)}</option>`)
    .join("");

  if (!goals.length) {
    goalCheckInSelect.innerHTML = "";
  }
}

function setDefaultDates() {
  const today = new Date().toISOString().slice(0, 10);
  if (!goalForm.elements.startDate.value) {
    goalForm.elements.startDate.value = today;
  }
  if (!goalCheckInForm.elements.checkInDate.value) {
    goalCheckInForm.elements.checkInDate.value = today;
  }
}

function numberOrNull(value) {
  return value === "" || value == null ? null : Number(value);
}

function boundedPercent(value) {
  return Math.max(0, Math.min(100, Number(value) || 0));
}

function notifyFitnessDataChanged() {
  window.dispatchEvent(new CustomEvent("liftlog:data-changed"));
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll("\"", "&quot;")
    .replaceAll("'", "&#039;");
}
