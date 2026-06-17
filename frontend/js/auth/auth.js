import { clearCsrfToken, request } from "../api/apiClient.js";
import { clearAiFeedbackData, initAiFeedback, loadAiFeedbackData } from "../ai/feedback.js";
import { clearDashboardData, initDashboard, loadDashboardData } from "../dashboard/dashboard.js";
import { clearGoalData, initGoals, loadGoalData } from "../goals/goals.js";
import { clearNutritionData, initNutrition, loadNutritionData } from "../nutrition/nutrition.js";
import { showMessage, showUser } from "../shared/ui.js";
import { clearWorkoutData, initWorkouts, loadWorkoutData } from "../workouts/workouts.js";

const registerForm = document.querySelector("#register-form");
const loginForm = document.querySelector("#login-form");
const refreshButton = document.querySelector("#refresh-session");
const logoutButton = document.querySelector("#logout");

registerForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  await submitAuthForm("/api/auth/register", registerForm, "Account created.");
});

loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  await submitAuthForm("/api/auth/login", loginForm, "Signed in.");
});

refreshButton.addEventListener("click", async () => {
  try {
    const response = await request("/api/auth/refresh", { method: "POST" });
    clearCsrfToken();
    showUser(response.user);
    await loadDashboardData();
    await loadWorkoutData();
    await loadNutritionData();
    await loadGoalData();
    await loadAiFeedbackData();
    showMessage("Session refreshed.");
  } catch (error) {
    showMessage(error.message);
  }
});

logoutButton.addEventListener("click", async () => {
  try {
    await request("/api/auth/logout", { method: "POST" });
    clearCsrfToken();
    showUser(null);
    clearDashboardData();
    clearWorkoutData();
    clearNutritionData();
    clearGoalData();
    clearAiFeedbackData();
    showMessage("Signed out.");
  } catch (error) {
    showMessage(error.message);
  }
});

async function submitAuthForm(path, form, successMessage) {
  const formData = new FormData(form);
  const payload = Object.fromEntries(formData.entries());

  try {
    const response = await request(path, {
      method: "POST",
      body: JSON.stringify(payload)
    });
    clearCsrfToken();
    showUser(response.user);
    await loadDashboardData();
    await loadWorkoutData();
    await loadNutritionData();
    await loadGoalData();
    await loadAiFeedbackData();
    showMessage(successMessage);
    form.reset();
  } catch (error) {
    showMessage(error.message);
  }
}

async function loadCurrentUser() {
  try {
    const response = await request("/api/auth/me");
    showUser(response.user);
    await loadDashboardData();
    await loadWorkoutData();
    await loadNutritionData();
    await loadGoalData();
    await loadAiFeedbackData();
  } catch {
    showUser(null);
    clearDashboardData();
    clearWorkoutData();
    clearNutritionData();
    clearGoalData();
    clearAiFeedbackData();
  }
}

initDashboard();
initWorkouts();
initNutrition();
initGoals();
initAiFeedback();
loadCurrentUser();
