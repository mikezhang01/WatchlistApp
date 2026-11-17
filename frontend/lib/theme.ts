import { createTheme, type CSSVariablesResolver, type MantineColorsTuple } from "@mantine/core";

/**
 * The CineStack look, in one place. See docs/design-direction.html for the mockups.
 *
 * - Dark by default: posters carry the colour, the interface stays quiet.
 * - One accent ("marquee amber") for the main action, the add control and star ratings.
 * - A condensed face for page titles and the wordmark, a plain sans for everything else.
 */

/** Index 5 is the accent itself (#f2b43c); lower is lighter, higher is darker. */
const amber: MantineColorsTuple = [
  "#fff8e6",
  "#ffefcc",
  "#fde09c",
  "#fbd06a",
  "#f8c14a",
  "#f2b43c",
  "#dc9f2b",
  "#b98117",
  "#8a5a00",
  "#5e3d00"
];

/**
 * Mantine's dark scale runs from text (0) to the darkest background (9).
 * 7 is the page, 6 a surface, 5 a raised surface, 4 a border, 2 dimmed text.
 */
const dark: MantineColorsTuple = [
  "#ece9e2",
  "#c9cad3",
  "#9da3b4",
  "#6b7186",
  "#303547",
  "#242836",
  "#1a1d28",
  "#12141c",
  "#0d0f15",
  "#08090d"
];

export const theme = createTheme({
  primaryColor: "amber",
  primaryShade: 5,
  // Pick dark or light text automatically so labels on amber stay readable.
  autoContrast: true,
  luminanceThreshold: 0.4,
  colors: { amber, dark },
  defaultRadius: "md",
  fontFamily: "var(--font-body), system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif",
  fontWeights: { medium: "500" },
  headings: {
    fontFamily: "var(--font-display), 'Arial Narrow', 'Helvetica Neue', Arial, sans-serif",
    fontWeight: "600",
    sizes: {
      h1: { fontSize: "2.25rem", lineHeight: "1.05" },
      h2: { fontSize: "1.75rem", lineHeight: "1.1" },
      h3: { fontSize: "1.25rem", lineHeight: "1.2" }
    }
  }
});

/** Page backgrounds and the accent-as-text colour, which differ between the two schemes. */
export const cssVariablesResolver: CSSVariablesResolver = () => ({
  variables: {},
  light: {
    "--mantine-color-body": "#f6f5f1",
    "--cs-accent-text": "#8a5a00",
    "--cs-surface": "#ffffff",
    "--cs-border": "#dcd9d2"
  },
  dark: {
    "--cs-accent-text": "#f2b43c",
    "--cs-surface": "#1a1d28",
    "--cs-border": "#303547"
  }
});

export type WatchStatus = "PLANNED" | "WATCHING" | "WATCHED" | "DROPPED";

/** Status has its own colours, separate from the accent. */
export const STATUS: Record<WatchStatus, { label: string; color: string }> = {
  PLANNED: { label: "Planned", color: "gray" },
  WATCHING: { label: "Watching", color: "blue" },
  WATCHED: { label: "Watched", color: "teal" },
  DROPPED: { label: "Dropped", color: "pink" }
};

export const STATUS_ORDER: WatchStatus[] = ["PLANNED", "WATCHING", "WATCHED", "DROPPED"];
