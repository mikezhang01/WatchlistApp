"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { api, User, Watchlist } from "../../lib/api";
import { PageTitle, Panel } from "../../components/ui";

export default function DashboardPage() {
  const [user, setUser] = useState<User | null>(null);
  const [watchlists, setWatchlists] = useState<Watchlist[]>([]);

  useEffect(() => {
    api<User>("/api/auth/me").then(setUser).catch(() => window.location.href = "/login");
    api<Watchlist[]>("/api/watchlists").then(setWatchlists).catch(() => setWatchlists([]));
  }, []);

  const watched = watchlists.flatMap(w => w.items).filter(i => i.status === "WATCHED").length;
  const rated = watchlists.flatMap(w => w.items).filter(i => i.personalRating).length;

  return (
    <>
      <PageTitle title={`Dashboard${user ? ` for ${user.displayName}` : ""}`} subtitle="Your watchlists, watched history, and next recommendations." />
      <div className="grid gap-4 md:grid-cols-3">
        <Panel><p className="text-sm text-neutral-600">Watchlists</p><p className="text-3xl font-semibold">{watchlists.length}</p></Panel>
        <Panel><p className="text-sm text-neutral-600">Watched</p><p className="text-3xl font-semibold">{watched}</p></Panel>
        <Panel><p className="text-sm text-neutral-600">Rated</p><p className="text-3xl font-semibold">{rated}</p></Panel>
      </div>
      <div className="mt-6 grid gap-3">
        {watchlists.map(watchlist => (
          <Link key={watchlist.id} className="rounded border border-line bg-white p-4 hover:border-accent" href={`/watchlists/${watchlist.id}`}>
            <div className="flex items-center justify-between">
              <div>
                <h2 className="font-medium">{watchlist.name}</h2>
                <p className="text-sm text-neutral-600">{watchlist.items.length} movies</p>
              </div>
              <span className="text-xs">{watchlist.visibility}</span>
            </div>
          </Link>
        ))}
      </div>
    </>
  );
}

