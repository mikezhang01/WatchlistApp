"use client";

import { FormEvent, useEffect, useState } from "react";
import Link from "next/link";
import { ApiError, api, Watchlist } from "../../lib/api";
import { clearAuth } from "../../lib/auth";
import { Button, Field, PageTitle, Panel, TextArea } from "../../components/ui";

export default function WatchlistsPage() {
  const [watchlists, setWatchlists] = useState<Watchlist[]>([]);
  const [error, setError] = useState("");

  function load() {
    api<Watchlist[]>("/api/watchlists").then(data => {
      setWatchlists(data);
      setError("");
    }).catch(err => {
      if (err instanceof ApiError && err.status === 401) {
        clearAuth();
        window.location.href = "/login";
        return;
      }
      setError(err instanceof Error ? err.message : "Could not load watchlists");
    });
  }

  useEffect(load, []);

  async function create(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    await api<Watchlist>("/api/watchlists", {
      method: "POST",
      body: JSON.stringify({
        name: form.get("name"),
        description: form.get("description"),
        visibility: form.get("visibility")
      })
    });
    event.currentTarget.reset();
    load();
  }

  return (
    <>
      <PageTitle title="Watchlists" subtitle="Create focused lists and decide which ones are public." />
      {error ? <p className="mb-4 text-sm text-berry">{error}</p> : null}
      <div className="grid gap-6 md:grid-cols-[360px_1fr]">
        <Panel>
          <form className="grid gap-3" onSubmit={create}>
            <Field name="name" placeholder="List name" required />
            <TextArea name="description" placeholder="Description" />
            <select name="visibility" className="rounded border border-line px-3 py-2" defaultValue="PRIVATE">
              <option value="PRIVATE">Private</option>
              <option value="PUBLIC">Public</option>
            </select>
            <Button type="submit">Create List</Button>
          </form>
        </Panel>
        <div className="grid gap-3">
          {watchlists.map(w => (
            <Link className="rounded border border-line bg-white p-4 hover:border-accent" href={`/watchlists/${w.id}`} key={w.id}>
              <h2 className="font-medium">{w.name}</h2>
              <p className="text-sm text-neutral-600">{w.description || "No description"} · {w.items.length} movies · {w.visibility}</p>
            </Link>
          ))}
        </div>
      </div>
    </>
  );
}
