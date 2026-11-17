import { notifications } from "@mantine/notifications";

export function notifySuccess(message: React.ReactNode) {
  notifications.show({ message, color: "teal" });
}

export function notifyError(error: unknown, fallback = "Something went wrong") {
  notifications.show({ message: error instanceof Error ? error.message : fallback, color: "red" });
}
