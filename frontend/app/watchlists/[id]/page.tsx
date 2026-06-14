"use client";

import { FormEvent, useEffect, useState } from "react";
import { api, Watchlist } from "../../../lib/api";
import { Button, PageTitle, Panel } from "../../../components/ui";

export default function WatchlistDetailPage({ params }: { params: { id: string } }) {
  const [watchlist, setWatchlist] = useState<Watchlist | null>(null);

  function load() {
    api<Watchlist>(`/api/watchlists/${params.id}`).then(setWatchlist);
  }

  useEffect(load, [params.id]);

  async function updateItem(event: FormEvent<HTMLFormElement>, itemId: string) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    await api(`/api/watchlists/${params.id}/items/${itemId}`, {
      method: "PATCH",
      body: JSON.stringify({
        status: form.get("status"),
        personalRating: form.get("personalRating") ? Number(form.get("personalRating")) : null,
        notes: form.get("notes")
      })
    });
    load();
  }

  if (!watchlist) return <PageTitle title="Loading watchlist" />;

  return (
    <>
      <PageTitle title={watchlist.name} subtitle={`${watchlist.visibility} · ${watchlist.items.length} movies`} />
      <div className="grid gap-4">
        {watchlist.items.map(item => (
          <Panel key={item.id}>
            <div className="grid gap-3 md:grid-cols-[1fr_320px]">
              <div>
                <h2 className="font-medium">{item.movie.title}</h2>
                <p className="mt-1 text-sm text-neutral-600">{item.movie.overview}</p>
              </div>
              <form className="grid gap-2" onSubmit={(event) => updateItem(event, item.id)}>
                <select name="status" className="rounded border border-line px-3 py-2" defaultValue={item.status}>
                  <option value="PLANNED">Planned</option>
                  <option value="WATCHING">Watching</option>
                  <option value="WATCHED">Watched</option>
                  <option value="DROPPED">Dropped</option>
                </select>
                <input name="personalRating" type="number" min={1} max={5} defaultValue={item.personalRating ?? ""} className="rounded border border-line px-3 py-2" placeholder="Rating 1-5" />
                <input name="notes" defaultValue={item.notes ?? ""} className="rounded border border-line px-3 py-2" placeholder="Notes" />
                <Button type="submit">Update</Button>
              </form>
            </div>
          </Panel>
        ))}
      </div>
    </>
  );
}

