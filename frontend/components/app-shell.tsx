"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { CircleHelp, Film, LogOut, Search, Star, UserRound } from "lucide-react";
import { currentUser, logout } from "../lib/auth";
import type { User } from "../lib/api";

export function AppShell({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    const syncUser = () => setUser(currentUser());
    syncUser();
    window.addEventListener("cinestack-auth-change", syncUser);
    return () => window.removeEventListener("cinestack-auth-change", syncUser);
  }, []);

  return (
    <div className="min-h-screen">
      <header className="border-b border-line bg-white">
        <div className="mx-auto flex max-w-6xl flex-col gap-2 px-4 py-3 sm:flex-row sm:items-center sm:justify-between">
          <Link href="/dashboard" className="flex items-center gap-2 font-semibold">
            <Film size={22} className="text-accent" />
            CineStack
          </Link>
          <nav className="flex w-full items-center gap-1 overflow-x-auto text-sm sm:w-auto sm:gap-2">
            {user ? <Link className="rounded px-3 py-2 hover:bg-panel" href="/watchlists">Watchlists</Link> : null}
            <Link className="rounded px-3 py-2 hover:bg-panel" href="/movies/search">
              <Search size={16} className="inline" /> Search
            </Link>
            {user ? <Link className="rounded px-3 py-2 hover:bg-panel" href="/recommendations">
              <Star size={16} className="inline" /> Picks
            </Link> : null}
            {user ? <Link className="rounded px-3 py-2 hover:bg-panel" href="/reviews">Reviews</Link> : null}
            <Link className="rounded px-3 py-2 hover:bg-panel" href="/about" title="About CineStack">
              <CircleHelp size={16} className="inline" /> About
            </Link>
            {user ? (
              <>
                <Link className="rounded px-3 py-2 hover:bg-panel" href="/dashboard" title="Account">
                  <UserRound size={16} />
                </Link>
                <button className="rounded px-3 py-2 hover:bg-panel" onClick={logout} title="Log out">
                  <LogOut size={16} />
                </button>
              </>
            ) : (
              <>
                <Link className="rounded px-3 py-2 hover:bg-panel" href="/login">Login</Link>
                <Link className="rounded px-3 py-2 hover:bg-panel" href="/register">Register</Link>
              </>
            )}
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-6">{children}</main>
      <footer className="border-t border-line bg-white">
        <div className="mx-auto flex max-w-6xl flex-col gap-1 px-4 py-4 text-xs text-neutral-600 sm:flex-row sm:items-center sm:justify-between">
          <p>This application uses TMDB and the TMDB APIs but is not endorsed, certified, or otherwise approved by TMDB.</p>
          <Link className="font-medium text-accent" href="/about">Attribution and legal information</Link>
        </div>
      </footer>
    </div>
  );
}
