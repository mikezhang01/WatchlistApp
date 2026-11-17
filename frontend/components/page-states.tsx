import { Alert, Button, Group } from "@mantine/core";
import { CircleAlert } from "lucide-react";

/** Shown when a page's data could not be loaded, with a way to try again. */
export function LoadError({ error, onRetry }: { error: unknown; onRetry?: () => void }) {
  return (
    <Alert color="red" variant="light" icon={<CircleAlert size={18} />} title="Couldn't load this">
      <Group justify="space-between" align="center">
        {error instanceof Error ? error.message : "Something went wrong."}
        {onRetry ? (
          <Button size="xs" variant="default" onClick={onRetry}>
            Try again
          </Button>
        ) : null}
      </Group>
    </Alert>
  );
}
