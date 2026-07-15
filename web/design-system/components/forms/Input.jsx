import React from "react";
import { injectStyle } from "../_lib/styleInjector.js";

const CSS = `
.gops-input {
  font-family: var(--font-sans);
  font-size: var(--text-md);
  color: var(--text-body);
  background: var(--surface-card);
  border: var(--border-width) solid var(--border-default);
  border-radius: var(--radius-md);
  height: 40px;
  padding: 0 var(--space-3);
  width: 100%;
  transition: border-color var(--dur-fast) var(--ease-out), box-shadow var(--dur-fast) var(--ease-out);
}
.gops-input::placeholder { color: var(--text-disabled); }
.gops-input:hover:not(:disabled) { border-color: var(--border-strong); }
.gops-input:focus { outline: none; border-color: var(--border-focus); box-shadow: var(--ring); }
.gops-input:disabled { background: var(--surface-sunken); color: var(--text-disabled); cursor: not-allowed; }
.gops-input--invalid { border-color: var(--danger-500); }
.gops-input--invalid:focus { box-shadow: 0 0 0 3px color-mix(in srgb, var(--danger-500) 30%, transparent); }
.gops-input--sm { height: 32px; font-size: var(--text-sm); }
.gops-input--lg { height: 48px; font-size: var(--text-lg); }
.gops-input--mono { font-family: var(--font-mono); letter-spacing: 0.04em; }

.gops-inputwrap { position: relative; display: flex; align-items: center; }
.gops-inputwrap .gops-input { padding-left: 38px; }
.gops-inputwrap__icon { position: absolute; left: 11px; display: flex; color: var(--text-subtle); pointer-events: none; }
.gops-inputwrap__icon svg { width: 18px; height: 18px; }
`;

/** Text input. Pass `icon` for a leading glyph, `invalid` for the error border. */
export function Input({ size = "md", invalid = false, mono = false, icon = null, className = "", ...rest }) {
  injectStyle("gops-input", CSS);
  const cls = [
    "gops-input",
    size !== "md" ? `gops-input--${size}` : "",
    invalid ? "gops-input--invalid" : "",
    mono ? "gops-input--mono" : "",
    className,
  ].filter(Boolean).join(" ");
  const input = <input className={cls} aria-invalid={invalid || undefined} {...rest} />;
  if (!icon) return input;
  return (
    <span className="gops-inputwrap">
      <span className="gops-inputwrap__icon" aria-hidden="true">{icon}</span>
      {input}
    </span>
  );
}
