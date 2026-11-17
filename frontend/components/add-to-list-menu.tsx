"use client";

import { useState } from "react";
import Link from "next/link";
import { Anchor, Loader, Menu, Text } from "@mantine/core";
import { Check, Plus } from "lucide-react";
import { ApiError, type Watchlist } from "../lib/api";
import { notifyError, notifySuccess } from "../lib/notify";
import { useAddToList, useCreateList, type ListInput } from "../lib/queries";
import { ListFormModal } from "./list-form-modal";

type Props = {
  titleName: string;
  lists: Watchlist[];
  /** Ids of the lists this title is already on. */
  containingListIds: string[];
  /** Returns the CineStack id of the title, importing it from TMDB first if needed. */
  resolveTitleId: () => Promise<string>;
  /** The button that opens the menu. It is given `loading` while an add is in progress. */
  children: (state: { loading: boolean }) => React.ReactNode;
};

/** "Add to list": pick one of my lists, or make a new one, and confirm with a toast. */
export function AddToListMenu({ titleName, lists, containingListIds, resolveTitleId, children }: Props) {
  const addToList = useAddToList();
  const createList = useCreateList();
  const [busy, setBusy] = useState(false);
  const [creating, setCreating] = useState(false);

  async function add(list: Pick<Watchlist, "id" | "name">) {
    setBusy(true);
    try {
      const titleId = await resolveTitleId();
      await addToList.mutateAsync({ listId: list.id, titleId });
      notifySuccess(
        <Text size="sm">
          Added <b>{titleName}</b> to {list.name}.{" "}
          <Anchor component={Link} href={`/lists/${list.id}`} size="sm" className="cs-accent-text">
            View list
          </Anchor>
        </Text>
      );
    } catch (error) {
      if (error instanceof ApiError && error.status === 409) {
        notifyError(new Error(`${titleName} is already on ${list.name}.`));
      } else {
        notifyError(error, `Couldn't add ${titleName}. Try again.`);
      }
    } finally {
      setBusy(false);
    }
  }

  async function createAndAdd(values: ListInput) {
    try {
      const list = await createList.mutateAsync(values);
      setCreating(false);
      await add(list);
    } catch (error) {
      notifyError(error, "Couldn't create the list. Try again.");
    }
  }

  return (
    <>
      <Menu position="bottom-end" width={230} withinPortal>
        <Menu.Target>{children({ loading: busy })}</Menu.Target>
        <Menu.Dropdown>
          <Menu.Label>Add to list</Menu.Label>
          {lists.map(list => {
            const alreadyOn = containingListIds.includes(list.id);
            return (
              <Menu.Item
                key={list.id}
                disabled={alreadyOn || busy}
                onClick={() => add(list)}
                rightSection={
                  alreadyOn ? (
                    <Check size={14} aria-label="Already on this list" />
                  ) : (
                    <Text size="xs" c="dimmed">
                      {list.items.length}
                    </Text>
                  )
                }
              >
                {list.name}
              </Menu.Item>
            );
          })}
          {lists.length > 0 ? <Menu.Divider /> : null}
          <Menu.Item leftSection={busy ? <Loader size={14} /> : <Plus size={14} />} onClick={() => setCreating(true)}>
            New list
          </Menu.Item>
        </Menu.Dropdown>
      </Menu>

      <ListFormModal
        opened={creating}
        onClose={() => setCreating(false)}
        heading={`New list for ${titleName}`}
        submitLabel="Create and add"
        submitting={createList.isPending || busy}
        onSubmit={createAndAdd}
      />
    </>
  );
}
