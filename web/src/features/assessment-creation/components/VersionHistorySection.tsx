"use client";

import { useVersionHistorySection } from "../hooks/useVersionHistorySection";
import type { AssessmentDraftVersionViewModel } from "../mappers/toAssessmentDraftBuilderPageViewModel";

interface VersionHistorySectionProps {
  versions: AssessmentDraftVersionViewModel[];
  selectedVersion: number;
  onViewVersion: (versionNumber: number) => void;
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
              <span style={{ fontWeight: 600 }}>{row.previewLabel}</span>
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
