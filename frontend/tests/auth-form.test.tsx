import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { AuthForm } from "../components/auth-form";
import { renderWithApp, stubApi } from "./helpers";

const replace = vi.fn();
let search = "";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace, push: vi.fn() }),
  useSearchParams: () => new URLSearchParams(search),
  usePathname: () => "/login"
}));

const password = () => screen.getByLabelText("Password", { selector: "input" });

describe("AuthForm", () => {
  beforeEach(() => {
    search = "";
  });

  it("checks the sign-up fields before calling the server", async () => {
    const calls = stubApi({});
    renderWithApp(<AuthForm mode="register" />);
    const user = userEvent.setup();

    await user.type(password(), "short");
    await user.click(screen.getByRole("button", { name: "Create account" }));

    expect(await screen.findByText("Enter a name to show on your lists")).toBeInTheDocument();
    expect(screen.getByText("Enter a valid email address")).toBeInTheDocument();
    expect(screen.getByText("Use at least 8 characters")).toBeInTheDocument();
    expect(calls).toHaveLength(0);
  });

  it("logs in, stores the session and returns to the page the user came from", async () => {
    search = "next=%2Flists%2Flist-1";
    const calls = stubApi({
      "POST /api/auth/login": { body: { token: "new-token", user: { id: "user-1", email: "me@example.com", displayName: "Me", role: "USER" } } }
    });
    renderWithApp(<AuthForm mode="login" />);
    const user = userEvent.setup();

    await user.type(screen.getByLabelText("Email"), "me@example.com");
    await user.type(password(), "correct-horse");
    await user.click(screen.getByRole("button", { name: "Log in" }));

    await waitFor(() => expect(replace).toHaveBeenCalledWith("/lists/list-1"));
    expect(calls[0].body).toEqual({ email: "me@example.com", password: "correct-horse" });
    expect(localStorage.getItem("cinestack_token")).toBe("new-token");
  });

  it("ignores a return address that points outside the app", async () => {
    search = "next=https%3A%2F%2Fevil.example";
    stubApi({
      "POST /api/auth/login": { body: { token: "new-token", user: { id: "user-1", email: "me@example.com", displayName: "Me", role: "USER" } } }
    });
    renderWithApp(<AuthForm mode="login" />);
    const user = userEvent.setup();

    await user.type(screen.getByLabelText("Email"), "me@example.com");
    await user.type(password(), "correct-horse");
    await user.click(screen.getByRole("button", { name: "Log in" }));

    await waitFor(() => expect(replace).toHaveBeenCalledWith("/"));
  });

  it("shows the server's message when the password is wrong", async () => {
    stubApi({ "POST /api/auth/login": { status: 401, body: { message: "Invalid email or password" } } });
    renderWithApp(<AuthForm mode="login" />);
    const user = userEvent.setup();

    await user.type(screen.getByLabelText("Email"), "me@example.com");
    await user.type(password(), "wrong-password");
    await user.click(screen.getByRole("button", { name: "Log in" }));

    expect(await screen.findByText("Invalid email or password")).toBeInTheDocument();
    expect(replace).not.toHaveBeenCalled();
    expect(localStorage.getItem("cinestack_token")).toBeNull();
  });
});
