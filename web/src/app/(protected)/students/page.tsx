"use client";
import { useShellConfig } from "@/components/shell/ShellContext";
import PlaceholderPage from "@/components/shell/PlaceholderPage";
import LucideIcon from "@/components/ds/LucideIcon";

export default function StudentsPage() {
  useShellConfig({ title: "Estudiantes", subtitle: "Gestiona tus cursos y alumnos" });
  return (
    <PlaceholderPage
      icon={<LucideIcon name="users" size={40} color="var(--text-subtle)" />}
      title="Tus estudiantes"
      description="Visualiza el rendimiento de cada alumno, identifica a quienes están en riesgo y envía retroalimentación personalizada."
      badge="Próximamente"
    />
  );
}
