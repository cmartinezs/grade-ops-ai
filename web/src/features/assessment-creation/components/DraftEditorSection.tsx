"use client";

import { Field, Input, Textarea, Button } from "@/components/ds";
import { useDraftEditorSection, type DraftEditableField, type DraftEditableFields } from "../hooks/useDraftEditorSection";
import type { AssessmentDraftViewModel } from "../mappers/toAssessmentDraftBuilderPageViewModel";

interface DraftEditorSectionProps {
  draft: AssessmentDraftViewModel;
  aiDisclosureLabel: "generado-por-ia" | "version-actual";
  isReadOnly: boolean;
  isSaving: boolean;
  fieldErrors: Partial<Record<DraftEditableField, string>> | null;
  serverError: string | null;
  onSave: (values: DraftEditableFields) => void;
}

export default function DraftEditorSection({
  draft,
  aiDisclosureLabel,
  isReadOnly,
  isSaving,
  fieldErrors,
  serverError,
  onSave,
}: DraftEditorSectionProps) {
  const view = useDraftEditorSection({ draft, isReadOnly, isSaving, fieldErrors, serverError, onSave });

  return (
    <section aria-labelledby="draft-editor-title">
      <h2
        id="draft-editor-title"
        style={{ fontFamily: "var(--font-display)", fontSize: "var(--text-xl)", color: "var(--text-strong)", margin: "0 0 8px" }}
      >
        Editor de draft
      </h2>
      {!isReadOnly && (
        <p style={{ fontSize: "var(--text-sm)", color: "var(--text-subtle)", margin: "0 0 16px" }}>
          {aiDisclosureLabel === "generado-por-ia" ? "Generado por IA" : "Versión actual"} · v{draft.versionNumber}
        </p>
      )}

      {isReadOnly && (
        <p role="status" style={{ fontSize: "var(--text-sm)", color: "var(--text-subtle)", margin: "0 0 16px" }}>
          Viendo una versión anterior — de solo lectura. Selecciona la versión actual para editar.
        </p>
      )}

      {serverError && (
        <p role="alert" style={{ fontSize: "var(--text-sm)", color: "var(--danger-600)", margin: "0 0 16px" }}>
          {serverError}
        </p>
      )}

      <form onSubmit={view.handleSave} noValidate>
        <Field label="Título" htmlFor="draft-title" required error={view.errors.title?.message} style={{ marginBottom: 16 }}>
          <Input id="draft-title" error={view.errors.title?.message} disabled={view.isDisabled} {...view.registerField("title")} />
        </Field>

        <Field label="Contexto" htmlFor="draft-context" required error={view.errors.context?.message} style={{ marginBottom: 16 }}>
          <Textarea id="draft-context" error={view.errors.context?.message} disabled={view.isDisabled} {...view.registerField("context")} />
        </Field>

        <Field
          label="Instrucciones"
          htmlFor="draft-instructions"
          required
          error={view.errors.instructions?.message}
          style={{ marginBottom: 16 }}
        >
          <Textarea
            id="draft-instructions"
            error={view.errors.instructions?.message}
            disabled={view.isDisabled}
            {...view.registerField("instructions")}
          />
        </Field>

        <Field
          label="Objetivos (uno por línea)"
          htmlFor="draft-objectives"
          required
          error={view.errors.objectivesText?.message}
          style={{ marginBottom: 16 }}
        >
          <Textarea
            id="draft-objectives"
            error={view.errors.objectivesText?.message}
            disabled={view.isDisabled}
            {...view.registerField("objectivesText")}
          />
        </Field>

        <Field
          label="Entregables (uno por línea)"
          htmlFor="draft-deliverables"
          required
          error={view.errors.deliverablesText?.message}
          style={{ marginBottom: 16 }}
        >
          <Textarea
            id="draft-deliverables"
            error={view.errors.deliverablesText?.message}
            disabled={view.isDisabled}
            {...view.registerField("deliverablesText")}
          />
        </Field>

        <Field
          label="Restricciones (una por línea)"
          htmlFor="draft-constraints"
          required
          error={view.errors.constraintsText?.message}
          style={{ marginBottom: 16 }}
        >
          <Textarea
            id="draft-constraints"
            error={view.errors.constraintsText?.message}
            disabled={view.isDisabled}
            {...view.registerField("constraintsText")}
          />
        </Field>

        {!isReadOnly && (
          <Button type="submit" variant="primary" loading={isSaving} disabled={isSaving}>
            {isSaving ? "Guardando…" : "Guardar cambios"}
          </Button>
        )}
      </form>
    </section>
  );
}
