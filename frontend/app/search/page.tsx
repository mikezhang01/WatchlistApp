"use client";

import { Suspense, useState } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { ActionIcon, Button, EmptyState, Group, SegmentedControl, Stack, Text, Title } from "@mantine/core";
import { Check, Plus, Search } from "lucide-react";
import { AddToListMenu } from "../../components/add-to-list-menu";
import { LoadError } from "../../components/page-states";
import { KindBadge, TitleCard, TitleGrid, TitleGridSkeleton } from "../../components/title-card";
import type { SearchResult } from "../../lib/api";
import { useAuth } from "../../lib/auth";
import { listsByTmdbKey, titleCount, tmdbKey, year } from "../../lib/format";
import { notifyError } from "../../lib/notify";
import { importTitle, useMyLists, useSearch } from "../../lib/queries";

type Kind = "ALL" | "MOVIE" | "TV";

export default function SearchPage() {
  // useSearchParams needs a Suspense boundary in the App Router.
  return (
    <Suspense fallback={<TitleGridSkeleton />}>
      <SearchResults />
    </Suspense>
  );
}

function SearchResults() {
  const params = useSearchParams();
  const pathname = usePathname();
  const router = useRouter();
  const query = (params.get("q") ?? "").trim();
  const page = Math.max(1, Number(params.get("page")) || 1);

  const { user } = useAuth();
  const search = useSearch(query, page);
  const lists = useMyLists(Boolean(user));
  const [kind, setKind] = useState<Kind>("ALL");
  const [opening, setOpening] = useState<string | null>(null);

  const here = `${pathname}?${params.toString()}`;
  const loginHref = `/login?next=${encodeURIComponent(here)}`;
  const onLists = listsByTmdbKey(lists.data);

  function goToPage(next: number) {
    router.push(`/search?q=${encodeURIComponent(query)}&page=${next}`);
  }

  /** A search hit only exists on TMDB. Opening it saves it to CineStack first so it has a page. */
  async function open(result: SearchResult) {
    if (!result.tmdbId) return;
    if (!user) {
      router.push(loginHref);
      return;
    }
    const key = tmdbKey(result.mediaType, result.tmdbId);
    setOpening(key);
    try {
      const title = await importTitle(result.tmdbId, result.mediaType);
      router.push(`/titles/${title.id}`);
    } catch (error) {
      notifyError(error, `Couldn't open ${result.title}. Try again.`);
      setOpening(null);
    }
  }

  if (!query) {
    return (
      <EmptyState
        icon={<Search size={28} />}
        title="Search movies and TV"
        description="Type a title in the search box at the top of the page."
        py="xl"
      />
    );
  }

  const results = search.data?.results ?? [];
  const totalPages = search.data?.totalPages ?? 1;
  const shown = results.filter(result => kind === "ALL" || result.mediaType === kind);

  return (
    <Stack gap="lg">
      <Group justify="space-between" align="flex-end">
        <div>
          <Title order={1}>Results for "{query}"</Title>
          {search.isSuccess ? (
            <Text c="dimmed" size="sm">
              {totalPages > 1 ? `Page ${page} of ${totalPages} · ` : ""}
              {titleCount(shown.length)}
            </Text>
          ) : null}
        </div>
        <SegmentedControl
          value={kind}
          onChange={value => setKind(value as Kind)}
          data={[
            { value: "ALL", label: "All" },
            { value: "MOVIE", label: "Movies" },
            { value: "TV", label: "TV" }
          ]}
        />
      </Group>

      {search.isPending ? (
        <TitleGridSkeleton />
      ) : search.isError ? (
        <LoadError error={search.error} onRetry={() => search.refetch()} />
      ) : shown.length === 0 ? (
        <EmptyState
          title={page > 1 ? "No more results" : results.length > 0 ? "Nothing of that kind" : "No titles found"}
          description={
            page > 1
              ? "That was the last page."
              : results.length > 0
                ? "Switch the filter back to All to see every result."
                : "Check the spelling, or try a shorter title."
          }
          py="xl"
        />
      ) : (
        <TitleGrid>
          {shown.map(result => {
            const key = tmdbKey(result.mediaType, result.tmdbId);
            const containing = key ? onLists.get(key) ?? [] : [];
            return (
              <TitleCard
                key={key ?? result.title}
                title={result.title}
                posterPath={result.posterPath}
                onClick={result.tmdbId ? () => open(result) : undefined}
                busy={opening !== null && opening === key}
                meta={
                  <>
                    {year(result.releaseDate) ? <span>{year(result.releaseDate)}</span> : null}
                    <KindBadge mediaType={result.mediaType} />
                  </>
                }
                action={
                  !result.tmdbId ? null : !user ? (
                    <ActionIcon
                      radius="xl"
                      size="lg"
                      aria-label={`Log in to add ${result.title} to a list`}
                      onClick={() => router.push(loginHref)}
                    >
                      <Plus size={18} />
                    </ActionIcon>
                  ) : (
                    <AddToListMenu
                      titleName={result.title}
                      lists={lists.data ?? []}
                      containingListIds={containing}
                      resolveTitleId={async () => (await importTitle(result.tmdbId!, result.mediaType)).id}
                    >
                      {({ loading }) =>
                        containing.length > 0 ? (
                          <ActionIcon
                            radius="xl"
                            size="lg"
                            variant="default"
                            loading={loading}
                            aria-label={`${result.title} is on a list. Add to another list`}
                          >
                            <Check size={16} color="var(--mantine-color-teal-5)" />
                          </ActionIcon>
                        ) : (
                          <ActionIcon radius="xl" size="lg" loading={loading} aria-label={`Add ${result.title} to a list`}>
                            <Plus size={18} />
                          </ActionIcon>
                        )
                      }
                    </AddToListMenu>
                  )
                }
              />
            );
          })}
        </TitleGrid>
      )}

      {search.isSuccess && (page > 1 || totalPages > 1) ? (
        <Group justify="center">
          <Button variant="default" disabled={page <= 1} onClick={() => goToPage(page - 1)}>
            Previous
          </Button>
          <Button variant="default" disabled={page >= totalPages} loading={search.isFetching} onClick={() => goToPage(page + 1)}>
            Next
          </Button>
        </Group>
      ) : null}
    </Stack>
  );
}
