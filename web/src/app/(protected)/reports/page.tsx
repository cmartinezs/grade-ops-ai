"use client";
import { useShellConfig } from "@/components/shell/ShellContext";
import PlaceholderPage from "@/components/shell/PlaceholderPage";
import LucideIcon from "@/components/ds/LucideIcon";

export default function ReportsPage() {
  useShellConfig({ title: "Reportes", subtitle: "Análisis y métricas de tus cursos" });
  return (
    <PlaceholderPage
      icon={<LucideIcon name="bar-chart-3" size={40} color="var(--text-subtle)" />}
      title="Panel de reportes"
      description="Analiza el rendimiento de tus cursos a lo largo del tiempo. Exporta reportes para compartir con tu institución."
      badge="Próximamente"
    />
  );
}
