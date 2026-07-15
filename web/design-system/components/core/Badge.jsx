import React from "react";
import { injectStyle } from "../_lib/styleInjector.js";

const CSS = `
.gops-badge {
  display: inline-flex; align-items: center; gap: var(--space-1);
  font-family: var(--font-sans);
  font-weight: var(--weight-semibold);
  font-size: var(--text-xs);
  line-height: 1;
  letter-spacing: 0.01em;
  padding: 4px 9px;
  border-radius: var(--radius-pill);
  white-space: nowrap;
  border: 1px solid transparent;
}
.gops-badge__dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }
.gops-badge--neutral { background: var(--slate-100); color: var(--slate-700); border-color: var(--slate-200); }
.gops-badge--brand   { background: var(--sprout-50); color: var(--sprout-700); border-color: var(--sprout-200); }
.gops-badge--gold    { background: var(--gold-50); color: var(--gold-700); border-color: var(--gold-200); }
.gops-badge--success { background: var(--success-50); color: var(--success-700); border-color: color-mix(in srgb, var(--success-500) 28%, transparent); }
.gops-badge--warning { background: var(--warning-50); color: var(--warning-700); border-color: color-mix(in srgb, var(--warning-500) 30%, transparent); }
.gops-badge--danger  { background: var(--danger-50); color: var(--danger-700); border-color: color-mix(in srgb, var(--danger-500) 28%, transparent); }
.gops-badge--info    { background: var(--info-50); color: var(--info-700); border-color: color-mix(in srgb, var(--info-500) 26%, transparent); }
.gops-badge--solid   { background: var(--brand); color: #fff; border-color: transparent; }
`;

/** Small status / category pill. Use `dot` for live-status semantics. */
export function Badge({ tone = "neutral", dot = false, children, className = "", ...rest }) {
  injectStyle("gops-badge", CSS);
  const cls = ["gops-badge", `gops-badge--${tone}`, className].filter(Boolean).join(" ");
  return (
    <span className={cls} {...rest}>
      {dot && <span className="gops-badge__dot" aria-hidden="true" />}
      {children}
    </span>
  );
}
