import type { Metadata } from "next";
import { Bricolage_Grotesque, Hanken_Grotesk, JetBrains_Mono } from "next/font/google";
import "./globals.css";

const bricolage = Bricolage_Grotesque({
  subsets: ["latin"],
  variable: "--font-bricolage",
  display: "swap",
});

const hanken = Hanken_Grotesk({
  subsets: ["latin"],
  variable: "--font-hanken",
  display: "swap",
  weight: ["400", "500", "600"],
});

const jetbrains = JetBrains_Mono({
  subsets: ["latin"],
  variable: "--font-jetbrains",
  display: "swap",
  weight: ["400"],
});

export const metadata: Metadata = {
  title: "GradeOps AI",
  description: "Plataforma de evaluaciones con IA para docentes",
  icons: { icon: "/icon.svg" },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html
      lang="es"
      className={`${bricolage.variable} ${hanken.variable} ${jetbrains.variable}`}
    >
      <body>{children}</body>
    </html>
  );
}
