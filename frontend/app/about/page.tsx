"use client";

import Link from "next/link";
import { Anchor, Box, Center, Group, Paper, SimpleGrid, Stack, Text, Title } from "@mantine/core";
import { ExternalLink, Film, ShieldCheck } from "lucide-react";
import { TmdbLogo } from "../../components/tmdb-logo";

export default function AboutPage() {
  return (
    <Stack gap="lg">
      <div>
        <Title order={1}>About CineStack</Title>
        <Text c="dimmed">A non-commercial portfolio application for organizing movies and TV shows.</Text>
      </div>

      <SimpleGrid cols={{ base: 1, md: 2 }} spacing="lg">
        <Stack gap="lg">
          <Section icon={<Film size={22} />} title="The project">
            CineStack lets users create public or private watchlists, track viewing status, rate titles, and discover
            them through search. It is an educational portfolio project and does not charge users or display
            advertising.
          </Section>
          <Section icon={<ShieldCheck size={22} />} title="Data and independence">
            Account details, watchlists, ratings, and notes are CineStack user data. Movie and TV metadata retrieved
            from TMDB is cached temporarily and refreshed or removed according to the configured retention policy.
            CineStack is independently developed.
          </Section>
        </Stack>

        <Paper withBorder p="lg" bg="var(--cs-surface)">
          <Stack gap="md">
            <Center mih={80} p="md" style={{ border: "1px dashed var(--cs-border)", borderRadius: "var(--mantine-radius-md)" }}>
              <TmdbLogo />
            </Center>
            <div>
              <Title order={2} size="h3">
                TMDB attribution
              </Title>
              <Text size="sm" mt={6}>
                This application uses TMDB and the TMDB APIs but is not endorsed, certified, or otherwise approved by
                TMDB.
              </Text>
            </div>
            <Stack gap={6}>
              <ExternalAnchor href="https://www.themoviedb.org/about/logos-attribution">TMDB logos and attribution</ExternalAnchor>
              <ExternalAnchor href="https://www.themoviedb.org/api-terms-of-use">TMDB API Terms of Use</ExternalAnchor>
              <Anchor component={Link} href="/" size="sm" className="cs-accent-text">
                Return home
              </Anchor>
            </Stack>
          </Stack>
        </Paper>
      </SimpleGrid>
    </Stack>
  );
}

function Section({ icon, title, children }: { icon: React.ReactNode; title: string; children: React.ReactNode }) {
  return (
    <Paper withBorder p="lg" bg="var(--cs-surface)">
      <Group align="flex-start" wrap="nowrap" gap="md">
        <Box className="cs-accent-text" mt={2}>
          {icon}
        </Box>
        <div>
          <Title order={2} size="h3">
            {title}
          </Title>
          <Text size="sm" mt={6}>
            {children}
          </Text>
        </div>
      </Group>
    </Paper>
  );
}

function ExternalAnchor({ href, children }: { href: string; children: React.ReactNode }) {
  return (
    <Anchor href={href} target="_blank" rel="noreferrer" size="sm" className="cs-accent-text">
      <Group gap={6} component="span" display="inline-flex">
        {children} <ExternalLink size={14} />
      </Group>
    </Anchor>
  );
}
