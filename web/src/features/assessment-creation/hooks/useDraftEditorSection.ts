"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import type { AssessmentDraftViewModel } from "../mappers/toAssessmentDraftBuilderPageViewModel";

export type DraftEditableField =
  | "title"
  | "context"
  | "instructions"
  | "objectives"
  | "deliverables"
  | "constraints";

export interface DraftEditableFields {
  title: string;
  context: string;
  instructions: string;
  objectives: string[];
  deliverables: string[];
  constraints: string[];
}

// RHF/Textarea only manage plain strings — objectives/deliverables/constraints are
// edited as one item per line and converted to/from string[] at the form boundary,
// since no DS list-editor component exists (out of scope here, see task-09's own
// Design notes on composing Field/Textarea directly rather than a declarative list).
interface DraftEditorFormValues {
  title: string;
  context: string;
  instructions: string;
  objectivesText: string;
  deliverablesText: string;
  constraintsText: string;
}

function linesToList(text: string): string[] {
  return text
    .split("\n")
    .map((line) => line.trim())
    .filter((line) => line.length > 0);
}

function listToLines(list: string[]): string {
  return list.join("\n");
}

function toFormValues(draft: AssessmentDraftViewModel): DraftEditorFormValues {
  return {
    title: draft.title,
    context: draft.context,
    instructions: draft.instructions,
    objectivesText: listToLines(draft.objectives),
    deliverablesText: listToLines(draft.deliverables),
    constraintsText: listToLines(draft.constraints),
  };
}

const draftEditorSchema = z.object({
  title: z.string().trim().min(1, "Ingresa un título."),
  context: z.string().trim().min(1, "Ingresa el contexto."),
  instructions: z.string().trim().min(1, "Ingresa las instrucciones."),
  objectivesText: z.string().refine((v) => linesToList(v).length > 0, "Ingresa al menos un objetivo."),
  deliverablesText: z.string().refine((v) => linesToList(v).length > 0, "Ingresa al menos un entregable."),
  constraintsText: z.string().refine((v) => linesToList(v).length > 0, "Ingresa al menos una restricción."),
});

interface UseDraftEditorSectionParams {
  draft: AssessmentDraftViewModel;
  isReadOnly: boolean;
  isSaving: boolean;
  fieldErrors: Partial<Record<DraftEditableField, string>> | null;
  serverError: string | null;
  onSave: (values: DraftEditableFields) => void;
}

const FIELD_TO_FORM_NAME: Record<DraftEditableField, keyof DraftEditorFormValues> = {
  title: "title",
  context: "context",
  instructions: "instructions",
  objectives: "objectivesText",
  deliverables: "deliverablesText",
  constraints: "constraintsText",
};

export function useDraftEditorSection({
  draft,
  isReadOnly,
  isSaving,
  fieldErrors,
  serverError,
  onSave,
}: UseDraftEditorSectionParams) {
  const {
    register,
    handleSubmit,
    setError,
    clearErrors,
    formState: { errors },
  } = useForm<DraftEditorFormValues>({
    resolver: zodResolver(draftEditorSchema),
    values: toFormValues(draft),
  });

  useEffect(() => {
    if (!fieldErrors) return;
    for (const [field, message] of Object.entries(fieldErrors) as [DraftEditableField, string | undefined][]) {
      if (message) {
        setError(FIELD_TO_FORM_NAME[field], { type: "server", message });
      }
    }
  }, [fieldErrors, setError]);

  function registerField(name: keyof DraftEditorFormValues) {
    const registration = register(name);
    return {
      ...registration,
      onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const result = registration.onChange(event);
        if (errors[name]?.type === "server") {
          clearErrors(name);
        }
        return result;
      },
    };
  }

  const isDisabled = isReadOnly || isSaving;

  const handleSave = handleSubmit((values) => {
    if (isReadOnly) return;
    onSave({
      title: values.title,
      context: values.context,
      instructions: values.instructions,
      objectives: linesToList(values.objectivesText),
      deliverables: linesToList(values.deliverablesText),
      constraints: linesToList(values.constraintsText),
    });
  });

  return {
    registerField,
    handleSave,
    errors,
    isDisabled,
    isReadOnly,
    isSaving,
    serverError,
  };
}
