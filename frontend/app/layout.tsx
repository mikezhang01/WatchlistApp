import "@mantine/core/styles.css";
import "@mantine/notifications/styles.css";
import "./globals.css";

import type { Metadata } from "next";
import { Barlow_Condensed, DM_Sans } from "next/font/google";
import { ColorSchemeScript, Container, mantineHtmlProps } from "@mantine/core";
import { AppFooter } from "../components/app-footer";
import { AppHeader } from "../components/app-header";
import { Providers } from "./providers";

const display = Barlow_Condensed({ subsets: ["latin"], weight: ["500", "600"], variable: "--font-display" });
const body = DM_Sans({ subsets: ["latin"], variable: "--font-body" });

export const metadata: Metadata = {
  title: "CineStack",
  description: "Keep track of the movies and shows you want to watch."
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" {...mantineHtmlProps} className={`${display.variable} ${body.variable}`}>
      <head>
        {/* Sets the colour scheme before the page paints, so there is no flash of the wrong one. */}
        <ColorSchemeScript defaultColorScheme="dark" />
      </head>
      <body>
        <Providers>
          <AppHeader />
          <Container component="main" size="lg" w="100%" py="xl" style={{ flex: 1 }}>
            {children}
          </Container>
          <AppFooter />
        </Providers>
      </body>
    </html>
  );
}
