export function getSession() {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem("procure_session");
  return raw ? JSON.parse(raw) : null;
}

export function setSession(session) {
  localStorage.setItem("procure_session", JSON.stringify(session));
}

export function clearSession() {
  localStorage.removeItem("procure_session");
}
