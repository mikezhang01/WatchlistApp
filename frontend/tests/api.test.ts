import { describe, expect, it } from "vitest";
import { api, ApiError } from "../lib/api";
import { signIn, stubApi } from "./helpers";

describe("api()", () => {
  it("sends the token when signed in and returns the JSON body", async () => {
    signIn();
    const calls = stubApi({ "GET /api/watchlists": { body: [{ id: "list-1" }] } });

    const lists = await api<{ id: string }[]>("/api/watchlists");

    expect(lists).toEqual([{ id: "list-1" }]);
    expect(calls[0].headers.Authorization).toBe("Bearer test-token");
  });

  it("sends no token for a visitor", async () => {
    const calls = stubApi({ "GET /api/movies/popular": { body: [] } });

    await api("/api/movies/popular");

    expect(calls[0].headers.Authorization).toBeUndefined();
  });

  it("signs the user out when the server rejects their token", async () => {
    signIn();
    stubApi({ "GET /api/watchlists": { status: 401, body: { message: "Log in to continue" } } });

    await expect(api("/api/watchlists")).rejects.toMatchObject({ status: 401 });

    expect(localStorage.getItem("cinestack_token")).toBeNull();
    expect(localStorage.getItem("cinestack_user")).toBeNull();
  });

  it("turns an error response into an ApiError with the server's message and field errors", async () => {
    stubApi({
      "POST /api/auth/register": { status: 400, body: { message: "Request validation failed", fields: { email: "must be an email" } } }
    });

    const error = (await api("/api/auth/register", { method: "POST", body: "{}" }).catch((e: unknown) => e)) as ApiError;

    expect(error).toBeInstanceOf(ApiError);
    expect(error.message).toBe("Request validation failed");
    expect(error.fields).toEqual({ email: "must be an email" });
  });

  it("handles an empty success response", async () => {
    signIn();
    stubApi({ "DELETE /api/watchlists/list-1": {} });

    await expect(api("/api/watchlists/list-1", { method: "DELETE" })).resolves.toBeUndefined();
  });
});
