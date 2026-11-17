import { describe, expect, it } from "vitest";
import { listIdsContaining, listsByTmdbKey, runtime, titleCount, year } from "../lib/format";
import { aList, aTitle } from "./helpers";

describe("format helpers", () => {
  it("writes a runtime the way a film listing does", () => {
    expect(runtime(170)).toBe("2h 50m");
    expect(runtime(120)).toBe("2h");
    expect(runtime(47)).toBe("47m");
    expect(runtime(null)).toBeNull();
  });

  it("takes the year from a date and counts titles in words", () => {
    expect(year("1995-12-15")).toBe("1995");
    expect(year(undefined)).toBeNull();
    expect(titleCount(1)).toBe("1 title");
    expect(titleCount(6)).toBe("6 titles");
  });

  it("finds which lists already contain a title, keeping movies and shows apart", () => {
    const heat = aTitle({ id: "t-heat", tmdbId: 949, mediaType: "MOVIE" });
    const show = aTitle({ id: "t-show", tmdbId: 949, mediaType: "TV", title: "Some show" });
    const item = (id: string, movie: typeof heat) => ({ id, movie, status: "PLANNED" as const, addedAt: "2026-01-01T00:00:00Z" });
    const lists = [
      aList({ id: "list-a", items: [item("i1", heat)] }),
      aList({ id: "list-b", items: [item("i2", heat), item("i3", show)] }),
      aList({ id: "list-c" })
    ];

    const byKey = listsByTmdbKey(lists);
    expect(byKey.get("MOVIE:949")).toEqual(["list-a", "list-b"]);
    expect(byKey.get("TV:949")).toEqual(["list-b"]);
    expect(listIdsContaining(lists, show)).toEqual(["list-b"]);
  });
});
