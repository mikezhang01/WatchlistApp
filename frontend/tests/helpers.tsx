import { MantineProvider } from "@mantine/core";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render } from "@testing-library/react";
import { vi } from "vitest";
import type { Title, Watchlist } from "../lib/api";
import { theme } from "../lib/theme";

/** Renders a component with the same providers the app gives it. */
export function renderWithApp(ui: React.ReactElement) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });
  return render(
    // env="test" turns off Mantine's transitions and portals so menus appear at once.
    <MantineProvider theme={theme} env="test">
      <QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>
    </MantineProvider>
  );
}

type Reply = { status?: number; body?: unknown };

/**
 * Replaces fetch for one test. `routes` maps "METHOD /path" to the reply; a request
 * that is not listed fails the test, so nothing reaches the network by accident.
 */
export function stubApi(routes: Record<string, Reply>) {
  const calls: { method: string; path: string; body: unknown; headers: Record<string, string> }[] = [];
  const fetchMock = vi.fn(async (url: string, init: RequestInit = {}) => {
    const method = init.method ?? "GET";
    const path = url.replace("http://localhost:8080", "");
    calls.push({
      method,
      path,
      body: init.body ? JSON.parse(String(init.body)) : undefined,
      headers: (init.headers ?? {}) as Record<string, string>
    });
    const reply = routes[`${method} ${path}`];
    if (!reply) throw new Error(`Unexpected request in test: ${method} ${path}`);
    const status = reply.status ?? 200;
    return new Response(reply.body === undefined ? "" : JSON.stringify(reply.body), { status });
  });
  vi.stubGlobal("fetch", fetchMock);
  return calls;
}

export function signIn() {
  localStorage.setItem("cinestack_token", "test-token");
  localStorage.setItem("cinestack_user", JSON.stringify({ id: "user-1", email: "me@example.com", displayName: "Me", role: "USER" }));
}

export function aTitle(overrides: Partial<Title> = {}): Title {
  return { id: "title-1", tmdbId: 949, mediaType: "MOVIE", title: "Heat", genres: ["Crime"], ...overrides };
}

export function aList(overrides: Partial<Watchlist> = {}): Watchlist {
  return { id: "list-1", ownerId: "user-1", ownerDisplayName: "Me", name: "Weekend", visibility: "PRIVATE", items: [], ...overrides };
}
