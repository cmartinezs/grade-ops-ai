"use client";

import { useMemo, useState } from "react";
import {
  toAssessmentDraftBuilderPageViewModel,
  toDraftViewModel,
  type AssessmentDraftDto,
  type AssessmentDraftViewModel,
  type AssessmentDraftVersionViewModel,
} from "../mappers/toAssessmentDraftBuilderPageViewModel";
import type { DraftEditableField, DraftEditableFields } from "./useDraftEditorSection";

// No second consumer needs this yet — promote to a shared module once another
// screen needs the same shape, per 04-hooks-y-logica-de-ui.md §7.
export type RemoteData<T> =
  | { status: "loading" }
  | { status: "ready"; data: T }
  | { status: "error"; error: string };

export interface AssessmentDraftBuilderPageViewModel {
  draft: AssessmentDraftViewModel;
  versions: AssessmentDraftVersionViewModel[];
  selectedVersion: number;
  isViewingHistoricalVersion: boolean;
  aiDisclosureLabel: "generado-por-ia" | "version-actual";
  onViewVersion: (versionNumber: number) => void;
  isSaving: boolean;
  saveFieldErrors: Partial<Record<DraftEditableField, string>> | null;
  saveServerError: string | null;
  onSave: (values: DraftEditableFields) => Promise<void>;
  isRegenerating: boolean;
  regenerateFieldError: string | null;
  regenerateAgentError: string | null;
  onRegenerate: (adjustmentNotes: string) => Promise<void>;
}

// Fake dataset only — task-12 replaces this with loadAssessmentDraftBuilderPage (task-10).
// Covers: long text (v4, 500+ chars in instructions/objectives) and 4+ versions (many-versions
// edge case). The single-current-version edge case is exercised directly in VersionHistorySection's
// own tests via a one-item fixture, not through this hook's dataset.
const LONG_INSTRUCTIONS =
  "Implementa una función recursiva que calcule el n-ésimo número de la secuencia de Fibonacci. " +
  "Debe manejar casos base (n=0, n=1) explícitamente y evitar recursión no controlada para valores grandes de n. " +
  "Considera el costo computacional de tu solución: una recursión ingenua tiene complejidad exponencial; " +
  "explica en un comentario si tu solución usa memoización u otra técnica para mejorar el rendimiento. " +
  "Incluye al menos tres casos de prueba que cubran el caso base, un caso intermedio y un caso con un valor " +
  "de entrada considerablemente grande (por ejemplo, n=30), y documenta el tiempo de ejecución observado " +
  "para cada uno de ellos en un comentario al final de tu archivo de solución.";

function buildFakeVersions(): AssessmentDraftDto[] {
  return [
    {
      draftId: "fake-draft-1",
      title: "Recursividad: Fibonacci",
      context: "Evaluación práctica sobre recursividad para el curso de Estructuras de Datos.",
      instructions: "Implementa una función recursiva para calcular Fibonacci.",
      objectives: ["Comprender recursividad básica"],
      deliverables: ["Archivo .py con la función implementada"],
      constraints: ["No usar librerías externas"],
      versionNumber: 1,
    },
    {
      draftId: "fake-draft-1",
      title: "Recursividad: Fibonacci (revisado)",
      context: "Evaluación práctica sobre recursividad para el curso de Estructuras de Datos.",
      instructions: "Implementa una función recursiva para calcular Fibonacci, manejando casos base.",
      objectives: ["Comprender recursividad básica", "Identificar casos base"],
      deliverables: ["Archivo .py con la función implementada"],
      constraints: ["No usar librerías externas"],
      versionNumber: 2,
    },
    {
      draftId: "fake-draft-1",
      title: "Recursividad: Fibonacci con casos de prueba",
      context: "Evaluación práctica sobre recursividad para el curso de Estructuras de Datos.",
      instructions: "Implementa una función recursiva para calcular Fibonacci y agrega casos de prueba.",
      objectives: ["Comprender recursividad básica", "Identificar casos base", "Escribir casos de prueba"],
      deliverables: ["Archivo .py con la función implementada", "Casos de prueba"],
      constraints: ["No usar librerías externas"],
      versionNumber: 3,
    },
    {
      draftId: "fake-draft-1",
      title: "Recursividad: Fibonacci con análisis de complejidad",
      context: "Evaluación práctica sobre recursividad para el curso de Estructuras de Datos.",
      instructions: LONG_INSTRUCTIONS,
      objectives: [
        "Comprender recursividad básica",
        "Identificar casos base",
        "Escribir casos de prueba",
        "Analizar la complejidad computacional de una solución recursiva",
      ],
      deliverables: ["Archivo .py con la función implementada", "Casos de prueba", "Comentario con análisis de tiempo"],
      constraints: ["No usar librerías externas", "Documentar el tiempo de ejecución observado"],
      versionNumber: 4,
    },
  ];
}

export function useAssessmentDraftBuilderPage(_assessmentId: string): RemoteData<AssessmentDraftBuilderPageViewModel> {
  const [versions, setVersions] = useState<AssessmentDraftDto[]>(buildFakeVersions);
  const [selectedVersion, setSelectedVersion] = useState<number>(() => Math.max(...versions.map((v) => v.versionNumber)));
  const [aiDisclosureLabel, setAiDisclosureLabel] = useState<"generado-por-ia" | "version-actual">("version-actual");

  const [isSaving, setIsSaving] = useState(false);
  const [saveFieldErrors, setSaveFieldErrors] = useState<Partial<Record<DraftEditableField, string>> | null>(null);
  const [saveServerError, setSaveServerError] = useState<string | null>(null);

  const [isRegenerating, setIsRegenerating] = useState(false);
  const [regenerateFieldError, setRegenerateFieldError] = useState<string | null>(null);
  const [regenerateAgentError, setRegenerateAgentError] = useState<string | null>(null);

  const currentVersionNumber = useMemo(() => Math.max(...versions.map((v) => v.versionNumber)), [versions]);

  const { versions: versionViewModels } = useMemo(() => {
    const current = versions.find((v) => v.versionNumber === currentVersionNumber)!;
    return toAssessmentDraftBuilderPageViewModel({ draft: current, versions });
  }, [versions, currentVersionNumber]);

  const selectedDto = versions.find((v) => v.versionNumber === selectedVersion) ?? versions[0];
  const draft = toDraftViewModel(selectedDto);

  function onViewVersion(versionNumber: number) {
    setSelectedVersion(versionNumber);
  }

  async function onSave(values: DraftEditableFields) {
    if (selectedVersion !== currentVersionNumber) return;
    setSaveFieldErrors(null);
    setSaveServerError(null);
    setIsSaving(true);
    await new Promise((resolve) => setTimeout(resolve, 0));
    setVersions((prev) => prev.map((v) => (v.versionNumber === currentVersionNumber ? { ...v, ...values } : v)));
    setAiDisclosureLabel("version-actual");
    setIsSaving(false);
  }

  async function onRegenerate(adjustmentNotes: string) {
    setRegenerateFieldError(null);
    setRegenerateAgentError(null);
    setIsRegenerating(true);
    await new Promise((resolve) => setTimeout(resolve, 0));
    const current = versions.find((v) => v.versionNumber === currentVersionNumber)!;
    const newVersionNumber = currentVersionNumber + 1;
    setVersions((prev) => [
      ...prev,
      {
        ...current,
        versionNumber: newVersionNumber,
        instructions: `${current.instructions}\n\n(Ajustado según notas del docente: ${adjustmentNotes})`,
      },
    ]);
    setSelectedVersion(newVersionNumber);
    setAiDisclosureLabel("generado-por-ia");
    setIsRegenerating(false);
  }

  return {
    status: "ready",
    data: {
      draft,
      versions: versionViewModels,
      selectedVersion,
      isViewingHistoricalVersion: selectedVersion !== currentVersionNumber,
      aiDisclosureLabel,
      onViewVersion,
      isSaving,
      saveFieldErrors,
      saveServerError,
      onSave,
      isRegenerating,
      regenerateFieldError,
      regenerateAgentError,
      onRegenerate,
    },
  };
}
