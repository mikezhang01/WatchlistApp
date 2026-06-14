"use client";

import { useEffect, useState } from "react";
import { api, Movie } from "../../lib/api";
import { PageTitle, Panel } from "../../components/ui";

export default function RecommendationsPage() {
  const [movies, setMovies] = useState<Movie[]>([]);

  useEffect(() => {
    api<Movie[]>("/api/recommendations").then(setMovies).catch(() => window.location.href = "/login");
  }, []);

  return (
    <>
      <PageTitle title="Recommendations" subtitle="Early MVP recommendations exclude movies already in your lists." />
      <div className="grid gap-3">
        {movies.map(movie => (
          <Panel key={movie.id}>
            <h2 className="font-medium">{movie.title}</h2>
            <p className="text-sm text-neutral-700">{movie.overview}</p>
          </Panel>
        ))}
      </div>
    </>
  );
}

