"use client";

import { use } from "react";
import Link from "next/link";
import { useShellConfig } from "@/components/shell/ShellContext";
import { useAssessmentDraftBuilderPage, NOT_FOUND_MESSAGE } from "@/features/assessment-creation/hooks/useAssessmentDraftBuilderPage";
import DraftEditorSection from "@/features/assessment-creation/components/DraftEditorSection";
import RegenerateSection from "@/features/assessment-creation/components/RegenerateSection";
import VersionHistorySection from "@/features/assessment-creation/components/VersionHistorySection";

interface DraftBuilderPageProps {
  params: Promise<{ id: string }>;
}

export default function DraftBuilderPage({ params }: DraftBuilderPageProps) {
  const { id } = use(params);

  useShellConfig({
    title: "Draft de la evaluación",
    subtitle: "Revisa, edita y regenera el borrador generado por IA",
  });

  const page = useAssessmentDraftBuilderPage(id);

  if (page.status === "loading") {
    return <p role="status">Cargando…</p>;
  }

  if (page.status === "not-found") {
    return (
      <div style={{ maxWidth: "var(--content-max)", textAlign: "center", padding: "48px 16px" }}>
        <p role="alert" style={{ fontSize: "var(--text-lg)", color: "var(--text-strong)", margin: "0 0 16px" }}>
          {NOT_FOUND_MESSAGE}
        </p>
        <Link
          href="/assessments"
          style={{
            display: "inline-flex",
            alignItems: "center",
            justifyContent: "center",
            height: 40,
            padding: "0 16px",
            borderRadius: "var(--radius-md)",
            background: "var(--brand)",
            color: "#fff",
            fontFamily: "var(--font-sans)",
            fontWeight: 500,
            textDecoration: "none",
          }}
        >
          Volver al listado
        </Link>
      </div>
    );
  }

  if (page.status === "error") {
    return <p role="alert">{page.error}</p>;
  }

  const { data } = page;

  return (
    <div style={{ maxWidth: "var(--content-max)", display: "flex", flexDirection: "column", gap: 32 }}>
      <DraftEditorSection
        draft={data.draft}
        aiDisclosureLabel={data.aiDisclosureLabel}
        isReadOnly={data.isViewingHistoricalVersion}
        isSaving={data.isSaving}
        fieldErrors={data.saveFieldErrors}
        serverError={data.saveServerError}
        onSave={data.onSave}
      />
      <RegenerateSection
        isRegenerating={data.isRegenerating}
        fieldError={data.regenerateFieldError}
        agentError={data.regenerateAgentError}
        onRegenerate={data.onRegenerate}
      />
      <VersionHistorySection versions={data.versions} selectedVersion={data.selectedVersion} onViewVersion={data.onViewVersion} />
    </div>
  );
}
