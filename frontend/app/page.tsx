"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Box, Button, EmptyState, Group, Progress, SimpleGrid, Skeleton, Stack, Text, TextInput, Title } from "@mantine/core";
import { Clapperboard, ListPlus, Search, Star } from "lucide-react";
import { ListFormModal } from "../components/list-form-modal";
import { LoadError } from "../components/page-states";
import { StatusBadge } from "../components/status";
import { KindBadge, TitleCard, TitleGrid } from "../components/title-card";
import type { Watchlist } from "../lib/api";
import { useAuth } from "../lib/auth";
import { hueFor, posterUrl } from "../lib/format";
import { notifyError } from "../lib/notify";
import { useCreateList, useMyLists, type ListInput } from "../lib/queries";
import classes from "./home.module.css";

export default function HomePage() {
  const { user, ready } = useAuth();

  if (!ready) return <ListsSkeleton />;
  return user ? <Dashboard /> : <Landing />;
}

/** What a visitor sees before signing in. */
function Landing() {
  const router = useRouter();
  const [query, setQuery] = useState("");

  function submit(event: React.FormEvent) {
    event.preventDefault();
    if (query.trim()) router.push(`/search?q=${encodeURIComponent(query.trim())}`);
  }

  return (
    <Stack gap="xl">
      <Stack className={classes.hero} gap="lg">
        <Title order={1} className={classes.heroTitle}>
          Keep track of what to watch
        </Title>
        <Text c="dimmed" size="lg">
          Search movies and TV shows, collect them into lists, and mark what you've watched and how you rated it.
        </Text>
        <form onSubmit={submit}>
          <Group gap="xs" wrap="nowrap">
            <TextInput
              size="md"
              flex={1}
              aria-label="Search movies and TV"
              placeholder="Try a title, like Heat"
              leftSection={<Search size={18} />}
              value={query}
              onChange={event => setQuery(event.currentTarget.value)}
            />
            <Button type="submit" size="md" variant="default">
              Search
            </Button>
          </Group>
        </form>
        <Group justify="center">
          <Button component={Link} href="/register" size="md">
            Create account
          </Button>
          <Button component={Link} href="/login" size="md" variant="subtle" color="gray">
            Log in
          </Button>
        </Group>
      </Stack>

      <SimpleGrid cols={{ base: 1, sm: 3 }} spacing="lg">
        <Feature icon={<Search size={20} />} title="Find anything" text="Movies and TV shows, with posters and details from TMDB." />
        <Feature icon={<ListPlus size={20} />} title="Build lists" text="As many lists as you like, private or shared with a link." />
        <Feature icon={<Star size={20} />} title="Track and rate" text="Mark titles planned, watching, watched or dropped, and rate them." />
      </SimpleGrid>
    </Stack>
  );
}

function Feature({ icon, title, text }: { icon: React.ReactNode; title: string; text: string }) {
  return (
    <Stack gap={6}>
      <Box className="cs-accent-text">{icon}</Box>
      <Text fw={600}>{title}</Text>
      <Text c="dimmed" size="sm">
        {text}
      </Text>
    </Stack>
  );
}

/** The signed-in home: what is in progress, then every list. */
function Dashboard() {
  const lists = useMyLists(true);
  const createList = useCreateList();
  const router = useRouter();
  const [creating, setCreating] = useState(false);

  function create(values: ListInput) {
    createList.mutate(values, {
      onSuccess: list => {
        setCreating(false);
        router.push(`/lists/${list.id}`);
      },
      onError: error => notifyError(error, "Couldn't create the list. Try again.")
    });
  }

  if (lists.isPending) return <ListsSkeleton />;
  if (lists.isError) return <LoadError error={lists.error} onRetry={() => lists.refetch()} />;

  const watching = lists.data.flatMap(list =>
    list.items.filter(item => item.status === "WATCHING").map(item => ({ item, list }))
  );

  return (
    <Stack gap="xl">
      {watching.length > 0 ? (
        <Stack gap="md">
          <Title order={2}>Watching now</Title>
          <TitleGrid>
            {watching.map(({ item, list }) => (
              <TitleCard
                key={item.id}
                title={item.movie.title}
                posterPath={item.movie.posterPath}
                href={`/lists/${list.id}?item=${item.id}`}
                meta={
                  <>
                    <StatusBadge status={item.status} />
                    <KindBadge mediaType={item.movie.mediaType} />
                  </>
                }
              />
            ))}
          </TitleGrid>
        </Stack>
      ) : null}

      <Stack gap="md">
        <Group justify="space-between">
          <Title order={2}>Your lists</Title>
          {lists.data.length > 0 ? <Button onClick={() => setCreating(true)}>New list</Button> : null}
        </Group>

        {lists.data.length === 0 ? (
          <EmptyState
            icon={<Clapperboard size={28} />}
            title="Start your first list"
            description="A list holds the movies and shows you want to watch. Make one, then search for titles to add."
            py="xl"
          >
            <EmptyState.Actions>
              <Button onClick={() => setCreating(true)}>Create list</Button>
            </EmptyState.Actions>
          </EmptyState>
        ) : (
          <div className={classes.lists}>
            {lists.data.map(list => (
              <ListCard key={list.id} list={list} />
            ))}
            <button type="button" className={classes.newList} onClick={() => setCreating(true)}>
              Start a new list
            </button>
          </div>
        )}
      </Stack>

      <ListFormModal
        opened={creating}
        onClose={() => setCreating(false)}
        heading="New list"
        submitLabel="Create list"
        submitting={createList.isPending}
        onSubmit={create}
      />
    </Stack>
  );
}

function ListCard({ list }: { list: Watchlist }) {
  const total = list.items.length;
  const watched = list.items.filter(item => item.status === "WATCHED").length;
  const covers = list.items.slice(0, 4);

  return (
    <Link href={`/lists/${list.id}`} className={classes.list}>
      <div className={classes.collage} aria-hidden="true">
        {covers.map(item => {
          const url = posterUrl(item.movie.posterPath);
          return url ? (
            <div key={item.id}>
              <img src={url} alt="" loading="lazy" />
            </div>
          ) : (
            <div
              key={item.id}
              className={classes.swatch}
              style={{ "--poster-hue": hueFor(item.movie.title) } as React.CSSProperties}
            />
          );
        })}
      </div>
      <Stack gap={4} p="md">
        <Text fw={600} truncate>
          {list.name}
        </Text>
        <Text c="dimmed" size="sm">
          {list.visibility === "PUBLIC" ? "Public" : "Private"} ·{" "}
          {total === 0 ? "No titles yet" : `${watched} of ${total} watched`}
        </Text>
        <Progress value={total === 0 ? 0 : (watched / total) * 100} size="xs" color="teal" mt={6} aria-label="Watched" />
      </Stack>
    </Link>
  );
}

function ListsSkeleton() {
  return (
    <Stack gap="md">
      <Skeleton height={32} width={180} />
      <div className={classes.lists}>
        {Array.from({ length: 3 }, (_, index) => (
          <Skeleton key={index} height={170} radius="md" />
        ))}
      </div>
    </Stack>
  );
}
