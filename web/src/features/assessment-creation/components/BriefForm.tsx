"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { DynamicForm, Button, type FieldDefinition } from "@/components/ds";
import { briefSchema, type BriefFormValues } from "../schemas/briefSchema";

const fields: FieldDefinition[] = [
  { name: "learningGoal", label: "Objetivo de aprendizaje", control: "textarea", required: true },
  { name: "topic", label: "Tema", control: "input", required: true },
  { name: "level", label: "Nivel", control: "input", required: true },
  { name: "duration", label: "Duración", control: "input", required: true },
  { name: "language", label: "Idioma", control: "input", required: true },
];

interface BriefFormProps {
  onSubmit: (values: BriefFormValues) => void;
  isSubmitting: boolean;
  serverError: string | null;
  fieldErrors: Partial<Record<keyof BriefFormValues, string>> | null;
}

export default function BriefForm({ onSubmit, isSubmitting, serverError, fieldErrors }: BriefFormProps) {
  return (
    <DynamicForm<BriefFormValues>
      fields={fields}
      onSubmit={onSubmit}
      resolver={zodResolver(briefSchema)}
      externalErrors={fieldErrors ?? undefined}
    >
      {serverError && (
        <p role="alert" style={{ fontSize: "var(--text-sm)", color: "var(--danger-600)", margin: 0 }}>
          {serverError}
        </p>
      )}
      <Button type="submit" variant="primary" loading={isSubmitting} disabled={isSubmitting}>
        {isSubmitting ? "Creando evaluación…" : "Crear evaluación"}
      </Button>
    </DynamicForm>
  );
}
