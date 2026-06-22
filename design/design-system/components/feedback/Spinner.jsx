import React from "react";
import { injectStyle } from "../_lib/styleInjector.js";

const CSS = `
@keyframes gops-spin { to { transform: rotate(360deg); } }
.gops-spinner {
  display: inline-block; border-radius: 50%;
  border-style: solid; border-color: currentColor;
  border-right-color: transparent !important;
  animation: gops-spin 0.6s linear infinite;
  vertical-align: -0.15em;
}
.gops-spinner--xs { width: 14px; height: 14px; border-width: 2px; }
.gops-spinner--sm { width: 18px; height: 18px; border-width: 2px; }
.gops-spinner--md { width: 24px; height: 24px; border-width: 2.5px; }
.gops-spinner--lg { width: 36px; height: 36px; border-width: 3px; }
.gops-loadingoverlay { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; gap: 10px; background: color-mix(in srgb, var(--surface-card) 70%, transparent); backdrop-filter: blur(1.5px); border-radius: inherit; color: var(--brand); font-size: var(--text-sm); font-weight: var(--weight-medium); z-index: 2; }
`;

/** Inline loading spinner — the GradeOps async indicator. Inherits text color. */
export function Spinner({ size = "sm", className = "", style, ...rest }) {
  injectStyle("gops-spinner", CSS);
  const cls = ["gops-spinner", `gops-spinner--${size}`, className].filter(Boolean).join(" ");
  return <span className={cls} role="status" aria-label="Cargando" style={style} {...rest} />;
}

/** Overlay a component with a centered spinner while an async action runs.
 *  Wrap content in a position:relative parent. */
export function LoadingOverlay({ label = "Procesando…", size = "md" }) {
  injectStyle("gops-spinner", CSS);
  return (
    <div className="gops-loadingoverlay">
      <Spinner size={size} />
      {label && <span>{label}</span>}
    </div>
  );
}
