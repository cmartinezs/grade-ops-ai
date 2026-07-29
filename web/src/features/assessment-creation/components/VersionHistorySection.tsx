"use client";

import { Badge } from "@/components/ds";
import { useVersionHistorySection, type VersionHistoryRowViewModel } from "../hooks/useVersionHistorySection";
import type { AssessmentDraftVersionViewModel } from "../mappers/toAssessmentDraftBuilderPageViewModel";

interface VersionHistorySectionProps {
  versions: AssessmentDraftVersionViewModel[];
  selectedVersion: number;
  onViewVersion: (versionNumber: number) => void;
}

// Authoritative provenance from the API (origin/actorId), additive to the existing preview
// fields — see docs/99-decisions/2026-07-28-authoring-operation-contract.md § 2/5/6.
function provenanceLabel(row: VersionHistoryRowViewModel): string {
  if (row.origin === "AI_GENERATED") return "IA";
  return row.actorId ? `Editado por ${row.actorId}` : "Editado";
}

export default function VersionHistorySection({ versions, selectedVersion, onViewVersion }: VersionHistorySectionProps) {
  const view = useVersionHistorySection({ versions, selectedVersion });

  return (
    <section aria-labelledby="version-history-title">
      <h2
        id="version-history-title"
        style={{ fontFamily: "var(--font-display)", fontSize: "var(--text-xl)", color: "var(--text-strong)", margin: "0 0 16px" }}
      >
        Historial de versiones (solo lectura)
      </h2>
      <ul style={{ listStyle: "none", margin: 0, padding: 0, display: "flex", flexDirection: "column", gap: 8 }}>
        {view.rows.map((row) => (
          <li key={row.versionNumber}>
            <button
              type="button"
              onClick={() => onViewVersion(row.versionNumber)}
              aria-pressed={row.isSelected}
              style={{
                width: "100%",
                textAlign: "left",
                display: "flex",
                justifyContent: "space-between",
                gap: 12,
                padding: "10px 12px",
                borderRadius: "var(--radius-md)",
                border: `1px solid ${row.isSelected ? "var(--border-brand)" : "var(--border-subtle)"}`,
                background: row.isSelected ? "var(--surface-brand-soft)" : "var(--surface-card)",
                cursor: "pointer",
                fontSize: "var(--text-sm)",
                color: "var(--text-body)",
              }}
            >
              <span style={{ display: "flex", alignItems: "center", gap: 8, minWidth: 0 }}>
                <span style={{ fontWeight: 600, flexShrink: 0 }}>{row.previewLabel}</span>
                <Badge tone={row.origin === "AI_GENERATED" ? "info" : "neutral"}>{provenanceLabel(row)}</Badge>
              </span>
              <span style={{ color: "var(--text-subtle)", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                {row.titlePreview}
              </span>
            </button>
          </li>
        ))}
      </ul>
    </section>
  );
}
