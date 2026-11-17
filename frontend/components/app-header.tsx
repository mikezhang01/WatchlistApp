"use client";

import { Suspense, useEffect, useState } from "react";
import Link from "next/link";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import {
  ActionIcon,
  Avatar,
  Box,
  Button,
  Container,
  Group,
  Menu,
  Text,
  TextInput,
  UnstyledButton,
  useComputedColorScheme,
  useMantineColorScheme
} from "@mantine/core";
import { Info, List, LogOut, Moon, Search, Sun } from "lucide-react";
import { clearAuth, useAuth } from "../lib/auth";
import { initials } from "../lib/format";
import classes from "./app-header.module.css";

export function AppHeader() {
  const { user, ready } = useAuth();
  const router = useRouter();

  function logout() {
    clearAuth();
    router.push("/");
  }

  return (
    <Box component="header" className={classes.header}>
      <Container size="lg">
        <Group h={60} gap="md" wrap="nowrap" justify="space-between">
          <Link href="/" className={classes.mark}>
            Cine<span>Stack</span>
          </Link>

          {/* useSearchParams needs a Suspense boundary so the rest of the page can render first. */}
          <Suspense fallback={<SearchField initialQuery="" />}>
            <SearchFieldFromUrl />
          </Suspense>

          <Group gap="xs" wrap="nowrap">
            {user ? (
              <Button component={Link} href="/" variant="subtle" color="gray" visibleFrom="sm">
                My lists
              </Button>
            ) : null}
            <ColorSchemeToggle />
            {!ready ? null : user ? (
              <Menu position="bottom-end" width={220}>
                <Menu.Target>
                  <UnstyledButton aria-label="Account menu">
                    <Avatar color="amber" radius="xl" size={34}>
                      {initials(user.displayName)}
                    </Avatar>
                  </UnstyledButton>
                </Menu.Target>
                <Menu.Dropdown>
                  <Box px="sm" py={6}>
                    <Text fw={600} size="sm" truncate>
                      {user.displayName}
                    </Text>
                    <Text c="dimmed" size="xs" truncate>
                      {user.email}
                    </Text>
                  </Box>
                  <Menu.Divider />
                  <Menu.Item component={Link} href="/" leftSection={<List size={15} />}>
                    My lists
                  </Menu.Item>
                  <Menu.Item component={Link} href="/about" leftSection={<Info size={15} />}>
                    About
                  </Menu.Item>
                  <Menu.Divider />
                  <Menu.Item onClick={logout} leftSection={<LogOut size={15} />}>
                    Log out
                  </Menu.Item>
                </Menu.Dropdown>
              </Menu>
            ) : (
              <>
                <Button component={Link} href="/login" variant="subtle" color="gray" px="xs">
                  Log in
                </Button>
                <Button component={Link} href="/register" visibleFrom="xs">
                  Sign up
                </Button>
              </>
            )}
          </Group>
        </Group>
      </Container>
    </Box>
  );
}

/** On the search page the box shows the current query, so it can be edited in place. */
function SearchFieldFromUrl() {
  const pathname = usePathname();
  const params = useSearchParams();
  return <SearchField initialQuery={pathname === "/search" ? params.get("q") ?? "" : ""} />;
}

function SearchField({ initialQuery }: { initialQuery: string }) {
  const router = useRouter();
  const [value, setValue] = useState(initialQuery);
  useEffect(() => setValue(initialQuery), [initialQuery]);

  function submit(event: React.FormEvent) {
    event.preventDefault();
    const query = value.trim();
    if (query) router.push(`/search?q=${encodeURIComponent(query)}`);
  }

  return (
    <form onSubmit={submit} className={classes.search} role="search">
      <TextInput
        aria-label="Search movies and TV"
        placeholder="Search movies and TV"
        leftSection={<Search size={16} />}
        value={value}
        onChange={event => setValue(event.currentTarget.value)}
      />
    </form>
  );
}

function ColorSchemeToggle() {
  const { setColorScheme } = useMantineColorScheme();
  const current = useComputedColorScheme("dark", { getInitialValueInEffect: true });

  // Both icons are always rendered and CSS shows the right one, so the server and the
  // browser produce the same HTML whichever scheme is active.
  return (
    <ActionIcon
      variant="default"
      size="lg"
      aria-label="Switch between dark and light"
      onClick={() => setColorScheme(current === "dark" ? "light" : "dark")}
    >
      <Box component="span" lightHidden lh={0}>
        <Sun size={16} />
      </Box>
      <Box component="span" darkHidden lh={0}>
        <Moon size={16} />
      </Box>
    </ActionIcon>
  );
}
