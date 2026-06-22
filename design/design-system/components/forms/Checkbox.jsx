import React from "react";
import { injectStyle } from "../_lib/styleInjector.js";

const CSS = `
.gops-check { display: inline-flex; align-items: flex-start; gap: 10px; cursor: pointer; font-family: var(--font-sans); font-size: var(--text-md); color: var(--text-body); user-select: none; }
.gops-check input { position: absolute; opacity: 0; width: 0; height: 0; }
.gops-check__box {
  flex: none; width: 20px; height: 20px; margin-top: 1px;
  border: 1.5px solid var(--border-strong);
  border-radius: var(--radius-xs);
  background: var(--surface-card);
  display: inline-flex; align-items: center; justify-content: center;
  color: #fff;
  transition: background var(--dur-fast) var(--ease-out), border-color var(--dur-fast) var(--ease-out);
}
.gops-check__box svg { width: 14px; height: 14px; opacity: 0; transform: scale(0.6); transition: opacity var(--dur-fast) var(--ease-out), transform var(--dur-fast) var(--ease-spring); }
.gops-check input:checked + .gops-check__box { background: var(--brand); border-color: var(--brand); }
.gops-check input:checked + .gops-check__box svg { opacity: 1; transform: scale(1); }
.gops-check input:focus-visible + .gops-check__box { box-shadow: var(--ring); }
.gops-check input:disabled + .gops-check__box { background: var(--surface-sunken); border-color: var(--border-default); }
.gops-check--disabled { cursor: not-allowed; color: var(--text-disabled); }
.gops-check__text { display: flex; flex-direction: column; gap: 1px; }
.gops-check__desc { font-size: var(--text-xs); color: var(--text-muted); }
`;

/** Checkbox for multi-select answer keys, settings, and bulk row selection. */
export function Checkbox({ label, description, disabled = false, className = "", ...rest }) {
  injectStyle("gops-check", CSS);
  const cls = ["gops-check", disabled ? "gops-check--disabled" : "", className].filter(Boolean).join(" ");
  return (
    <label className={cls}>
      <input type="checkbox" disabled={disabled} {...rest} />
      <span className="gops-check__box" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"><path d="M5 12l5 5L20 7"/></svg>
      </span>
      {(label || description) && (
        <span className="gops-check__text">
          {label && <span>{label}</span>}
          {description && <span className="gops-check__desc">{description}</span>}
        </span>
      )}
    </label>
  );
}
