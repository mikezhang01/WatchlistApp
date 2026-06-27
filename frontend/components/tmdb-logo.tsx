"use client";

import { useState } from "react";

export function TmdbLogo() {
  const [missing, setMissing] = useState(false);

  if (missing) {
    return (
      <p className="text-center text-sm text-neutral-600">
        Place the official TMDB logo at <code>/frontend/public/tmdb-logo.svg</code>
      </p>
    );
  }

  return (
    <img
      alt="The Movie Database (TMDB)"
      className="h-auto max-h-16 max-w-48 object-contain"
      onError={() => setMissing(true)}
      src="/tmdb-logo.svg"
    />
  );
}

