const TOKEN_KEY = "TASKOPS_TOKEN";
const USER_KEY = "TASKOPS_USER";

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || "";
}

export function setAuth(auth) {
  localStorage.setItem(TOKEN_KEY, auth.token || "");
  localStorage.setItem(USER_KEY, JSON.stringify(auth.user || {}));
}

export function getUser() {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw);
  } catch (error) {
    return null;
  }
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}
