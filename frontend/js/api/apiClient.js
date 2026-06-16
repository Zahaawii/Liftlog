const defaultHeaders = {
  "Content-Type": "application/json"
};

let csrfToken = null;

export async function request(path, options = {}) {
  const method = options.method || "GET";
  const headers = { ...defaultHeaders, ...(options.headers || {}) };

  if (method !== "GET" && method !== "HEAD") {
    csrfToken ||= await fetchCsrfToken();
    headers["X-XSRF-TOKEN"] = csrfToken;
  }

  const response = await fetch(path, {
    ...options,
    method,
    headers,
    credentials: "include"
  });

  const text = await response.text();
  const body = text ? JSON.parse(text) : null;

  if (!response.ok) {
    const message = body?.message || "Request failed.";
    throw new Error(message);
  }

  return body;
}

export async function fetchCsrfToken() {
  const response = await fetch("/api/auth/csrf", {
    credentials: "include"
  });

  if (!response.ok) {
    throw new Error("Could not prepare request security.");
  }

  const body = await response.json();
  csrfToken = body.csrfToken;
  return csrfToken;
}

export function clearCsrfToken() {
  csrfToken = null;
}
