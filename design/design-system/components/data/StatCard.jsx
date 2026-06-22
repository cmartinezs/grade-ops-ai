import React from "react";
import { injectStyle } from "../_lib/styleInjector.js";

const CSS = `
.gops-stat { background: var(--surface-card); border: 1px solid var(--border-subtle); border-radius: var(--radius-lg); padding: var(--space-5); box-shadow: var(--shadow-xs); }
.gops-stat__top { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 10px; }
.gops-stat__label { font-family: var(--font-sans); font-size: var(--text-sm); font-weight: var(--weight-medium); color: var(--text-muted); }
.gops-stat__icon { width: 34px; height: 34px; border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; background: var(--sprout-50); color: var(--brand); flex: none; }
.gops-stat__icon svg { width: 19px; height: 19px; }
.gops-stat__icon--gold { background: var(--gold-50); color: var(--gold-600); }
.gops-stat__icon--info { background: var(--info-50); color: var(--info-600); }
.gops-stat__icon--danger { background: var(--danger-50); color: var(--danger-600); }
.gops-stat__value { font-family: var(--font-display); font-weight: var(--weight-bold); font-size: var(--text-3xl); color: var(--text-strong); line-height: 1; letter-spacing: var(--tracking-tight); }
.gops-stat__value small { font-size: var(--text-lg); color: var(--text-muted); font-weight: var(--weight-medium); margin-left: 4px; }
.gops-stat__delta { display: inline-flex; align-items: center; gap: 3px; font-size: var(--text-xs); font-weight: var(--weight-semibold); margin-top: 8px; }
.gops-stat__delta--up { color: var(--success-600); }
.gops-stat__delta--down { color: var(--danger-600); }
.gops-stat__delta svg { width: 13px; height: 13px; }
`;

/** KPI tile for the teacher dashboard (promedio, entregas, por corregir…). */
export function StatCard({ label, value, unit, icon, iconTone = "brand", delta, deltaDir = "up", className = "", ...rest }) {
  injectStyle("gops-stat", CSS);
  return (
    <div className={["gops-stat", className].filter(Boolean).join(" ")} {...rest}>
      <div className="gops-stat__top">
        <span className="gops-stat__label">{label}</span>
        {icon && <span className={["gops-stat__icon", iconTone !== "brand" ? `gops-stat__icon--${iconTone}` : ""].filter(Boolean).join(" ")}>{icon}</span>}
      </div>
      <div className="gops-stat__value">{value}{unit && <small>{unit}</small>}</div>
      {delta != null && (
        <div className={`gops-stat__delta gops-stat__delta--${deltaDir}`}>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            {deltaDir === "up" ? <path d="M7 17L17 7M9 7h8v8" /> : <path d="M7 7l10 10M9 17h8V9" />}
          </svg>
          {delta}
        </div>
      )}
    </div>
  );
}
