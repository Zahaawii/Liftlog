import { request } from "../api/apiClient.js";
import { showMessage } from "../shared/ui.js";

const nutritionForm = document.querySelector("#nutrition-form");
const nutritionSummary = document.querySelector("#nutrition-summary");
const nutritionList = document.querySelector("#nutrition-list");
const loadNutritionButton = document.querySelector("#load-nutrition");

export function initNutrition() {
  nutritionForm.addEventListener("submit", handleNutritionSubmit);
  loadNutritionButton.addEventListener("click", loadNutritionData);
  setDefaultNutritionDate();
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
    setDefaultNutritionDate();
    showMessage("Food logged.");
    await loadNutritionData();
    notifyFitnessDataChanged();
  } catch (error) {
    showMessage(error.message);
  }
}

function renderSummary(summary) {
  const protein = Number(summary.protein) || 0;
  const proteinMeterWidth = Math.min(100, Math.round((protein / 160) * 100));

  nutritionSummary.innerHTML = `
    <div class="metric-card-header">
      <div>
        <h3>Protein today</h3>
        <p class="metric-subtext">${escapeHtml(summary.date)} totals</p>
      </div>
      <span class="pill protein-pill">${formatNumber(protein)}g</span>
    </div>
    <p class="metric-value">${formatNumber(protein)}g</p>
    <div class="progress-track" aria-label="Protein logged today">
      <span style="width: ${proteinMeterWidth}%"></span>
    </div>
    <p class="metric-subtext">${formatNumber(summary.calories)} calories · ${formatNumber(summary.carbohydrates)}g carbs · ${formatNumber(summary.fat)}g fat</p>
  `;
}

function renderLogs(logs) {
  if (!logs.length) {
    nutritionList.innerHTML = `
      <article class="empty-state">
        <h3>No food logged yet</h3>
        <p class="metric-subtext">Log a protein source to start tracking today's intake.</p>
      </article>
    `;
    return;
  }

  nutritionList.innerHTML = logs.map(renderLog).join("");
}

function renderLog(log) {
  return `
    <article class="nutrition-item">
      <div class="item-row">
        <h3>${escapeHtml(log.foodName)}</h3>
        <span class="pill protein-pill">${formatNumber(log.protein)}g protein</span>
      </div>
      <p class="item-meta">${escapeHtml(formatMealType(log.mealType))} · ${escapeHtml(log.logDate)} · ${formatNumber(log.calories)} calories</p>
      <p>${formatNumber(log.carbohydrates)}g carbs · ${formatNumber(log.fat)}g fat</p>
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

function setDefaultNutritionDate() {
  if (!nutritionForm.elements.logDate.value) {
    nutritionForm.elements.logDate.value = new Date().toISOString().slice(0, 10);
  }
}

function formatMealType(value) {
  return String(value || "Meal")
    .replaceAll("_", " ")
    .toLowerCase()
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
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

function notifyFitnessDataChanged() {
  window.dispatchEvent(new CustomEvent("liftlog:data-changed"));
}
