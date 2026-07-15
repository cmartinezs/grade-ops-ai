import React from "react";
import { injectStyle } from "../_lib/styleInjector.js";

const CSS = `
.gops-radio { display: inline-flex; align-items: flex-start; gap: 10px; cursor: pointer; font-family: var(--font-sans); font-size: var(--text-md); color: var(--text-body); user-select: none; }
.gops-radio input { position: absolute; opacity: 0; width: 0; height: 0; }
.gops-radio__dot {
  flex: none; width: 20px; height: 20px; margin-top: 1px;
  border: 1.5px solid var(--border-strong);
  border-radius: 50%;
  background: var(--surface-card);
  display: inline-flex; align-items: center; justify-content: center;
  transition: border-color var(--dur-fast) var(--ease-out);
}
.gops-radio__dot::after { content: ""; width: 10px; height: 10px; border-radius: 50%; background: var(--brand); transform: scale(0); transition: transform var(--dur-fast) var(--ease-spring); }
.gops-radio input:checked + .gops-radio__dot { border-color: var(--brand); }
.gops-radio input:checked + .gops-radio__dot::after { transform: scale(1); }
.gops-radio input:focus-visible + .gops-radio__dot { box-shadow: var(--ring); }
.gops-radio input:disabled + .gops-radio__dot { background: var(--surface-sunken); }
.gops-radio--disabled { cursor: not-allowed; color: var(--text-disabled); }
.gops-radio__text { display: flex; flex-direction: column; gap: 1px; }
.gops-radio__desc { font-size: var(--text-xs); color: var(--text-muted); }
`;

/** Single-choice radio for single-answer keys and exclusive options. */
export function Radio({ label, description, disabled = false, className = "", ...rest }) {
  injectStyle("gops-radio", CSS);
  const cls = ["gops-radio", disabled ? "gops-radio--disabled" : "", className].filter(Boolean).join(" ");
  return (
    <label className={cls}>
      <input type="radio" disabled={disabled} {...rest} />
      <span className="gops-radio__dot" aria-hidden="true" />
      {(label || description) && (
        <span className="gops-radio__text">
          {label && <span>{label}</span>}
          {description && <span className="gops-radio__desc">{description}</span>}
        </span>
      )}
    </label>
  );
}
