import { Button } from "@/components/ds";
import { useShellConfig } from "@/components/shell/ShellContext";
import { useAssessmentDraftBuilderPage, NOT_FOUND_MESSAGE } from "@/features/assessment-creation/hooks/useAssessmentDraftBuilderPage";
import DraftEditorSection from "@/features/assessment-creation/components/DraftEditorSection";
import RegenerateSection from "@/features/assessment-creation/components/RegenerateSection";
import VersionHistorySection from "@/features/assessment-creation/components/VersionHistorySection";

// Test-only mirror of src/app/(protected)/assessments/[id]/draft/page.tsx's rendering logic.
// Exists because that file is a "use client" page whose `params` prop is a Promise unwrapped
// via React's `use()` — passing a freshly-constructed Promise.resolve() to it triggers a real,
// hard-to-control Suspense/act cycle in RTL. This wrapper takes a plain assessmentId string
// instead, exercising the exact same hook + component tree. Keep in sync with page.tsx when
// either changes; shared by page.integration.test.tsx and the Research 02 §5.6 acceptance test
// so the two don't drift from each other. Deliberately not under __tests__/ — Jest's default
// testMatch would otherwise pick this up as its own (empty) test suite.
export function DraftBuilderPageTestWrapper({ assessmentId }: { assessmentId: string }) {
  useShellConfig({
    title: "Draft de la evaluación",
    subtitle: "Revisa, edita y regenera el borrador generado por IA",
  });

  const page = useAssessmentDraftBuilderPage(assessmentId);

  if (page.status === "loading") {
    return <p role="status">Cargando…</p>;
  }

  if (page.status === "not-found") {
    return <p role="alert">{NOT_FOUND_MESSAGE}</p>;
  }

  if (page.status === "error") {
    return <p role="alert">{page.error}</p>;
  }

  if (page.status === "generation-not-started") {
    return (
      <div>
        <p role="status">Aún no se ha generado un borrador para esta evaluación.</p>
        {page.data.generateError && <p role="alert">{page.data.generateError}</p>}
        <Button variant="primary" loading={page.data.isGenerating} disabled={page.data.isGenerating} onClick={page.data.onGenerate}>
          {page.data.isGenerating ? "Generando…" : "Generar borrador"}
        </Button>
      </div>
    );
  }

  if (page.status === "generation-in-progress") {
    return <p role="status">Generando el borrador… esto puede tardar hasta un minuto.</p>;
  }

  if (page.status === "generation-failed") {
    return (
      <div>
        <p role="alert">No pudimos generar el borrador.</p>
        {page.data.retryError && <p role="alert">{page.data.retryError}</p>}
        <Button variant="primary" loading={page.data.isRetrying} disabled={page.data.isRetrying} onClick={page.data.onRetry}>
          {page.data.isRetrying ? "Reintentando…" : "Reintentar"}
        </Button>
      </div>
    );
  }

  if (page.status === "generation-indeterminate") {
    return (
      <div>
        <p role="alert">No pudimos confirmar el estado de la generación. Puedes reintentar, pero podría duplicar un intento anterior.</p>
        {page.data.retryError && <p role="alert">{page.data.retryError}</p>}
        <Button variant="primary" loading={page.data.isRetrying} disabled={page.data.isRetrying} onClick={page.data.onRetry}>
          {page.data.isRetrying ? "Reintentando…" : "Reintentar de todas formas"}
        </Button>
      </div>
    );
  }

  const { data } = page;
  const isBlockedByConflict = Boolean(data.staleConflict);

  return (
    <div style={{ maxWidth: "var(--content-max)", display: "flex", flexDirection: "column", gap: 32 }}>
      {data.staleConflict && (
        <div role="alert">
          <span>{data.staleConflict.message}</span>
          <Button variant="outline" size="sm" onClick={data.staleConflict.onReload}>
            Recargar
          </Button>
        </div>
      )}
      <DraftEditorSection
        draft={data.draft}
        isReadOnly={data.isViewingHistoricalVersion || isBlockedByConflict}
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
        disabled={isBlockedByConflict}
      />
      <VersionHistorySection versions={data.versions} selectedVersion={data.selectedVersion} onViewVersion={data.onViewVersion} />
    </div>
  );
}
