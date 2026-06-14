"use client";

import { AuthResponse, User } from "./api";

export function saveAuth(auth: AuthResponse) {
  localStorage.setItem("cinestack_token", auth.token);
  localStorage.setItem("cinestack_user", JSON.stringify(auth.user));
}

export function currentUser(): User | null {
  const raw = localStorage.getItem("cinestack_user");
  return raw ? JSON.parse(raw) as User : null;
}

export function logout() {
  localStorage.removeItem("cinestack_token");
  localStorage.removeItem("cinestack_user");
  window.location.href = "/login";
}

