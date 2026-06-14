"use client";

import { FormEvent, useEffect, useState } from "react";
import { api, Movie, Watchlist } from "../../../lib/api";
import { Button, Field, PageTitle, Panel } from "../../../components/ui";

type SearchResult = {
  tmdbId?: number;
  title: string;
  overview?: string;
  releaseDate?: string;
  posterPath?: string;
};

export default function MovieSearchPage() {
  const [results, setResults] = useState<SearchResult[]>([]);
  const [watchlists, setWatchlists] = useState<Watchlist[]>([]);
  const [selectedList, setSelectedList] = useState("");

  useEffect(() => {
    api<Watchlist[]>("/api/watchlists").then(data => {
      setWatchlists(data);
      setSelectedList(data[0]?.id ?? "");
    }).catch(() => undefined);
  }, []);

  async function search(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setResults(await api<SearchResult[]>(`/api/movies/search?query=${encodeURIComponent(String(form.get("query")))}&page=1`));
  }

  async function importAndAdd(tmdbId?: number) {
    if (!tmdbId || !selectedList) return;
    const movie = await api<Movie>(`/api/movies/import/tmdb/${tmdbId}`, { method: "POST" });
    await api(`/api/watchlists/${selectedList}/items`, {
      method: "POST",
      body: JSON.stringify({ movieId: movie.id, status: "PLANNED" })
    });
  }

  return (
    <>
      <PageTitle title="Search Movies" subtitle="Search TMDB-backed metadata and add movies to a watchlist." />
      <Panel>
        <form className="grid gap-3 md:grid-cols-[1fr_220px_120px]" onSubmit={search}>
          <Field name="query" placeholder="Search by title" required />
          <select className="rounded border border-line px-3 py-2" value={selectedList} onChange={e => setSelectedList(e.target.value)}>
            {watchlists.map(w => <option key={w.id} value={w.id}>{w.name}</option>)}
          </select>
          <Button type="submit">Search</Button>
        </form>
      </Panel>
      <div className="mt-6 grid gap-3">
        {results.map(result => (
          <Panel key={`${result.tmdbId}-${result.title}`}>
            <div className="flex items-start justify-between gap-4">
              <div>
                <h2 className="font-medium">{result.title}</h2>
                <p className="text-sm text-neutral-600">{result.releaseDate}</p>
                <p className="mt-2 text-sm">{result.overview}</p>
              </div>
              <Button type="button" onClick={() => importAndAdd(result.tmdbId)} disabled={!selectedList || !result.tmdbId}>Add</Button>
            </div>
          </Panel>
        ))}
      </div>
    </>
  );
}

