"use client";

import { useState } from "react";
import Link from "next/link";
import { Badge, Group, Skeleton, Text } from "@mantine/core";
import type { MediaType } from "../lib/api";
import { hueFor, kindLabel, posterUrl } from "../lib/format";
import classes from "./title-card.module.css";

export function Poster({ title, path }: { title: string; path?: string | null }) {
  const [failed, setFailed] = useState(false);
  const url = failed ? null : posterUrl(path);

  return (
    <span className={classes.poster}>
      {url ? (
        <img src={url} alt={`Poster for ${title}`} loading="lazy" onError={() => setFailed(true)} />
      ) : (
        <span className={classes.placeholder} style={{ "--poster-hue": hueFor(title) } as React.CSSProperties}>
          <span>{title}</span>
        </span>
      )}
    </span>
  );
}

export function KindBadge({ mediaType }: { mediaType: MediaType }) {
  return (
    <Badge variant="outline" color="gray" size="xs" radius="sm" tt="none" fw={500}>
      {kindLabel(mediaType)}
    </Badge>
  );
}

type TitleCardProps = {
  title: string;
  posterPath?: string | null;
  /** The line under the name: year, status, rating. */
  meta?: React.ReactNode;
  /** A control pinned to the poster's top-right corner, such as the add button. */
  action?: React.ReactNode;
  href?: string;
  onClick?: () => void;
  selected?: boolean;
  busy?: boolean;
};

/** A poster with a name and one line of detail. Used in every grid in the app. */
export function TitleCard({ title, posterPath, meta, action, href, onClick, selected, busy }: TitleCardProps) {
  const poster = <Poster title={title} path={posterPath} />;
  const className = `${classes.card} ${selected ? classes.selected : ""} ${busy ? classes.busy : ""}`;

  return (
    <div className={className}>
      {href ? (
        <Link href={href} className={classes.hit} aria-label={title}>
          {poster}
        </Link>
      ) : onClick ? (
        <button
          type="button"
          onClick={onClick}
          className={classes.hit}
          aria-label={title}
          aria-pressed={selected}
          style={{ padding: 0, border: 0, background: "none", cursor: "pointer" }}
        >
          {poster}
        </button>
      ) : (
        poster
      )}
      {action ? <div className={classes.action}>{action}</div> : null}
      <Text fw={500} size="sm" truncate title={title}>
        {title}
      </Text>
      {meta ? (
        <Group gap={6} wrap="wrap" c="dimmed" fz="xs">
          {meta}
        </Group>
      ) : null}
    </div>
  );
}

export function TitleGrid({ children }: { children: React.ReactNode }) {
  return <div className={classes.grid}>{children}</div>;
}

/** Placeholder cards with the same shape as real ones, so the page does not jump when data arrives. */
export function TitleGridSkeleton({ count = 12 }: { count?: number }) {
  return (
    <TitleGrid>
      {Array.from({ length: count }, (_, index) => (
        <div className={classes.card} key={index}>
          <Skeleton className={classes.poster} />
          <Skeleton height={12} width="80%" />
          <Skeleton height={10} width="45%" />
        </div>
      ))}
    </TitleGrid>
  );
}
