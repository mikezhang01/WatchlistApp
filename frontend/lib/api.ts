import { authToken, clearAuth } from "./auth";
import type { WatchStatus } from "./theme";

export const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  constructor(public status: number, message: string, public fields: Record<string, string> = {}) {
    super(message);
  }
}

export type MediaType = "MOVIE" | "TV";
export type Visibility = "PRIVATE" | "PUBLIC";

export type User = {
  id: string;
  email: string;
  displayName: string;
  role: string;
};

export type AuthResponse = {
  token: string;
  user: User;
};

/** A title stored in CineStack (a movie or a TV show). */
export type Title = {
  id: string;
  tmdbId?: number;
  mediaType: MediaType;
  title: string;
  overview?: string;
  releaseDate?: string;
  runtimeMinutes?: number;
  posterPath?: string;
  backdropPath?: string;
  voteAverage?: number;
  genres: string[];
};

/** A search hit from TMDB. It has no CineStack id until it is imported. */
export type SearchResult = {
  tmdbId?: number;
  mediaType: MediaType;
  title: string;
  overview?: string;
  releaseDate?: string;
  posterPath?: string;
};

export type SearchPage = {
  results: SearchResult[];
  page: number;
  totalPages: number;
};

export type WatchlistItem = {
  id: string;
  movie: Title;
  status: WatchStatus;
  addedAt: string;
  watchedAt?: string;
  personalRating?: number | null;
  notes?: string | null;
};

export type Watchlist = {
  id: string;
  ownerId: string;
  ownerDisplayName: string;
  name: string;
  description?: string;
  visibility: Visibility;
  items: WatchlistItem[];
};

/**
 * The one place that talks to the backend. It adds the token, turns error responses
 * into ApiError, and signs the user out when the server says the token is no longer
 * valid, so individual pages never have to handle an expired session themselves.
 */
export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = authToken();
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers
      }
    });
  } catch {
    throw new ApiError(0, "Can't reach the server. Check your connection and try again.");
  }

  if (response.status === 401 && token) {
    clearAuth();
    throw new ApiError(401, "Your session expired. Log in again.");
  }
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new ApiError(response.status, body?.message ?? "Something went wrong", body?.fields ?? {});
  }
  if (response.status === 204) {
    return undefined as T;
  }
  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
}
