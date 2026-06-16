import { request } from "../api/apiClient.js";
import { showMessage } from "../shared/ui.js";

const nutritionForm = document.querySelector("#nutrition-form");
const nutritionSummary = document.querySelector("#nutrition-summary");
const nutritionList = document.querySelector("#nutrition-list");
const loadNutritionButton = document.querySelector("#load-nutrition");

export function initNutrition() {
  nutritionForm.addEventListener("submit", handleNutritionSubmit);
  loadNutritionButton.addEventListener("click", loadNutritionData);
  clearNutritionData();
}

export async function loadNutritionData() {
  const date = currentDateValue();

  try {
    const [logs, summary] = await Promise.all([
      request("/api/nutrition/logs?page=0&size=10"),
      request(`/api/nutrition/summary/daily?date=${encodeURIComponent(date)}`)
    ]);
    renderSummary(summary);
    renderLogs(logs.items);
  } catch (error) {
    nutritionSummary.textContent = "";
    nutritionList.textContent = "Sign in to view nutrition.";
    showMessage(error.message);
  }
}

export function clearNutritionData() {
  nutritionSummary.textContent = "";
  nutritionList.textContent = "Sign in to view nutrition.";
}

async function handleNutritionSubmit(event) {
  event.preventDefault();
  const formData = new FormData(nutritionForm);
  const payload = {
    logDate: formData.get("logDate"),
    mealType: formData.get("mealType"),
    foodName: formData.get("foodName"),
    calories: numberOrNull(formData.get("calories")),
    protein: numberOrNull(formData.get("protein")),
    carbohydrates: numberOrNull(formData.get("carbohydrates")),
    fat: numberOrNull(formData.get("fat")),
    notes: formData.get("notes")
  };

  try {
    await request("/api/nutrition/logs", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    nutritionForm.reset();
    showMessage("Food logged.");
    await loadNutritionData();
    notifyFitnessDataChanged();
  } catch (error) {
    showMessage(error.message);
  }
}

function renderSummary(summary) {
  nutritionSummary.innerHTML = `
    <p>${escapeHtml(summary.date)} totals</p>
    <p>${summary.calories} calories · ${summary.protein}g protein · ${summary.carbohydrates}g carbs · ${summary.fat}g fat</p>
  `;
}

function renderLogs(logs) {
  if (!logs.length) {
    nutritionList.textContent = "No nutrition logged yet.";
    return;
  }

  nutritionList.innerHTML = logs.map(renderLog).join("");
}

function renderLog(log) {
  return `
    <article class="nutrition-item">
      <h3>${escapeHtml(log.foodName)} · ${escapeHtml(log.logDate)}</h3>
      <p>${escapeHtml(log.mealType)} · ${log.calories ?? 0} calories</p>
      <p>${log.protein ?? 0}g protein · ${log.carbohydrates ?? 0}g carbs · ${log.fat ?? 0}g fat</p>
      ${log.notes ? `<p>${escapeHtml(log.notes)}</p>` : ""}
    </article>
  `;
}

function currentDateValue() {
  const dateInput = nutritionForm.elements.logDate;
  if (dateInput.value) {
    return dateInput.value;
  }
  return new Date().toISOString().slice(0, 10);
}

function numberOrNull(value) {
  return value === "" || value == null ? null : Number(value);
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll("\"", "&quot;")
    .replaceAll("'", "&#039;");
}

function notifyFitnessDataChanged() {
  window.dispatchEvent(new CustomEvent("liftlog:data-changed"));
}
