"use client";

import Link from "next/link";
import { Film, LogOut, Search, Star, UserRound } from "lucide-react";
import { logout } from "../lib/auth";

export function AppShell({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen">
      <header className="border-b border-line bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
          <Link href="/dashboard" className="flex items-center gap-2 font-semibold">
            <Film size={22} className="text-accent" />
            CineStack
          </Link>
          <nav className="flex items-center gap-2 text-sm">
            <Link className="rounded px-3 py-2 hover:bg-panel" href="/watchlists">Watchlists</Link>
            <Link className="rounded px-3 py-2 hover:bg-panel" href="/movies/search">
              <Search size={16} className="inline" /> Search
            </Link>
            <Link className="rounded px-3 py-2 hover:bg-panel" href="/recommendations">
              <Star size={16} className="inline" /> Picks
            </Link>
            <Link className="rounded px-3 py-2 hover:bg-panel" href="/reviews">Reviews</Link>
            <button className="rounded px-3 py-2 hover:bg-panel" onClick={logout} title="Log out">
              <LogOut size={16} />
            </button>
            <Link className="rounded px-3 py-2 hover:bg-panel" href="/login" title="Account">
              <UserRound size={16} />
            </Link>
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-6">{children}</main>
    </div>
  );
}

