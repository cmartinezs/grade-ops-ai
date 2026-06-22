"use client";

import { useState } from "react";
import Badge from "@/components/ds/Badge";
import LucideIcon from "@/components/ds/LucideIcon";
import type { BadgeTone } from "@/components/ds/Badge";
import type { AssessmentSummaryDto, AssessmentStatus } from "@/types/assessment";

interface AssessmentRowProps {
  assessment: AssessmentSummaryDto;
  onClick?: () => void;
}

const STATUS_MAP: Record<AssessmentStatus, { tone: BadgeTone; dot: boolean; label: string }> = {
  DRAFT:   { tone: "neutral", dot: false, label: "Borrador"    },
  OPEN:    { tone: "info",    dot: true,  label: "En curso"    },
  GRADING: { tone: "warning", dot: true,  label: "En revisión" },
  CLOSED:  { tone: "success", dot: true,  label: "Cerrada"     },
};

export default function AssessmentRow({ assessment, onClick }: AssessmentRowProps) {
  const [hovered, setHovered] = useState(false);
  const statusConfig = STATUS_MAP[assessment.status] ?? STATUS_MAP.DRAFT;

  return (
    <div
      onClick={onClick}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      style={{
        display: "grid",
        gridTemplateColumns: "1fr auto",
        gap: 12,
        alignItems: "center",
        padding: "13px 20px",
        borderBottom: "1px solid var(--border-subtle)",
        background: hovered ? "var(--surface-sunken)" : "transparent",
        cursor: "pointer",
        transition: "background 120ms",
      }}
    >
      {/* Left: name + meta */}
      <div style={{ minWidth: 0 }}>
        <p
          style={{
            fontFamily: "var(--font-sans)",
            fontWeight: 600,
            fontSize: "var(--text-md)",
            color: "var(--text-strong)",
            margin: 0,
            overflow: "hidden",
            textOverflow: "ellipsis",
            whiteSpace: "nowrap",
          }}
        >
          {assessment.title}
        </p>
        <div style={{ display: "flex", alignItems: "center", gap: 8, marginTop: 4 }}>
          <span style={{ fontFamily: "var(--font-sans)", fontSize: "var(--text-sm)", color: "var(--text-muted)" }}>
            {assessment.submissionCount} entregas
          </span>
          {assessment.pendingApprovals > 0 && (
            <span style={{ fontFamily: "var(--font-sans)", fontSize: "var(--text-sm)", color: "var(--warning-600)" }}>
              · {assessment.pendingApprovals} pendientes
            </span>
          )}
        </div>
      </div>

      {/* Right: status badge + chevron */}
      <div style={{ display: "flex", alignItems: "center", gap: 10, flexShrink: 0 }}>
        <Badge tone={statusConfig.tone} dot={statusConfig.dot}>
          {statusConfig.label}
        </Badge>
        <LucideIcon name="chevron-right" size={16} color="var(--text-subtle)" />
      </div>
    </div>
  );
}
