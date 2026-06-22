import React from "react";
import { injectStyle } from "../_lib/styleInjector.js";

const CSS = `
.gops-field { display: flex; flex-direction: column; gap: 6px; }
.gops-field__label { font-family: var(--font-sans); font-weight: var(--weight-semibold); font-size: var(--text-sm); color: var(--text-strong); display: flex; align-items: center; gap: 6px; }
.gops-field__req { color: var(--danger-500); }
.gops-field__opt { color: var(--text-subtle); font-weight: var(--weight-regular); font-size: var(--text-xs); }
.gops-field__hint { font-size: var(--text-xs); color: var(--text-muted); }
.gops-field__error { font-size: var(--text-xs); color: var(--danger-600); font-weight: var(--weight-medium); display: flex; align-items: center; gap: 5px; }
`;

/** Form field wrapper: label + required/optional marker + hint/error text. */
export function Field({ label, htmlFor, required = false, optional = false, hint, error, children, className = "", ...rest }) {
  injectStyle("gops-field", CSS);
  return (
    <div className={["gops-field", className].filter(Boolean).join(" ")} {...rest}>
      {label && (
        <label className="gops-field__label" htmlFor={htmlFor}>
          {label}
          {required && <span className="gops-field__req" aria-hidden="true">*</span>}
          {optional && <span className="gops-field__opt">(opcional)</span>}
        </label>
      )}
      {children}
      {error
        ? <span className="gops-field__error">{error}</span>
        : hint && <span className="gops-field__hint">{hint}</span>}
    </div>
  );
}
