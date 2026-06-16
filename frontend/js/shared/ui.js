const messageElement = document.querySelector("#message");
const currentUserElement = document.querySelector("#current-user");

export function showMessage(message) {
  messageElement.textContent = message;
}

export function showUser(user) {
  currentUserElement.textContent = user
    ? JSON.stringify(user, null, 2)
    : "Not signed in";
}
