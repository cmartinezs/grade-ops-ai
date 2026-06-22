"use client";
import { useShellConfig } from "@/components/shell/ShellContext";
import PlaceholderPage from "@/components/shell/PlaceholderPage";
import LucideIcon from "@/components/ds/LucideIcon";

export default function BankPage() {
  useShellConfig({ title: "Banco de preguntas", subtitle: "Tus preguntas personales y el banco global" });
  return (
    <PlaceholderPage
      icon={<LucideIcon name="library" size={40} color="var(--text-subtle)" />}
      title="Banco de preguntas"
      description="Reutiliza preguntas propias o accede al banco global de la comunidad docente. Genera preguntas con IA en segundos."
      badge="Próximamente"
    />
  );
}
