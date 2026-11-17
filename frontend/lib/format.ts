import type { MediaType, Title, Watchlist } from "./api";

const TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p";

export function posterUrl(path?: string | null) {
  return path ? `${TMDB_IMAGE_BASE}/w342${path}` : null;
}

export function backdropUrl(path?: string | null) {
  return path ? `${TMDB_IMAGE_BASE}/w1280${path}` : null;
}

export function year(date?: string | null) {
  return date ? date.slice(0, 4) : null;
}

/** 170 becomes "2h 50m". */
export function runtime(minutes?: number | null) {
  if (!minutes) return null;
  const hours = Math.floor(minutes / 60);
  const rest = minutes % 60;
  if (hours === 0) return `${rest}m`;
  return rest === 0 ? `${hours}h` : `${hours}h ${rest}m`;
}

export function kindLabel(mediaType: MediaType) {
  return mediaType === "TV" ? "TV" : "Movie";
}

export function titleCount(count: number) {
  return count === 1 ? "1 title" : `${count} titles`;
}

/** Identifies a TMDB title across search results and saved titles. */
export function tmdbKey(mediaType: MediaType, tmdbId?: number | null) {
  return tmdbId ? `${mediaType}:${tmdbId}` : null;
}

/** For each TMDB title, the ids of my lists that already contain it. */
export function listsByTmdbKey(lists: Watchlist[] = []) {
  const map = new Map<string, string[]>();
  for (const list of lists) {
    for (const item of list.items) {
      const key = tmdbKey(item.movie.mediaType, item.movie.tmdbId);
      if (key) map.set(key, [...(map.get(key) ?? []), list.id]);
    }
  }
  return map;
}

export function listIdsContaining(lists: Watchlist[] = [], title: Title) {
  return lists.filter(list => list.items.some(item => item.movie.id === title.id)).map(list => list.id);
}

/** A stable hue per title, for the placeholder shown when there is no poster image. */
export function hueFor(text: string) {
  let hash = 0;
  for (let i = 0; i < text.length; i++) {
    hash = (hash * 31 + text.charCodeAt(i)) % 360;
  }
  return hash;
}

export function initials(name: string) {
  return name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map(part => part[0]?.toUpperCase())
    .join("");
}
