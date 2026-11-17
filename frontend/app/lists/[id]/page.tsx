"use client";

import { Suspense, useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { useParams, useRouter, useSearchParams } from "next/navigation";
import {
  ActionIcon,
  Button,
  CloseButton,
  Drawer,
  EmptyState,
  Group,
  Input,
  Menu,
  Rating,
  SegmentedControl,
  Select,
  Skeleton,
  Stack,
  Text,
  Textarea,
  Title
} from "@mantine/core";
import { useMediaQuery } from "@mantine/hooks";
import { modals } from "@mantine/modals";
import { Ellipsis, Film, Lock, Pencil, Trash2 } from "lucide-react";
import { ListFormModal } from "../../../components/list-form-modal";
import { LoadError } from "../../../components/page-states";
import { Stars, StatusBadge } from "../../../components/status";
import { KindBadge, TitleCard, TitleGrid, TitleGridSkeleton } from "../../../components/title-card";
import { ApiError, type Watchlist, type WatchlistItem } from "../../../lib/api";
import { useAuth } from "../../../lib/auth";
import { runtime, titleCount, year } from "../../../lib/format";
import { notifyError, notifySuccess } from "../../../lib/notify";
import { useDeleteList, useList, useRemoveItem, useUpdateItem, useUpdateList, type ItemInput, type ListInput } from "../../../lib/queries";
import { STATUS, STATUS_ORDER, type WatchStatus } from "../../../lib/theme";
import classes from "./list.module.css";

type Filter = "ALL" | WatchStatus;
type Sort = "added" | "title" | "year" | "rating";

const SORTS: { value: Sort; label: string }[] = [
  { value: "added", label: "Recently added" },
  { value: "title", label: "Title A to Z" },
  { value: "year", label: "Newest release" },
  { value: "rating", label: "My rating" }
];

export default function ListPage() {
  return (
    <Suspense fallback={<ListSkeleton />}>
      <ListView />
    </Suspense>
  );
}

function ListView() {
  // In the App Router the route's [id] comes from this hook in a client component.
  const { id } = useParams<{ id: string }>();
  const params = useSearchParams();
  const router = useRouter();
  const { user, ready } = useAuth();
  const list = useList(id);
  const updateList = useUpdateList(id);
  const deleteList = useDeleteList();

  const [filter, setFilter] = useState<Filter>("ALL");
  const [sort, setSort] = useState<Sort>("added");
  const [selectedId, setSelectedId] = useState<string | null>(params.get("item"));
  const [editing, setEditing] = useState(false);
  const wide = useMediaQuery("(min-width: 62em)");

  const items = useMemo(() => sortItems(list.data?.items ?? [], sort), [list.data, sort]);

  if (list.isPending || !ready) return <ListSkeleton />;

  if (list.isError) {
    const status = list.error instanceof ApiError ? list.error.status : 0;
    if (status === 403) {
      return (
        <EmptyState
          icon={<Lock size={28} />}
          title="This list is private"
          description={user ? "Only its owner can see it." : "If it's yours, log in to see it."}
          py="xl"
        >
          {user ? null : (
            <EmptyState.Actions>
              <Button component={Link} href={`/login?next=${encodeURIComponent(`/lists/${id}`)}`}>
                Log in
              </Button>
            </EmptyState.Actions>
          )}
        </EmptyState>
      );
    }
    if (status === 404 || status === 400) {
      return (
        <EmptyState title="List not found" description="It may have been deleted." py="xl">
          <EmptyState.Actions>
            <Button component={Link} href="/" variant="default">
              Go home
            </Button>
          </EmptyState.Actions>
        </EmptyState>
      );
    }
    return <LoadError error={list.error} onRetry={() => list.refetch()} />;
  }

  const data = list.data;
  const isOwner = user?.id === data.ownerId;
  const counts = countByStatus(data.items);
  const shown = items.filter(item => filter === "ALL" || item.status === filter);
  const selected = isOwner ? data.items.find(item => item.id === selectedId) ?? null : null;

  function saveDetails(values: ListInput) {
    updateList.mutate(values, {
      onSuccess: () => {
        setEditing(false);
        notifySuccess("List updated");
      },
      onError: error => notifyError(error, "Couldn't save the list. Try again.")
    });
  }

  function confirmDelete() {
    modals.openConfirmModal({
      title: `Delete ${data.name}?`,
      centered: true,
      children: (
        <Text size="sm">
          The list and your status, ratings and notes on its {titleCount(data.items.length)} will be removed. This can't be undone.
        </Text>
      ),
      labels: { confirm: "Delete list", cancel: "Keep it" },
      confirmProps: { color: "red" },
      onConfirm: () =>
        deleteList.mutate(id, {
          onSuccess: () => {
            notifySuccess(`Deleted ${data.name}`);
            router.push("/");
          },
          onError: error => notifyError(error, "Couldn't delete the list. Try again.")
        })
    });
  }

  const panel = selected ? (
    <ItemPanel key={selected.id} listId={id} item={selected} onClose={() => setSelectedId(null)} />
  ) : null;

  return (
    <Stack gap="lg">
      <Group justify="space-between" align="flex-end">
        <div>
          <Title order={1}>{data.name}</Title>
          <Text c="dimmed" size="sm">
            {data.visibility === "PUBLIC" ? "Public" : "Private"} · {titleCount(data.items.length)}
            {data.items.length > 0 ? ` · ${counts.WATCHED} watched` : ""}
            {isOwner ? "" : ` · by ${data.ownerDisplayName}`}
          </Text>
          {data.description ? (
            <Text size="sm" mt={6} maw={640}>
              {data.description}
            </Text>
          ) : null}
        </div>
        <Group gap="xs">
          <Select
            aria-label="Sort titles"
            data={SORTS}
            value={sort}
            onChange={value => setSort((value as Sort) ?? "added")}
            allowDeselect={false}
            w={180}
          />
          {isOwner ? (
            <Menu position="bottom-end" width={190}>
              <Menu.Target>
                <ActionIcon variant="default" size="lg" aria-label="List options">
                  <Ellipsis size={18} />
                </ActionIcon>
              </Menu.Target>
              <Menu.Dropdown>
                <Menu.Item leftSection={<Pencil size={15} />} onClick={() => setEditing(true)}>
                  Edit details
                </Menu.Item>
                <Menu.Item color="red" leftSection={<Trash2 size={15} />} onClick={confirmDelete}>
                  Delete list
                </Menu.Item>
              </Menu.Dropdown>
            </Menu>
          ) : null}
        </Group>
      </Group>

      {data.items.length === 0 ? (
        <EmptyState
          icon={<Film size={28} />}
          title="No titles yet"
          description={
            isOwner
              ? "Search for a movie or show, then use the plus button on its poster to add it here."
              : "Nothing has been added to this list."
          }
          py="xl"
        />
      ) : (
        <>
          <div className={classes.filters}>
            <SegmentedControl
              value={filter}
              onChange={value => setFilter(value as Filter)}
              data={[
                { value: "ALL", label: `All ${data.items.length}` },
                ...STATUS_ORDER.map(status => ({ value: status, label: `${STATUS[status].label} ${counts[status]}` }))
              ]}
            />
          </div>

          <div className={`${classes.split} ${panel && wide ? classes.withPanel : ""}`}>
            {shown.length === 0 ? (
              <EmptyState title={`Nothing ${STATUS[filter as WatchStatus].label.toLowerCase()}`} description="Pick another filter to see the rest of the list." py="xl" />
            ) : (
              <TitleGrid>
                {shown.map(item => (
                  <TitleCard
                    key={item.id}
                    title={item.movie.title}
                    posterPath={item.movie.posterPath}
                    selected={item.id === selected?.id}
                    href={isOwner ? undefined : `/titles/${item.movie.id}`}
                    onClick={isOwner ? () => setSelectedId(item.id === selectedId ? null : item.id) : undefined}
                    meta={
                      <>
                        <StatusBadge status={item.status} />
                        <Stars value={item.personalRating} />
                        {item.movie.mediaType === "TV" ? <KindBadge mediaType="TV" /> : null}
                      </>
                    }
                  />
                ))}
              </TitleGrid>
            )}
            {panel && wide ? <aside className={classes.panel}>{panel}</aside> : null}
          </div>
        </>
      )}

      {/* On a narrow screen the same panel slides up from the bottom instead of sitting beside the grid. */}
      <Drawer
        opened={Boolean(panel) && !wide}
        onClose={() => setSelectedId(null)}
        position="bottom"
        withCloseButton={false}
        // Size the sheet to its content instead of the full screen.
        styles={{ content: { height: "auto", maxHeight: "85dvh" } }}
      >
        {panel}
      </Drawer>

      {isOwner ? (
        <ListFormModal
          opened={editing}
          onClose={() => setEditing(false)}
          heading="Edit list"
          submitLabel="Save"
          initial={{ name: data.name, description: data.description ?? "", visibility: data.visibility }}
          submitting={updateList.isPending}
          onSubmit={saveDetails}
        />
      ) : null}
    </Stack>
  );
}

/** Status, rating and notes for one title on the list. Each change saves straight away. */
function ItemPanel({ listId, item, onClose }: { listId: string; item: WatchlistItem; onClose: () => void }) {
  const update = useUpdateItem(listId);
  const remove = useRemoveItem(listId);
  const [notes, setNotes] = useState(item.notes ?? "");

  useEffect(() => setNotes(item.notes ?? ""), [item.notes]);

  // While a save is in flight, show the value that was just chosen rather than the old one.
  const pending = update.isPending && update.variables?.itemId === item.id ? update.variables.input : null;
  const status = pending?.status ?? item.status;
  const rating = pending ? pending.personalRating : item.personalRating ?? null;

  function save(change: Partial<ItemInput>) {
    // The API replaces all three fields together, so every save sends the full set.
    const input: ItemInput = { status, personalRating: rating, notes: notes.trim() || null, ...change };
    update.mutate({ itemId: item.id, input }, { onError: error => notifyError(error, "Couldn't save. Try again.") });
  }

  function confirmRemove() {
    modals.openConfirmModal({
      title: `Remove ${item.movie.title}?`,
      centered: true,
      children: <Text size="sm">Your status, rating and notes for it on this list will be removed.</Text>,
      labels: { confirm: "Remove", cancel: "Keep it" },
      confirmProps: { color: "red" },
      onConfirm: () =>
        remove.mutate(item.id, {
          onSuccess: () => {
            notifySuccess(`Removed ${item.movie.title}`);
            onClose();
          },
          onError: error => notifyError(error, "Couldn't remove it. Try again.")
        })
    });
  }

  const facts = [year(item.movie.releaseDate), runtime(item.movie.runtimeMinutes), item.movie.genres.slice(0, 2).join(", ")].filter(Boolean);
  const notesChanged = notes.trim() !== (item.notes ?? "").trim();

  return (
    <Stack gap="md">
      <Group justify="space-between" align="flex-start" wrap="nowrap">
        <div style={{ minWidth: 0 }}>
          <Text fw={600} size="lg" lh={1.2}>
            {item.movie.title}
          </Text>
          <Text c="dimmed" size="sm">
            {facts.join(" · ")}
          </Text>
        </div>
        <CloseButton aria-label="Close" onClick={onClose} />
      </Group>

      <Input.Wrapper label="Status">
        <SegmentedControl
          fullWidth
          size="xs"
          mt={4}
          value={status}
          onChange={value => save({ status: value as WatchStatus })}
          data={STATUS_ORDER.map(value => ({ value, label: STATUS[value].label }))}
        />
      </Input.Wrapper>

      <Input.Wrapper label="Your rating">
        <Rating mt={4} size="lg" value={rating ?? 0} onChange={value => save({ personalRating: value || null })} allowClear />
      </Input.Wrapper>

      <Stack gap="xs">
        <Textarea
          label="Notes"
          placeholder="What did you think?"
          autosize
          minRows={2}
          maxRows={6}
          value={notes}
          onChange={event => setNotes(event.currentTarget.value)}
        />
        {notesChanged ? (
          <Group justify="flex-end">
            <Button size="xs" variant="default" loading={update.isPending} onClick={() => save({ notes: notes.trim() || null })}>
              Save note
            </Button>
          </Group>
        ) : null}
      </Stack>

      <Group justify="space-between">
        <Button component={Link} href={`/titles/${item.movie.id}`} variant="default">
          Open title page
        </Button>
        <Button variant="subtle" color="red" loading={remove.isPending} onClick={confirmRemove}>
          Remove
        </Button>
      </Group>
    </Stack>
  );
}

function countByStatus(items: WatchlistItem[]): Record<WatchStatus, number> {
  const counts: Record<WatchStatus, number> = { PLANNED: 0, WATCHING: 0, WATCHED: 0, DROPPED: 0 };
  for (const item of items) counts[item.status] += 1;
  return counts;
}

function sortItems(items: Watchlist["items"], sort: Sort) {
  const sorted = [...items];
  switch (sort) {
    case "title":
      return sorted.sort((a, b) => a.movie.title.localeCompare(b.movie.title));
    case "year":
      return sorted.sort((a, b) => (b.movie.releaseDate ?? "").localeCompare(a.movie.releaseDate ?? ""));
    case "rating":
      return sorted.sort((a, b) => (b.personalRating ?? 0) - (a.personalRating ?? 0));
    default:
      return sorted.sort((a, b) => b.addedAt.localeCompare(a.addedAt));
  }
}

function ListSkeleton() {
  return (
    <Stack gap="lg">
      <div>
        <Skeleton height={34} width={260} />
        <Skeleton height={12} width={180} mt={10} />
      </div>
      <TitleGridSkeleton count={8} />
    </Stack>
  );
}
