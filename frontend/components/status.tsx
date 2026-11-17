import { Badge, Rating } from "@mantine/core";
import { STATUS, type WatchStatus } from "../lib/theme";

export function StatusBadge({ status }: { status: WatchStatus }) {
  return (
    <Badge variant="dot" color={STATUS[status].color} size="sm" tt="none" fw={500}>
      {STATUS[status].label}
    </Badge>
  );
}

/** A read-only star rating. Renders nothing when the title has not been rated. */
export function Stars({ value }: { value?: number | null }) {
  if (!value) return null;
  return <Rating value={value} readOnly size="xs" aria-label={`Rated ${value} out of 5`} />;
}
