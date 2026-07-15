import React from "react";
import { injectStyle } from "../_lib/styleInjector.js";

const CSS = `
.gops-tip { position: relative; display: inline-flex; }
.gops-tip__pop {
  position: absolute; bottom: calc(100% + 7px); left: 50%;
  transform: translateX(-50%) translateY(4px);
  background: var(--slate-900); color: #fff;
  font-family: var(--font-sans); font-size: var(--text-xs); font-weight: var(--weight-medium);
  line-height: 1.4; max-width: 220px; text-wrap: pretty;
  padding: 5px 10px; border-radius: var(--radius-sm);
  box-shadow: var(--shadow-md); pointer-events: none;
  opacity: 0; transform: translateX(-50%) translateY(4px);
  transition: opacity var(--dur-fast) var(--ease-out), transform var(--dur-fast) var(--ease-out);
  z-index: var(--z-tooltip);
}
.gops-tip__pop::after {
  content: ""; position: absolute; top: 100%; left: 50%; transform: translateX(-50%);
  border: 4px solid transparent; border-top-color: var(--slate-900);
}
.gops-tip:hover .gops-tip__pop,
.gops-tip:focus-within .gops-tip__pop { opacity: 1; transform: translateX(-50%) translateY(0); }
.gops-tip--bottom .gops-tip__pop {
  bottom: auto; top: calc(100% + 7px);
  transform: translateX(-50%) translateY(-4px);
}
.gops-tip--bottom:hover .gops-tip__pop,
.gops-tip--bottom:focus-within .gops-tip__pop { transform: translateX(-50%) translateY(0); }
.gops-tip--bottom .gops-tip__pop::after {
  top: auto; bottom: 100%;
  border-top-color: transparent; border-bottom-color: var(--slate-900);
}
`;

/** Text tooltip on hover / focus — wrap any trigger element. */
export function Tooltip({ label, placement = "top", children, className = "" }) {
  injectStyle("gops-tip", CSS);
  const cls = ["gops-tip", placement === "bottom" ? "gops-tip--bottom" : "", className].filter(Boolean).join(" ");
  return (
    <span className={cls}>
      {children}
      <span className="gops-tip__pop" role="tooltip">{label}</span>
    </span>
  );
}
