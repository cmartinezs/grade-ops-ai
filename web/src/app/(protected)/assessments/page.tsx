"use client";
import { useShellConfig } from "@/components/shell/ShellContext";
import PlaceholderPage from "@/components/shell/PlaceholderPage";
import LucideIcon from "@/components/ds/LucideIcon";

export default function AssessmentsPage() {
  useShellConfig({ title: "Evaluaciones", subtitle: "Crea y gestiona evaluaciones" });
  return (
    <PlaceholderPage
      icon={<LucideIcon name="file-pen-line" size={40} color="var(--text-subtle)" />}
      title="Tus evaluaciones"
      description="Crea evaluaciones abiertas (rúbrica), cerradas y mixtas con apoyo de IA. Gestiona entregas físicas y digitales."
      badge="Próximamente"
    />
  );
}
