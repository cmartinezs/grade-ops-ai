import React from "react";
import { injectStyle } from "../_lib/styleInjector.js";

const CSS = `
.gops-avatar {
  display: inline-flex; align-items: center; justify-content: center;
  border-radius: 50%;
  background: var(--sprout-100);
  color: var(--sprout-800);
  font-family: var(--font-display);
  font-weight: var(--weight-semibold);
  overflow: hidden;
  flex: none;
  user-select: none;
  border: 2px solid var(--surface-card);
}
.gops-avatar img { width: 100%; height: 100%; object-fit: cover; }
.gops-avatar--xs { width: 24px; height: 24px; font-size: 10px; }
.gops-avatar--sm { width: 32px; height: 32px; font-size: 12px; }
.gops-avatar--md { width: 40px; height: 40px; font-size: 15px; }
.gops-avatar--lg { width: 56px; height: 56px; font-size: 20px; }
.gops-avatar--gold { background: var(--gold-100); color: var(--gold-800); }
.gops-avatar--slate { background: var(--slate-200); color: var(--slate-700); }
`;

const TONES = ["", "--gold", "--slate"];
function pickTone(name = "") {
  let h = 0;
  for (let i = 0; i < name.length; i++) h = (h * 31 + name.charCodeAt(i)) % 997;
  return TONES[h % TONES.length];
}
function initials(name = "") {
  const parts = name.trim().split(/\s+/).slice(0, 2);
  return parts.map((p) => p[0]?.toUpperCase() || "").join("") || "?";
}

/** Circular user avatar with image or auto-initials fallback. */
export function Avatar({ name = "", src = null, size = "md", className = "", ...rest }) {
  injectStyle("gops-avatar", CSS);
  const tone = src ? "" : pickTone(name);
  const cls = ["gops-avatar", `gops-avatar--${size}`, tone ? `gops-avatar${tone}` : "", className]
    .filter(Boolean).join(" ");
  return (
    <span className={cls} title={name || undefined} {...rest}>
      {src ? <img src={src} alt={name} /> : initials(name)}
    </span>
  );
}
