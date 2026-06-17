const messageElement = document.querySelector("#message");
const currentUserElement = document.querySelector("#current-user");
const userLabelElement = document.querySelector("#user-label");
const welcomeNameElement = document.querySelector("#welcome-name");

export function showMessage(message) {
  messageElement.textContent = message;
}

export function showUser(user) {
  document.body.classList.toggle("authenticated", Boolean(user));
  document.body.classList.toggle("unauthenticated", !user);

  currentUserElement.textContent = user
    ? JSON.stringify(user, null, 2)
    : "Not signed in";

  if (userLabelElement) {
    userLabelElement.textContent = user?.displayName || user?.email || "Signed in";
  }

  if (welcomeNameElement) {
    welcomeNameElement.textContent = user?.displayName || "athlete";
  }
}
