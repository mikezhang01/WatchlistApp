"use client";

import { useEffect, useMemo, useState, useSyncExternalStore } from "react";
import { usePathname, useRouter } from "next/navigation";
import type { AuthResponse, User } from "./api";

const TOKEN_KEY = "cinestack_token";
const USER_KEY = "cinestack_user";
const CHANGE_EVENT = "cinestack-auth-change";

export function saveAuth(auth: AuthResponse) {
  localStorage.setItem(TOKEN_KEY, auth.token);
  localStorage.setItem(USER_KEY, JSON.stringify(auth.user));
  window.dispatchEvent(new Event(CHANGE_EVENT));
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  window.dispatchEvent(new Event(CHANGE_EVENT));
}

export function authToken(): string | null {
  return typeof window === "undefined" ? null : localStorage.getItem(TOKEN_KEY);
}

function subscribe(onChange: () => void) {
  window.addEventListener(CHANGE_EVENT, onChange);
  // "storage" fires when another tab logs in or out.
  window.addEventListener("storage", onChange);
  return () => {
    window.removeEventListener(CHANGE_EVENT, onChange);
    window.removeEventListener("storage", onChange);
  };
}

/**
 * The signed-in user, kept in step with localStorage.
 *
 * `ready` is false on the server and during the first browser render, when the stored
 * session cannot be read yet. Wait for it before deciding someone is logged out.
 */
export function useAuth(): { user: User | null; ready: boolean } {
  const raw = useSyncExternalStore(subscribe, () => localStorage.getItem(USER_KEY), () => null);
  const [ready, setReady] = useState(false);
  useEffect(() => setReady(true), []);

  const user = useMemo(() => {
    if (!raw) return null;
    try {
      return JSON.parse(raw) as User;
    } catch {
      return null;
    }
  }, [raw]);

  return { user: ready ? user : null, ready };
}

/** For pages that only make sense signed in: sends visitors to the login page and back. */
export function useRequireAuth() {
  const auth = useAuth();
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    if (auth.ready && !auth.user) {
      router.replace(`/login?next=${encodeURIComponent(pathname)}`);
    }
  }, [auth.ready, auth.user, pathname, router]);

  return auth;
}
