"use client";

import { useState } from "react";
import { Code, Text } from "@mantine/core";

export function TmdbLogo() {
  const [missing, setMissing] = useState(false);

  if (missing) {
    return (
      <Text size="sm" c="dimmed" ta="center">
        Place the official TMDB logo at <Code>/frontend/public/tmdb-logo.svg</Code>
      </Text>
    );
  }

  return (
    <img
      alt="The Movie Database (TMDB)"
      onError={() => setMissing(true)}
      src="/tmdb-logo.svg"
      style={{ height: "auto", maxHeight: 64, maxWidth: 192, objectFit: "contain" }}
    />
  );
}
