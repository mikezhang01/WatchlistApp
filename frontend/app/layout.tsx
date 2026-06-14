import "./globals.css";
import type { Metadata } from "next";
import { AppShell } from "../components/app-shell";

export const metadata: Metadata = {
  title: "CineStack",
  description: "Social movie watchlists and recommendations"
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        <AppShell>{children}</AppShell>
      </body>
    </html>
  );
}

