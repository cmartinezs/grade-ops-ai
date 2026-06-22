import React from "react";
import { injectStyle } from "../_lib/styleInjector.js";

const CSS = `
.gops-switch { display: inline-flex; align-items: center; gap: 10px; cursor: pointer; font-family: var(--font-sans); font-size: var(--text-md); color: var(--text-body); user-select: none; }
.gops-switch input { position: absolute; opacity: 0; width: 0; height: 0; }
.gops-switch__track {
  position: relative; flex: none;
  width: 40px; height: 24px; border-radius: var(--radius-pill);
  background: var(--slate-300);
  transition: background var(--dur-base) var(--ease-out);
}
.gops-switch__thumb {
  position: absolute; top: 3px; left: 3px;
  width: 18px; height: 18px; border-radius: 50%;
  background: #fff; box-shadow: var(--shadow-sm);
  transition: transform var(--dur-base) var(--ease-spring);
}
.gops-switch input:checked + .gops-switch__track { background: var(--brand); }
.gops-switch input:checked + .gops-switch__track .gops-switch__thumb { transform: translateX(16px); }
.gops-switch input:focus-visible + .gops-switch__track { box-shadow: var(--ring); }
.gops-switch input:disabled + .gops-switch__track { opacity: 0.5; }
.gops-switch--disabled { cursor: not-allowed; color: var(--text-disabled); }
`;

/** On/off toggle for assessment settings (late submissions, penalties, global). */
export function Switch({ label, disabled = false, className = "", ...rest }) {
  injectStyle("gops-switch", CSS);
  const cls = ["gops-switch", disabled ? "gops-switch--disabled" : "", className].filter(Boolean).join(" ");
  return (
    <label className={cls}>
      <input type="checkbox" role="switch" disabled={disabled} {...rest} />
      <span className="gops-switch__track" aria-hidden="true">
        <span className="gops-switch__thumb" />
      </span>
      {label && <span>{label}</span>}
    </label>
  );
}
