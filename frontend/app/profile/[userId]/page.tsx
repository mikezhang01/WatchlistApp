"use client";

import { useEffect, useState } from "react";
import { api, Review, User, Watchlist } from "../../../lib/api";
import { PageTitle, Panel } from "../../../components/ui";

export default function ProfilePage({ params }: { params: { userId: string } }) {
  const [user, setUser] = useState<User | null>(null);
  const [watchlists, setWatchlists] = useState<Watchlist[]>([]);
  const [reviews, setReviews] = useState<Review[]>([]);

  useEffect(() => {
    api<User>(`/api/users/${params.userId}`).then(setUser);
    api<Watchlist[]>(`/api/users/${params.userId}/public-watchlists`).then(setWatchlists);
    api<Review[]>(`/api/users/${params.userId}/reviews`).then(setReviews);
  }, [params.userId]);

  return (
    <>
      <PageTitle title={user?.displayName ?? "Profile"} subtitle="Public watchlists and reviews." />
      <div className="grid gap-6 md:grid-cols-2">
        <section>
          <h2 className="mb-3 font-medium">Public Watchlists</h2>
          <div className="grid gap-3">
            {watchlists.map(w => <Panel key={w.id}><h3 className="font-medium">{w.name}</h3><p className="text-sm">{w.items.length} movies</p></Panel>)}
          </div>
        </section>
        <section>
          <h2 className="mb-3 font-medium">Reviews</h2>
          <div className="grid gap-3">
            {reviews.map(r => <Panel key={r.id}><h3 className="font-medium">{r.movie.title} · {r.rating}/5</h3><p className="text-sm">{r.body}</p></Panel>)}
          </div>
        </section>
      </div>
    </>
  );
}

