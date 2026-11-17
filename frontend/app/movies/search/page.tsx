"use client";

import { FormEvent, useEffect, useState } from "react";
import { api, Movie, Watchlist } from "../../../lib/api";
import { Button, Field, PageTitle, Panel } from "../../../components/ui";

const TMDB_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w342";

type SearchResult = {
  tmdbId?: number;
  mediaType: "MOVIE" | "TV";
  title: string;
  overview?: string;
  releaseDate?: string;
  posterPath?: string;
};

export default function MovieSearchPage() {
  const [results, setResults] = useState<SearchResult[]>([]);
  const [watchlists, setWatchlists] = useState<Watchlist[]>([]);
  const [selectedList, setSelectedList] = useState("");
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(1);
  const [hasSearched, setHasSearched] = useState(false);
  const [isSearching, setIsSearching] = useState(false);

  useEffect(() => {
    api<Watchlist[]>("/api/watchlists").then(data => {
      setWatchlists(data);
      setSelectedList(data[0]?.id ?? "");
    }).catch(() => undefined);
  }, []);

  async function runSearch(nextQuery: string, nextPage: number) {
    const trimmedQuery = nextQuery.trim();
    if (!trimmedQuery) {
      setResults([]);
      setHasSearched(false);
      setPage(1);
      return;
    }
    setIsSearching(true);
    try {
      setResults(await api<SearchResult[]>(`/api/movies/search?query=${encodeURIComponent(trimmedQuery)}&page=${nextPage}`));
      setQuery(trimmedQuery);
      setPage(nextPage);
      setHasSearched(true);
    } finally {
      setIsSearching(false);
    }
  }

  async function search(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    await runSearch(String(form.get("query")), 1);
  }

  async function changePage(nextPage: number) {
    await runSearch(query, nextPage);
  }

  async function importAndAdd(tmdbId?: number, mediaType?: "MOVIE" | "TV") {
    if (!tmdbId || !selectedList) return;
    const movie = await api<Movie>(`/api/movies/import/tmdb/${tmdbId}?mediaType=${mediaType ?? "MOVIE"}`, { method: "POST" });
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
          <Field name="query" placeholder="Search by title" defaultValue={query} required />
          <select className="rounded border border-line px-3 py-2" value={selectedList} onChange={e => setSelectedList(e.target.value)}>
            {watchlists.map(w => <option key={w.id} value={w.id}>{w.name}</option>)}
          </select>
          <Button type="submit" disabled={isSearching}>{isSearching ? "Searching..." : "Search"}</Button>
        </form>
      </Panel>
      <div className="mt-6 grid gap-3">
        {hasSearched && results.length === 0 ? (
          <Panel>
            <p className="text-sm text-neutral-600">No movies found for this search.</p>
          </Panel>
        ) : null}
        {results.map(result => (
          <Panel key={`${result.tmdbId}-${result.title}`}>
            <div className="grid gap-4 sm:grid-cols-[92px_1fr_auto] sm:items-start">
              <div className="h-[138px] w-[92px] overflow-hidden rounded border border-line bg-neutral-100">
                {result.posterPath ? (
                  <img
                    alt={`${result.title} poster`}
                    className="h-full w-full object-cover"
                    loading="lazy"
                    src={`${TMDB_POSTER_BASE_URL}${result.posterPath}`}
                  />
                ) : (
                  <div className="flex h-full items-center justify-center px-2 text-center text-xs text-neutral-500">
                    No poster
                  </div>
                )}
              </div>
              <div className="min-w-0">
                <div className="flex flex-wrap items-center gap-2">
                  <h2 className="font-medium">{result.title}</h2>
                  <span className="rounded border border-line px-2 py-0.5 text-xs text-neutral-600">
                    {result.mediaType === "TV" ? "TV" : "Movie"}
                  </span>
                </div>
                <p className="text-sm text-neutral-600">{result.releaseDate}</p>
                <p className="mt-2 text-sm leading-6">{result.overview || "No overview available."}</p>
              </div>
              <Button className="sm:w-24" type="button" onClick={() => importAndAdd(result.tmdbId, result.mediaType)} disabled={!selectedList || !result.tmdbId}>Add</Button>
            </div>
          </Panel>
        ))}
      </div>
      {hasSearched ? (
        <div className="mt-6 flex items-center justify-between gap-3">
          <Button type="button" onClick={() => changePage(page - 1)} disabled={isSearching || page <= 1}>
            Previous
          </Button>
          <span className="text-sm text-neutral-600">Page {page}</span>
          <Button type="button" onClick={() => changePage(page + 1)} disabled={isSearching || results.length === 0}>
            Next
          </Button>
        </div>
      ) : null}
    </>
  );
}
