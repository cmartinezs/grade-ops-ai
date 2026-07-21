"use client";

import { Field, Textarea, Button } from "@/components/ds";
import { useRegenerateSection } from "../hooks/useRegenerateSection";

interface RegenerateSectionProps {
  isRegenerating: boolean;
  fieldError: string | null;
  agentError: string | null;
  onRegenerate: (adjustmentNotes: string) => void;
}

export default function RegenerateSection({ isRegenerating, fieldError, agentError, onRegenerate }: RegenerateSectionProps) {
  const view = useRegenerateSection({ isRegenerating, fieldError, agentError, onRegenerate });

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

        <Button type="submit" variant="primary" loading={isRegenerating} disabled={isRegenerating}>
          {isRegenerating ? "Regenerando…" : "Regenerar con IA"}
        </Button>
      </form>
    </section>
  );
}
