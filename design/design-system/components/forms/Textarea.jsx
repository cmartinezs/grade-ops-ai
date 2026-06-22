import React from "react";
import { injectStyle } from "../_lib/styleInjector.js";

const CSS = `
.gops-textarea {
  font-family: var(--font-sans);
  font-size: var(--text-md);
  color: var(--text-body);
  background: var(--surface-card);
  border: var(--border-width) solid var(--border-default);
  border-radius: var(--radius-md);
  padding: var(--space-3);
  width: 100%;
  min-height: 96px;
  line-height: var(--leading-normal);
  resize: vertical;
  transition: border-color var(--dur-fast) var(--ease-out), box-shadow var(--dur-fast) var(--ease-out);
}
.gops-textarea::placeholder { color: var(--text-disabled); }
.gops-textarea:hover:not(:disabled) { border-color: var(--border-strong); }
.gops-textarea:focus { outline: none; border-color: var(--border-focus); box-shadow: var(--ring); }
.gops-textarea:disabled { background: var(--surface-sunken); color: var(--text-disabled); cursor: not-allowed; }
.gops-textarea--invalid { border-color: var(--danger-500); }
`;

/** Multi-line text input for statements, rubric criteria, feedback notes. */
export function Textarea({ invalid = false, className = "", ...rest }) {
  injectStyle("gops-textarea", CSS);
  const cls = ["gops-textarea", invalid ? "gops-textarea--invalid" : "", className].filter(Boolean).join(" ");
  return <textarea className={cls} aria-invalid={invalid || undefined} {...rest} />;
}
