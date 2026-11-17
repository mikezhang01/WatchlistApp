"use client";

import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api, type MediaType, type SearchPage, type Title, type Visibility, type Watchlist, type WatchlistItem } from "./api";
import type { WatchStatus } from "./theme";

/**
 * All server data goes through these hooks. React Query caches each result under a key,
 * gives every page the same loading and error states, and refetches a key when a
 * mutation says it changed ("invalidate").
 */
const keys = {
  lists: ["lists"] as const,
  list: (id: string) => ["list", id] as const,
  title: (id: string) => ["title", id] as const,
  search: (query: string, page: number) => ["search", query, page] as const
};

export function useMyLists(enabled: boolean) {
  return useQuery({ queryKey: keys.lists, queryFn: () => api<Watchlist[]>("/api/watchlists"), enabled });
}

export function useList(id: string) {
  return useQuery({ queryKey: keys.list(id), queryFn: () => api<Watchlist>(`/api/watchlists/${id}`) });
}

export function useTitle(id: string) {
  return useQuery({ queryKey: keys.title(id), queryFn: () => api<Title>(`/api/movies/${id}`) });
}

export function useSearch(query: string, page: number) {
  return useQuery({
    queryKey: keys.search(query, page),
    queryFn: () => api<SearchPage>(`/api/movies/search?query=${encodeURIComponent(query)}&page=${page}`),
    enabled: query.length > 0,
    // Keep the current page on screen while the next one loads.
    placeholderData: keepPreviousData
  });
}

export type ListInput = { name: string; description: string; visibility: Visibility };

export function useCreateList() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (input: ListInput) =>
      api<Watchlist>("/api/watchlists", { method: "POST", body: JSON.stringify(input) }),
    onSuccess: () => client.invalidateQueries({ queryKey: keys.lists })
  });
}

export function useUpdateList(id: string) {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (input: ListInput) =>
      api<Watchlist>(`/api/watchlists/${id}`, { method: "PATCH", body: JSON.stringify(input) }),
    onSuccess: () => {
      client.invalidateQueries({ queryKey: keys.lists });
      client.invalidateQueries({ queryKey: keys.list(id) });
    }
  });
}

export function useDeleteList() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api<void>(`/api/watchlists/${id}`, { method: "DELETE" }),
    onSuccess: (_, id) => {
      client.removeQueries({ queryKey: keys.list(id) });
      client.invalidateQueries({ queryKey: keys.lists });
    }
  });
}

/** Saves a TMDB title into CineStack (or returns the existing row) so it has an id. */
export function importTitle(tmdbId: number, mediaType: MediaType) {
  return api<Title>(`/api/movies/import/tmdb/${tmdbId}?mediaType=${mediaType}`, { method: "POST" });
}

export function useAddToList() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: ({ listId, titleId }: { listId: string; titleId: string }) =>
      api<WatchlistItem>(`/api/watchlists/${listId}/items`, {
        method: "POST",
        body: JSON.stringify({ movieId: titleId, status: "PLANNED" })
      }),
    onSuccess: (_, { listId }) => {
      client.invalidateQueries({ queryKey: keys.lists });
      client.invalidateQueries({ queryKey: keys.list(listId) });
    }
  });
}

export type ItemInput = { status: WatchStatus; personalRating: number | null; notes: string | null };

export function useUpdateItem(listId: string) {
  const client = useQueryClient();
  return useMutation({
    mutationFn: ({ itemId, input }: { itemId: string; input: ItemInput }) =>
      api<WatchlistItem>(`/api/watchlists/${listId}/items/${itemId}`, {
        method: "PATCH",
        body: JSON.stringify(input)
      }),
    onSuccess: () => {
      client.invalidateQueries({ queryKey: keys.lists });
      client.invalidateQueries({ queryKey: keys.list(listId) });
    }
  });
}

export function useRemoveItem(listId: string) {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (itemId: string) =>
      api<void>(`/api/watchlists/${listId}/items/${itemId}`, { method: "DELETE" }),
    onSuccess: () => {
      client.invalidateQueries({ queryKey: keys.lists });
      client.invalidateQueries({ queryKey: keys.list(listId) });
    }
  });
}
