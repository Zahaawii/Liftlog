import { request } from "../api/apiClient.js";
import { showMessage, showUser } from "../shared/ui.js";

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
    showUser(response.user);
    showMessage("Session refreshed.");
  } catch (error) {
    showMessage(error.message);
  }
});

logoutButton.addEventListener("click", async () => {
  try {
    await request("/api/auth/logout", { method: "POST" });
    showUser(null);
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
    showUser(response.user);
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
  } catch {
    showUser(null);
  }
}

loadCurrentUser();
