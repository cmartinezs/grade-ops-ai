import React from "react";
import { injectStyle } from "../_lib/styleInjector.js";

const CSS = `
.gops-progress { width: 100%; }
.gops-progress__head { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 6px; }
.gops-progress__label { font-family: var(--font-sans); font-size: var(--text-sm); font-weight: var(--weight-medium); color: var(--text-body); }
.gops-progress__value { font-family: var(--font-mono); font-size: var(--text-xs); color: var(--text-muted); }
.gops-progress__track { height: 8px; border-radius: var(--radius-pill); background: var(--slate-200); overflow: hidden; }
.gops-progress__track--lg { height: 12px; }
.gops-progress__fill { height: 100%; border-radius: inherit; background: var(--brand); transition: width var(--dur-slow) var(--ease-out); }
.gops-progress__fill--gold { background: var(--accent); }
.gops-progress__fill--danger { background: var(--danger-500); }
.gops-progress__fill--success { background: var(--success-500); }
.gops-progress__fill--striped { background-image: linear-gradient(45deg, rgba(255,255,255,.25) 25%, transparent 25%, transparent 50%, rgba(255,255,255,.25) 50%, rgba(255,255,255,.25) 75%, transparent 75%); background-size: 16px 16px; animation: gops-stripe 0.8s linear infinite; }
@keyframes gops-stripe { to { background-position: 16px 0; } }
`;

/** Determinate progress bar — bulk grading, upload, course completion. */
export function ProgressBar({ value = 0, max = 100, label, showValue = false, tone = "brand", size = "md", striped = false, className = "", ...rest }) {
  injectStyle("gops-progress", CSS);
  const pct = Math.max(0, Math.min(100, (value / max) * 100));
  const fill = ["gops-progress__fill", tone !== "brand" ? `gops-progress__fill--${tone}` : "", striped ? "gops-progress__fill--striped" : ""].filter(Boolean).join(" ");
  return (
    <div className={["gops-progress", className].filter(Boolean).join(" ")} {...rest}>
      {(label || showValue) && (
        <div className="gops-progress__head">
          {label && <span className="gops-progress__label">{label}</span>}
          {showValue && <span className="gops-progress__value">{Math.round(pct)}%</span>}
        </div>
      )}
      <div className={["gops-progress__track", size === "lg" ? "gops-progress__track--lg" : ""].filter(Boolean).join(" ")}
           role="progressbar" aria-valuenow={Math.round(pct)} aria-valuemin={0} aria-valuemax={100}>
        <div className={fill} style={{ width: pct + "%" }} />
      </div>
    </div>
  );
}
