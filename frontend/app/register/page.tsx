"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import { api, AuthResponse } from "../../lib/api";
import { saveAuth } from "../../lib/auth";
import { Button, Field, PageTitle, Panel } from "../../components/ui";

export default function RegisterPage() {
  const [error, setError] = useState("");

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    try {
      const auth = await api<AuthResponse>("/api/auth/register", {
        method: "POST",
        body: JSON.stringify({
          email: form.get("email"),
          password: form.get("password"),
          displayName: form.get("displayName")
        })
      });
      saveAuth(auth);
      window.location.href = "/dashboard";
    } catch (err) {
      setError(err instanceof Error ? err.message : "Registration failed");
    }
  }

  return (
    <>
      <PageTitle title="Create Account" subtitle="Start tracking movies across lists, ratings, and reviews." />
      <Panel>
        <form className="grid max-w-md gap-3" onSubmit={submit}>
          <Field name="displayName" placeholder="Display name" required />
          <Field name="email" type="email" placeholder="Email" required />
          <Field name="password" type="password" minLength={8} placeholder="Password" required />
          {error ? <p className="text-sm text-berry">{error}</p> : null}
          <Button type="submit">Register</Button>
          <Link className="text-sm text-accent" href="/login">Already have an account?</Link>
        </form>
      </Panel>
    </>
  );
}

