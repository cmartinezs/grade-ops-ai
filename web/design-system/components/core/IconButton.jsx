import React from "react";
import { injectStyle } from "../_lib/styleInjector.js";

const CSS = `
.gops-iconbtn {
  display: inline-flex; align-items: center; justify-content: center;
  border: var(--border-width) solid transparent;
  background: transparent;
  color: var(--text-muted);
  border-radius: var(--radius-md);
  cursor: pointer;
  width: 40px; height: 40px;
  transition: background var(--dur-fast) var(--ease-out), color var(--dur-fast) var(--ease-out);
}
.gops-iconbtn svg { width: 20px; height: 20px; display: block; }
.gops-iconbtn:hover:not(:disabled) { background: var(--surface-sunken); color: var(--text-strong); }
.gops-iconbtn:focus-visible { outline: none; box-shadow: var(--ring); }
.gops-iconbtn:active:not(:disabled) { transform: translateY(1px); }
.gops-iconbtn:disabled { opacity: 0.45; cursor: not-allowed; }
.gops-iconbtn--sm { width: 32px; height: 32px; border-radius: var(--radius-sm); }
.gops-iconbtn--sm svg { width: 18px; height: 18px; }
.gops-iconbtn--solid { background: var(--brand); color: #fff; }
.gops-iconbtn--solid:hover:not(:disabled) { background: var(--brand-hover); color: #fff; }
.gops-iconbtn--outline { border-color: var(--border-default); color: var(--text-body); }
.gops-iconbtn--outline:hover:not(:disabled) { background: var(--surface-sunken); }
`;

/** Square icon-only button for toolbars, table rows and dense actions. */
export function IconButton({
  variant = "ghost",
  size = "md",
  label,
  children,
  className = "",
  ...rest
}) {
  injectStyle("gops-iconbtn", CSS);
  const cls = [
    "gops-iconbtn",
    variant !== "ghost" ? `gops-iconbtn--${variant}` : "",
    size === "sm" ? "gops-iconbtn--sm" : "",
    className,
  ].filter(Boolean).join(" ");
  return (
    <button className={cls} aria-label={label} title={label} {...rest}>
      {children}
    </button>
  );
}
