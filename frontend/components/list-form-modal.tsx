"use client";

import { useEffect } from "react";
import { Button, Group, Input, Modal, SegmentedControl, Stack, TextInput, Textarea } from "@mantine/core";
import { useForm } from "@mantine/form";
import type { ListInput } from "../lib/queries";

type Props = {
  opened: boolean;
  onClose: () => void;
  heading: string;
  submitLabel: string;
  initial?: ListInput;
  submitting: boolean;
  onSubmit: (values: ListInput) => void;
};

const EMPTY: ListInput = { name: "", description: "", visibility: "PRIVATE" };

/** One form for both creating a list and editing its details. */
export function ListFormModal({ opened, onClose, heading, submitLabel, initial, submitting, onSubmit }: Props) {
  const form = useForm<ListInput>({
    initialValues: initial ?? EMPTY,
    validate: {
      name: value => {
        if (!value.trim()) return "Give the list a name";
        return value.length > 100 ? "Keep the name under 100 characters" : null;
      }
    }
  });

  // Start from fresh values every time the modal opens.
  useEffect(() => {
    if (opened) {
      form.setValues(initial ?? EMPTY);
      form.resetDirty(initial ?? EMPTY);
      form.clearErrors();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [opened]);

  return (
    <Modal opened={opened} onClose={onClose} title={heading} centered>
      <form onSubmit={form.onSubmit(values => onSubmit({ ...values, name: values.name.trim() }))}>
        <Stack>
          <TextInput label="Name" placeholder="Weekend" data-autofocus {...form.getInputProps("name")} />
          <Textarea label="Description" placeholder="Optional" autosize minRows={2} {...form.getInputProps("description")} />
          <Input.Wrapper label="Who can see it">
            <SegmentedControl
              fullWidth
              mt={4}
              data={[
                { value: "PRIVATE", label: "Only me" },
                { value: "PUBLIC", label: "Anyone with the link" }
              ]}
              {...form.getInputProps("visibility")}
            />
          </Input.Wrapper>
          <Group justify="flex-end">
            <Button variant="default" onClick={onClose}>
              Cancel
            </Button>
            <Button type="submit" loading={submitting}>
              {submitLabel}
            </Button>
          </Group>
        </Stack>
      </form>
    </Modal>
  );
}
