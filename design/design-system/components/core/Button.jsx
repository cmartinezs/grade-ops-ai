import React from "react";
import { injectStyle } from "../_lib/styleInjector.js";

const CSS = `
.gops-btn {
  --_bg: var(--brand);
  --_fg: #fff;
  --_bd: transparent;
  display: inline-flex; align-items: center; justify-content: center;
  gap: var(--space-2);
  font-family: var(--font-sans);
  font-weight: var(--weight-semibold);
  font-size: var(--text-md);
  line-height: 1;
  white-space: nowrap;
  border: var(--border-width) solid var(--_bd);
  background: var(--_bg);
  color: var(--_fg);
  border-radius: var(--radius-md);
  padding: 0 var(--space-4);
  height: 40px;
  cursor: pointer;
  transition: background var(--dur-fast) var(--ease-out),
              box-shadow var(--dur-fast) var(--ease-out),
              transform var(--dur-fast) var(--ease-out),
              border-color var(--dur-fast) var(--ease-out);
  user-select: none;
}
.gops-btn:focus-visible { outline: none; box-shadow: var(--ring); }
.gops-btn:active:not(:disabled) { transform: translateY(1px); }
.gops-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.gops-btn--sm { height: 32px; font-size: var(--text-sm); border-radius: var(--radius-sm); padding: 0 var(--space-3); }
.gops-btn--lg { height: 48px; font-size: var(--text-lg); padding: 0 var(--space-6); border-radius: var(--radius-lg); }
.gops-btn--block { width: 100%; }

.gops-btn--primary { --_bg: var(--brand); --_fg: #fff; }
.gops-btn--primary:hover:not(:disabled) { --_bg: var(--brand-hover); box-shadow: var(--shadow-brand); }
.gops-btn--primary:active:not(:disabled) { --_bg: var(--brand-active); }

.gops-btn--accent { --_bg: var(--accent); --_fg: var(--text-on-gold); }
.gops-btn--accent:hover:not(:disabled) { --_bg: var(--accent-hover); }

.gops-btn--secondary { --_bg: var(--surface-card); --_fg: var(--text-body); --_bd: var(--border-default); }
.gops-btn--secondary:hover:not(:disabled) { --_bg: var(--surface-sunken); --_bd: var(--border-strong); }

.gops-btn--ghost { --_bg: transparent; --_fg: var(--text-body); --_bd: transparent; }
.gops-btn--ghost:hover:not(:disabled) { --_bg: var(--surface-sunken); }

.gops-btn--danger { --_bg: var(--danger-500); --_fg: #fff; }
.gops-btn--danger:hover:not(:disabled) { --_bg: var(--danger-600); }

.gops-btn--quiet-danger { --_bg: transparent; --_fg: var(--danger-600); --_bd: transparent; }
.gops-btn--quiet-danger:hover:not(:disabled) { --_bg: var(--danger-50); }

.gops-btn__spinner {
  width: 1em; height: 1em; border-radius: 50%;
  border: 2px solid currentColor; border-right-color: transparent;
  animation: gops-btn-spin 0.6s linear infinite;
}
@keyframes gops-btn-spin { to { transform: rotate(360deg); } }
`;

/**
 * Primary action button. Async-aware: pass `loading` to show an inline spinner
 * (the GradeOps async convention — disable the control + show a loader in-place).
 */
export function Button({
  variant = "primary",
  size = "md",
  block = false,
  loading = false,
  disabled = false,
  iconLeft = null,
  iconRight = null,
  children,
  className = "",
  ...rest
}) {
  injectStyle("gops-btn", CSS);
  const cls = [
    "gops-btn",
    `gops-btn--${variant}`,
    size !== "md" ? `gops-btn--${size}` : "",
    block ? "gops-btn--block" : "",
    className,
  ].filter(Boolean).join(" ");

  return (
    <button className={cls} disabled={disabled || loading} aria-busy={loading || undefined} {...rest}>
      {loading ? <span className="gops-btn__spinner" aria-hidden="true" /> : iconLeft}
      {children && <span>{children}</span>}
      {!loading && iconRight}
    </button>
  );
}
