"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { Badge, Button, EmptyState, Group, Skeleton, Stack, Text, Title } from "@mantine/core";
import { Plus } from "lucide-react";
import { AddToListMenu } from "../../../components/add-to-list-menu";
import { LoadError } from "../../../components/page-states";
import { Stars, StatusBadge } from "../../../components/status";
import { KindBadge, Poster } from "../../../components/title-card";
import { ApiError } from "../../../lib/api";
import { useAuth } from "../../../lib/auth";
import { backdropUrl, listIdsContaining, runtime, year } from "../../../lib/format";
import { useMyLists, useTitle } from "../../../lib/queries";
import classes from "./title.module.css";

export default function TitlePage() {
  const { id } = useParams<{ id: string }>();
  const { user, ready } = useAuth();
  const title = useTitle(id);
  const lists = useMyLists(Boolean(user));

  if (title.isPending) {
    return (
      <Stack>
        <Skeleton height={220} radius="md" />
        <Skeleton height={34} width={280} />
        <Skeleton height={80} />
      </Stack>
    );
  }

  if (title.isError) {
    const status = title.error instanceof ApiError ? title.error.status : 0;
    if (status === 404 || status === 400) {
      return (
        <EmptyState title="Title not found" description="Search for it to add it to CineStack." py="xl">
          <EmptyState.Actions>
            <Button component={Link} href="/" variant="default">
              Go home
            </Button>
          </EmptyState.Actions>
        </EmptyState>
      );
    }
    return <LoadError error={title.error} onRetry={() => title.refetch()} />;
  }

  const data = title.data;
  const backdrop = backdropUrl(data.backdropPath);
  const containing = listIdsContaining(lists.data, data);
  const onLists = (lists.data ?? []).filter(list => containing.includes(list.id));
  const facts = [year(data.releaseDate), runtime(data.runtimeMinutes)].filter(Boolean);

  return (
    <Stack gap="xl">
      <div>
        <div className={classes.backdrop} style={backdrop ? { backgroundImage: `url(${backdrop})` } : undefined} />
        <div className={classes.layout}>
          <Poster title={data.title} path={data.posterPath} />
          <Stack gap="md" className={classes.facts}>
            <div>
              <Title order={1}>{data.title}</Title>
              <Group gap={8} c="dimmed" fz="sm" mt={4}>
                {facts.map(fact => (
                  <span key={fact}>{fact}</span>
                ))}
                <KindBadge mediaType={data.mediaType} />
                {data.voteAverage ? <span>TMDB {Number(data.voteAverage).toFixed(1)}</span> : null}
              </Group>
            </div>

            {data.genres.length > 0 ? (
              <Group gap={6}>
                {data.genres.map(genre => (
                  <Badge key={genre} variant="light" color="gray" radius="xl" tt="none" fw={500}>
                    {genre}
                  </Badge>
                ))}
              </Group>
            ) : null}

            <Text maw={680}>{data.overview || "No overview available."}</Text>

            <Group>
              {!ready ? null : user ? (
                <AddToListMenu
                  titleName={data.title}
                  lists={lists.data ?? []}
                  containingListIds={containing}
                  resolveTitleId={async () => data.id}
                >
                  {({ loading }) => (
                    <Button leftSection={<Plus size={16} />} loading={loading}>
                      Add to list
                    </Button>
                  )}
                </AddToListMenu>
              ) : (
                <Button component={Link} href={`/login?next=${encodeURIComponent(`/titles/${data.id}`)}`}>
                  Log in to add to a list
                </Button>
              )}
            </Group>
          </Stack>
        </div>
      </div>

      {onLists.length > 0 ? (
        <Stack gap="xs" maw={560}>
          <Text c="dimmed" size="sm">
            On your lists
          </Text>
          {onLists.map(list => {
            const item = list.items.find(entry => entry.movie.id === data.id)!;
            return (
              <Link key={list.id} href={`/lists/${list.id}?item=${item.id}`} className={classes.onList}>
                <Text fw={500} truncate>
                  {list.name}
                </Text>
                <Group gap="xs" wrap="nowrap">
                  <StatusBadge status={item.status} />
                  <Stars value={item.personalRating} />
                </Group>
              </Link>
            );
          })}
        </Stack>
      ) : null}
    </Stack>
  );
}
