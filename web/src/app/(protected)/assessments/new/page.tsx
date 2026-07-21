"use client";

import { useShellConfig } from "@/components/shell/ShellContext";
import BriefFormSection from "@/features/assessment-creation/components/BriefFormSection";
import { useIntakeAssessmentPage } from "@/features/assessment-creation/hooks/useIntakeAssessmentPage";

export default function NewAssessmentPage() {
  useShellConfig({
    title: "Nueva evaluación",
    subtitle: "Describe el objetivo de aprendizaje",
  });

  const view = useIntakeAssessmentPage();

  return (
    <div style={{ maxWidth: "var(--content-max)" }}>
      <BriefFormSection view={view} />
    </div>
  );
}
