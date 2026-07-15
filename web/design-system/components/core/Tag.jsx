import React from "react";
import { injectStyle } from "../_lib/styleInjector.js";

const CSS = `
.gops-tag {
  display: inline-flex; align-items: center; gap: 5px;
  font-family: var(--font-sans); font-weight: var(--weight-medium); font-size: var(--text-xs);
  padding: 4px 9px; border-radius: var(--radius-sm);
  background: var(--surface-sunken); color: var(--text-body);
  border: 1px solid var(--border-default); white-space: nowrap; user-select: none;
  transition: background var(--dur-fast) var(--ease-out), border-color var(--dur-fast) var(--ease-out);
}
.gops-tag--brand { background: var(--sprout-50); color: var(--sprout-700); border-color: var(--sprout-200); }
.gops-tag--gold  { background: var(--gold-50);   color: var(--gold-700);   border-color: var(--gold-200); }
.gops-tag--info  { background: var(--info-50);   color: var(--info-700);   border-color: color-mix(in srgb, var(--info-500) 28%, transparent); }
.gops-tag--clickable { cursor: pointer; }
.gops-tag--clickable:hover { background: var(--slate-200); border-color: var(--border-strong); }
.gops-tag--brand.gops-tag--clickable:hover { background: var(--sprout-100); }
.gops-tag--gold.gops-tag--clickable:hover  { background: var(--gold-100); }
.gops-tag__dot { width: 7px; height: 7px; border-radius: 50%; background: currentColor; flex: none; }
.gops-tag__remove {
  width: 16px; height: 16px; border-radius: 50%;
  display: inline-flex; align-items: center; justify-content: center;
  border: none; background: none; cursor: pointer; padding: 0;
  color: inherit; opacity: 0.65; margin-right: -3px; flex: none;
  transition: opacity var(--dur-fast), background var(--dur-fast);
}
.gops-tag__remove:hover { opacity: 1; background: color-mix(in srgb, currentColor 16%, transparent); }
.gops-tag__remove svg { width: 11px; height: 11px; display: block; }
`;

/**
 * Filterable chip — subject/unit taxonomy filters, question-bank active filters,
 * selected-student groups. Unlike Badge, Tag supports an optional × remove button.
 */
export function Tag({ tone = "neutral", dot = false, children, onRemove, onClick, className = "", ...rest }) {
  injectStyle("gops-tag", CSS);
  const cls = ["gops-tag",
    tone !== "neutral" ? `gops-tag--${tone}` : "",
    (onClick || onRemove) ? "gops-tag--clickable" : "",
    className,
  ].filter(Boolean).join(" ");
  return (
    <span className={cls} onClick={onClick} role={onClick ? "button" : undefined} tabIndex={onClick ? 0 : undefined} {...rest}>
      {dot && <span className="gops-tag__dot" aria-hidden="true" />}
      {children}
      {onRemove && (
        <button className="gops-tag__remove" onClick={(e) => { e.stopPropagation(); onRemove(); }} aria-label="Quitar">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
            <path d="M18 6L6 18M6 6l12 12"/>
          </svg>
        </button>
      )}
    </span>
  );
}
