"use client";

import Link from "next/link";
import { Anchor, Box, Container, Group, Text } from "@mantine/core";

/** TMDB's terms require this notice wherever their data is shown, so it is on every page. */
export function AppFooter() {
  return (
    <Box component="footer" style={{ borderTop: "1px solid var(--cs-border)" }}>
      <Container size="lg" py="md">
        <Group justify="space-between" gap="xs">
          <Text size="xs" c="dimmed">
            This application uses TMDB and the TMDB APIs but is not endorsed, certified, or otherwise approved by TMDB.
          </Text>
          <Anchor component={Link} href="/about" size="xs" className="cs-accent-text">
            Attribution and legal information
          </Anchor>
        </Group>
      </Container>
    </Box>
  );
}
