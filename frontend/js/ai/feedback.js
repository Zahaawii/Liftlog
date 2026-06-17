import { request } from "../api/apiClient.js";
import { showMessage } from "../shared/ui.js";

const feedbackForm = document.querySelector("#ai-feedback-form");
const feedbackList = document.querySelector("#ai-feedback-list");
const loadFeedbackButton = document.querySelector("#load-ai-feedback");

export function initAiFeedback() {
  feedbackForm.addEventListener("submit", handleFeedbackSubmit);
  loadFeedbackButton.addEventListener("click", loadAiFeedbackData);
  clearAiFeedbackData();
}

export async function loadAiFeedbackData() {
  try {
    const response = await request("/api/ai/feedback?page=0&size=10");
    renderFeedbackHistory(response.items || []);
  } catch (error) {
    clearAiFeedbackData();
    showMessage(error.message);
  }
}

export function clearAiFeedbackData() {
  feedbackList.textContent = "Sign in to request AI feedback.";
}

async function handleFeedbackSubmit(event) {
  event.preventDefault();
  const formData = new FormData(feedbackForm);
  const payload = {
    requestType: formData.get("requestType"),
    question: formData.get("question"),
    includeWorkouts: formData.has("includeWorkouts"),
    includeNutrition: formData.has("includeNutrition"),
    includeGoals: formData.has("includeGoals")
  };

  try {
    await request("/api/ai/feedback", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    feedbackForm.reset();
    resetContextDefaults();
    showMessage("AI feedback requested.");
    await loadAiFeedbackData();
    notifyFitnessDataChanged();
  } catch (error) {
    showMessage(error.message);
  }
}

function renderFeedbackHistory(items) {
  if (!items.length) {
    feedbackList.innerHTML = `
      <article class="empty-state">
        <h3>No AI feedback yet</h3>
        <p class="metric-subtext">Ask for a weekly review after logging workouts, nutrition, or goals.</p>
      </article>
    `;
    return;
  }

  feedbackList.innerHTML = items.map(renderFeedbackItem).join("");
}

function renderFeedbackItem(item) {
  const title = formatRequestType(item.requestType);
  const createdAt = item.createdAt ? formatDateTime(item.createdAt) : "";
  const summary = item.promptSummary || item.question || "";
  const feedback = item.feedback || item.response || "";

  return `
    <article class="ai-feedback-item">
      <div class="item-row">
        <h3>${escapeHtml(title)}</h3>
        ${createdAt ? `<span class="pill">${escapeHtml(createdAt)}</span>` : ""}
      </div>
      ${summary ? `<p><strong>Request:</strong> ${escapeHtml(summary)}</p>` : ""}
      <p class="ai-feedback-content">${escapeHtml(feedback || "Feedback is being prepared.")}</p>
    </article>
  `;
}

function formatRequestType(value) {
  if (!value) {
    return "AI feedback";
  }

  return String(value)
    .replaceAll("_", " ")
    .replaceAll("-", " ")
    .toLowerCase()
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function resetContextDefaults() {
  feedbackForm.elements.includeWorkouts.checked = true;
  feedbackForm.elements.includeNutrition.checked = true;
  feedbackForm.elements.includeGoals.checked = true;
}

function formatDateTime(value) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString(undefined, {
    dateStyle: "medium",
    timeStyle: "short"
  });
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
