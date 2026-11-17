"use client";

import { AuthResponse, User } from "./api";

export function saveAuth(auth: AuthResponse) {
  localStorage.setItem("cinestack_token", auth.token);
  localStorage.setItem("cinestack_user", JSON.stringify(auth.user));
  window.dispatchEvent(new Event("cinestack-auth-change"));
}

export function currentUser(): User | null {
  if (typeof window === "undefined") {
    return null;
  }
  const raw = localStorage.getItem("cinestack_user");
  try {
    return raw ? JSON.parse(raw) as User : null;
  } catch {
    clearAuth();
    return null;
  }
}

export function authToken(): string | null {
  return typeof window === "undefined" ? null : localStorage.getItem("cinestack_token");
}

export function clearAuth() {
  localStorage.removeItem("cinestack_token");
  localStorage.removeItem("cinestack_user");
  window.dispatchEvent(new Event("cinestack-auth-change"));
}

export function logout() {
  clearAuth();
  window.location.href = "/login";
}
