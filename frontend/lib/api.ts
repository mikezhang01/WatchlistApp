export const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export type User = {
  id: string;
  email: string;
  displayName: string;
  role: string;
};

export type Movie = {
  id: string;
  tmdbId?: number;
  title: string;
  overview?: string;
  releaseDate?: string;
  runtimeMinutes?: number;
  posterPath?: string;
  voteAverage?: number;
  genres: string[];
};

export type Watchlist = {
  id: string;
  ownerId: string;
  ownerDisplayName: string;
  name: string;
  description?: string;
  visibility: "PRIVATE" | "PUBLIC";
  items: WatchlistItem[];
};

export type WatchlistItem = {
  id: string;
  movie: Movie;
  status: "PLANNED" | "WATCHING" | "WATCHED" | "DROPPED";
  watchedAt?: string;
  personalRating?: number;
  notes?: string;
};

export type Review = {
  id: string;
  userId: string;
  displayName: string;
  movie: Movie;
  rating: number;
  body?: string;
  visibility: "PRIVATE" | "PUBLIC";
};

export type AuthResponse = {
  token: string;
  user: User;
};

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = typeof window === "undefined" ? null : localStorage.getItem("cinestack_token");
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers
    }
  });
  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: response.statusText }));
    throw new Error(error.message ?? "Request failed");
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return response.json() as Promise<T>;
}

