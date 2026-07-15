import React from "react";
import { injectStyle } from "../_lib/styleInjector.js";

const CSS = `
.gops-select { position: relative; display: inline-flex; width: 100%; }
.gops-select select {
  appearance: none; -webkit-appearance: none;
  font-family: var(--font-sans);
  font-size: var(--text-md);
  color: var(--text-body);
  background: var(--surface-card);
  border: var(--border-width) solid var(--border-default);
  border-radius: var(--radius-md);
  height: 40px;
  padding: 0 38px 0 var(--space-3);
  width: 100%;
  cursor: pointer;
  transition: border-color var(--dur-fast) var(--ease-out), box-shadow var(--dur-fast) var(--ease-out);
}
.gops-select select:hover:not(:disabled) { border-color: var(--border-strong); }
.gops-select select:focus { outline: none; border-color: var(--border-focus); box-shadow: var(--ring); }
.gops-select select:disabled { background: var(--surface-sunken); color: var(--text-disabled); cursor: not-allowed; }
.gops-select__chevron { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); pointer-events: none; color: var(--text-subtle); }
.gops-select__chevron svg { width: 18px; height: 18px; display: block; }
.gops-select--sm select { height: 32px; font-size: var(--text-sm); }
`;

/** Native select styled to match the system, with chevron affordance. */
export function Select({ size = "md", children, className = "", ...rest }) {
  injectStyle("gops-select", CSS);
  const cls = ["gops-select", size === "sm" ? "gops-select--sm" : "", className].filter(Boolean).join(" ");
  return (
    <span className={cls}>
      <select {...rest}>{children}</select>
      <span className="gops-select__chevron" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M6 9l6 6 6-6"/></svg>
      </span>
    </span>
  );
}
