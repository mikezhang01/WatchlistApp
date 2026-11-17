import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { AddToListMenu } from "../components/add-to-list-menu";
import { aList, renderWithApp, signIn, stubApi } from "./helpers";

function renderMenu(resolveTitleId = vi.fn(async () => "title-1")) {
  renderWithApp(
    <AddToListMenu
      titleName="Heat"
      lists={[aList({ id: "list-a", name: "Weekend" }), aList({ id: "list-b", name: "Crime classics" })]}
      containingListIds={["list-b"]}
      resolveTitleId={resolveTitleId}
    >
      {() => <button type="button">Add</button>}
    </AddToListMenu>
  );
  return resolveTitleId;
}

describe("AddToListMenu", () => {
  it("adds the title to the chosen list", async () => {
    signIn();
    const calls = stubApi({ "POST /api/watchlists/list-a/items": { body: { id: "item-1" } } });
    const resolveTitleId = renderMenu();
    const user = userEvent.setup();

    await user.click(screen.getByRole("button", { name: "Add" }));
    await user.click(await screen.findByRole("menuitem", { name: /Weekend/ }));

    await waitFor(() => expect(calls).toHaveLength(1));
    expect(resolveTitleId).toHaveBeenCalledOnce();
    expect(calls[0].body).toEqual({ movieId: "title-1", status: "PLANNED" });
  });

  it("does not offer a list the title is already on", async () => {
    signIn();
    const calls = stubApi({});
    renderMenu();
    const user = userEvent.setup();

    await user.click(screen.getByRole("button", { name: "Add" }));
    const alreadyOn = await screen.findByRole("menuitem", { name: /Crime classics/ });

    expect(alreadyOn).toBeDisabled();
    expect(calls).toHaveLength(0);
  });

  it("can create a new list and add the title to it in one go", async () => {
    signIn();
    const calls = stubApi({
      "POST /api/watchlists": { body: { id: "list-new", name: "Heists", items: [] } },
      "POST /api/watchlists/list-new/items": { body: { id: "item-2" } }
    });
    renderMenu();
    const user = userEvent.setup();

    await user.click(screen.getByRole("button", { name: "Add" }));
    await user.click(await screen.findByRole("menuitem", { name: /New list/ }));
    await user.type(await screen.findByLabelText("Name"), "Heists");
    await user.click(screen.getByRole("button", { name: "Create and add" }));

    await waitFor(() => expect(calls).toHaveLength(2));
    expect(calls[0].body).toMatchObject({ name: "Heists", visibility: "PRIVATE" });
    expect(calls[1].path).toBe("/api/watchlists/list-new/items");
  });
});
