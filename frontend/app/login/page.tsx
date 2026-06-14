"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import { api, AuthResponse } from "../../lib/api";
import { saveAuth } from "../../lib/auth";
import { Button, Field, PageTitle, Panel } from "../../components/ui";

export default function LoginPage() {
  const [error, setError] = useState("");

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    try {
      const auth = await api<AuthResponse>("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({
          email: form.get("email"),
          password: form.get("password")
        })
      });
      saveAuth(auth);
      window.location.href = "/dashboard";
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    }
  }

  return (
    <>
      <PageTitle title="Login" subtitle="Open your movie dashboard." />
      <Panel>
        <form className="grid max-w-md gap-3" onSubmit={submit}>
          <Field name="email" type="email" placeholder="Email" required />
          <Field name="password" type="password" placeholder="Password" required />
          {error ? <p className="text-sm text-berry">{error}</p> : null}
          <Button type="submit">Login</Button>
          <Link className="text-sm text-accent" href="/register">Create an account</Link>
        </form>
      </Panel>
    </>
  );
}

