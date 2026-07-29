"use client";

import { Field, Textarea, Button } from "@/components/ds";
import { useRegenerateSection } from "../hooks/useRegenerateSection";

interface RegenerateSectionProps {
  isRegenerating: boolean;
  fieldError: string | null;
  agentError: string | null;
  onRegenerate: (adjustmentNotes: string) => void;
  // Blocks regeneration while a stale-revision conflict is showing (LOCAL-CONTRACTS.md §
  // Conflict handling) — see the shared conflict banner rendered above both sections in page.tsx.
  disabled?: boolean;
}

export default function RegenerateSection({ isRegenerating, fieldError, agentError, onRegenerate, disabled = false }: RegenerateSectionProps) {
  const view = useRegenerateSection({ isRegenerating, fieldError, agentError, onRegenerate, disabled });

  return (
    <section aria-labelledby="regenerate-title">
      <h2
        id="regenerate-title"
        style={{ fontFamily: "var(--font-display)", fontSize: "var(--text-xl)", color: "var(--text-strong)", margin: "0 0 16px" }}
      >
        Regenerar
      </h2>

      {view.agentError && (
        <p role="alert" style={{ fontSize: "var(--text-sm)", color: "var(--danger-600)", margin: "0 0 16px" }}>
          {view.agentError}
        </p>
      )}

      <form onSubmit={view.handleRegenerate} noValidate>
        <Field
          label="Notas de ajuste"
          htmlFor="adjustment-notes"
          required
          error={view.fieldError ?? undefined}
          style={{ marginBottom: 16 }}
        >
          <Textarea
            id="adjustment-notes"
            value={view.adjustmentNotes}
            onChange={view.handleChange}
            error={view.fieldError ?? undefined}
            disabled={view.isDisabled}
          />
        </Field>

        <Button type="submit" variant="primary" loading={isRegenerating} disabled={view.isDisabled}>
          {isRegenerating ? "Regenerando…" : "Regenerar con IA"}
        </Button>
      </form>
    </section>
  );
}
