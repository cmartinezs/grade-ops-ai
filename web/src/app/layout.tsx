import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "GradeOps AI",
  description: "AI-native assessment operations platform",
  icons: { icon: "/icon.svg" },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
