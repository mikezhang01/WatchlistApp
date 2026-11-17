"use client";

import { Suspense, useEffect, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Alert, Anchor, Button, Paper, PasswordInput, Stack, Text, TextInput, Title } from "@mantine/core";
import { useForm } from "@mantine/form";
import { api, ApiError, type AuthResponse } from "../lib/api";
import { saveAuth, useAuth } from "../lib/auth";

type Mode = "login" | "register";
type Values = { displayName: string; email: string; password: string };

const COPY = {
  login: {
    heading: "Log in",
    submit: "Log in",
    path: "/api/auth/login",
    switchText: "New to CineStack?",
    switchLabel: "Create an account",
    switchHref: "/register"
  },
  register: {
    heading: "Create your account",
    submit: "Create account",
    path: "/api/auth/register",
    switchText: "Already have an account?",
    switchLabel: "Log in",
    switchHref: "/login"
  }
} as const;

/** Login and sign-up share one form; sign-up adds the display name field. */
export function AuthForm({ mode }: { mode: Mode }) {
  return (
    <Suspense fallback={null}>
      <AuthFormInner mode={mode} />
    </Suspense>
  );
}

function AuthFormInner({ mode }: { mode: Mode }) {
  const copy = COPY[mode];
  const router = useRouter();
  const params = useSearchParams();
  const { user, ready } = useAuth();
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  // Only follow "next" when it is a path inside this app.
  const requested = params.get("next");
  const next = requested && requested.startsWith("/") && !requested.startsWith("//") ? requested : "/";
  const nextQuery = next === "/" ? "" : `?next=${encodeURIComponent(next)}`;

  const form = useForm<Values>({
    initialValues: { displayName: "", email: "", password: "" },
    validate: {
      displayName: value => (mode === "register" && !value.trim() ? "Enter a name to show on your lists" : null),
      email: value => (/^\S+@\S+\.\S+$/.test(value) ? null : "Enter a valid email address"),
      password: value => {
        if (mode === "login") return value ? null : "Enter your password";
        if (value.length < 8) return "Use at least 8 characters";
        return value.length > 72 ? "Use 72 characters or fewer" : null;
      }
    }
  });

  useEffect(() => {
    if (ready && user) router.replace(next);
  }, [ready, user, next, router]);

  async function submit(values: Values) {
    setError("");
    setSubmitting(true);
    try {
      const body =
        mode === "register"
          ? { displayName: values.displayName.trim(), email: values.email.trim(), password: values.password }
          : { email: values.email.trim(), password: values.password };
      saveAuth(await api<AuthResponse>(copy.path, { method: "POST", body: JSON.stringify(body) }));
      router.replace(next);
    } catch (failure) {
      if (failure instanceof ApiError && Object.keys(failure.fields).length > 0) {
        form.setErrors(failure.fields);
      } else {
        setError(failure instanceof Error ? failure.message : "Something went wrong. Try again.");
      }
      setSubmitting(false);
    }
  }

  return (
    <Paper withBorder p="xl" maw={420} mx="auto" mt="xl" bg="var(--cs-surface)">
      <form onSubmit={form.onSubmit(submit)} noValidate>
        <Stack>
          <Title order={1} size="h2">
            {copy.heading}
          </Title>
          {error ? (
            <Alert color="red" variant="light">
              {error}
            </Alert>
          ) : null}
          {mode === "register" ? (
            <TextInput label="Display name" placeholder="Mike" autoComplete="name" {...form.getInputProps("displayName")} />
          ) : null}
          <TextInput label="Email" type="email" placeholder="name@example.com" autoComplete="email" {...form.getInputProps("email")} />
          <PasswordInput
            label="Password"
            description={mode === "register" ? "At least 8 characters" : undefined}
            autoComplete={mode === "register" ? "new-password" : "current-password"}
            {...form.getInputProps("password")}
          />
          <Button type="submit" loading={submitting} fullWidth>
            {copy.submit}
          </Button>
          <Text size="sm" c="dimmed" ta="center">
            {copy.switchText}{" "}
            <Anchor component={Link} href={`${copy.switchHref}${nextQuery}`} size="sm" className="cs-accent-text">
              {copy.switchLabel}
            </Anchor>
          </Text>
        </Stack>
      </form>
    </Paper>
  );
}
