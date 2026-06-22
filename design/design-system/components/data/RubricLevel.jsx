import React from "react";
import { injectStyle } from "../_lib/styleInjector.js";

const CSS = `
.gops-rubric {
  display: inline-flex; align-items: center; gap: 7px;
  font-family: var(--font-sans); font-weight: var(--weight-semibold); font-size: var(--text-xs);
  padding: 4px 10px 4px 8px; border-radius: var(--radius-pill);
  border: 1px solid transparent; white-space: nowrap;
}
.gops-rubric__dot { width: 8px; height: 8px; border-radius: 2px; flex: none; }
.gops-rubric--exceeds    { background: color-mix(in srgb, var(--perf-exceeds) 12%, #fff); color: var(--perf-exceeds); border-color: color-mix(in srgb, var(--perf-exceeds) 30%, transparent); }
.gops-rubric--exceeds .gops-rubric__dot { background: var(--perf-exceeds); }
.gops-rubric--meets      { background: color-mix(in srgb, var(--perf-meets) 14%, #fff); color: var(--sprout-700); border-color: color-mix(in srgb, var(--perf-meets) 36%, transparent); }
.gops-rubric--meets .gops-rubric__dot { background: var(--perf-meets); }
.gops-rubric--developing { background: var(--gold-50); color: var(--gold-700); border-color: color-mix(in srgb, var(--perf-developing) 36%, transparent); }
.gops-rubric--developing .gops-rubric__dot { background: var(--perf-developing); }
.gops-rubric--beginning  { background: var(--warning-50); color: var(--warning-700); border-color: color-mix(in srgb, var(--perf-beginning) 34%, transparent); }
.gops-rubric--beginning .gops-rubric__dot { background: var(--perf-beginning); }
.gops-rubric--notmet     { background: var(--danger-50); color: var(--danger-700); border-color: color-mix(in srgb, var(--perf-notmet) 30%, transparent); }
.gops-rubric--notmet .gops-rubric__dot { background: var(--perf-notmet); }
.gops-rubric--solid { color: #fff !important; border-color: transparent !important; }
.gops-rubric--solid.gops-rubric--exceeds { background: var(--perf-exceeds); }
.gops-rubric--solid.gops-rubric--meets { background: var(--perf-meets); }
.gops-rubric--solid.gops-rubric--developing { background: var(--perf-developing); color: var(--slate-900) !important; }
.gops-rubric--solid.gops-rubric--beginning { background: var(--perf-beginning); color: var(--slate-900) !important; }
.gops-rubric--solid.gops-rubric--notmet { background: var(--perf-notmet); }
.gops-rubric--solid .gops-rubric__dot { display: none; }
`;

const LABELS = {
  exceeds: "Logrado destacado",
  meets: "Logrado",
  developing: "En desarrollo",
  beginning: "Inicial",
  notmet: "No logrado",
};

/**
 * Rubric performance-level badge for open assessments. Maps a level key to the
 * GradeOps achievement ladder (Logrado destacado → No logrado).
 */
export function RubricLevel({ level = "meets", label, solid = false, className = "", ...rest }) {
  injectStyle("gops-rubric", CSS);
  const cls = ["gops-rubric", `gops-rubric--${level}`, solid ? "gops-rubric--solid" : "", className].filter(Boolean).join(" ");
  return (
    <span className={cls} {...rest}>
      <span className="gops-rubric__dot" aria-hidden="true" />
      {label || LABELS[level]}
    </span>
  );
}
