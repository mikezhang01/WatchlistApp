"use client";

import { FormEvent, useEffect, useState } from "react";
import { api, Movie, Review } from "../../../lib/api";
import { Button, PageTitle, Panel, TextArea } from "../../../components/ui";

export default function MoviePage({ params }: { params: { id: string } }) {
  const [movie, setMovie] = useState<Movie | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);

  function load() {
    api<Movie>(`/api/movies/${params.id}`).then(setMovie);
    api<Review[]>(`/api/movies/${params.id}/reviews`).then(setReviews);
  }

  useEffect(load, [params.id]);

  async function review(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    await api(`/api/movies/${params.id}/reviews`, {
      method: "POST",
      body: JSON.stringify({
        rating: Number(form.get("rating")),
        body: form.get("body"),
        visibility: form.get("visibility")
      })
    });
    event.currentTarget.reset();
    load();
  }

  if (!movie) return <PageTitle title="Loading movie" />;

  return (
    <>
      <PageTitle title={movie.title} subtitle={movie.overview} />
      <div className="grid gap-6 md:grid-cols-[360px_1fr]">
        <Panel>
          <form className="grid gap-3" onSubmit={review}>
            <input name="rating" type="number" min={1} max={5} placeholder="Rating 1-5" className="rounded border border-line px-3 py-2" required />
            <TextArea name="body" placeholder="Review" />
            <select name="visibility" className="rounded border border-line px-3 py-2" defaultValue="PUBLIC">
              <option value="PUBLIC">Public</option>
              <option value="PRIVATE">Private</option>
            </select>
            <Button type="submit">Post Review</Button>
          </form>
        </Panel>
        <div className="grid gap-3">
          {reviews.map(r => (
            <Panel key={r.id}>
              <h2 className="font-medium">{r.displayName} · {r.rating}/5</h2>
              <p className="text-sm text-neutral-700">{r.body}</p>
            </Panel>
          ))}
        </div>
      </div>
    </>
  );
}

