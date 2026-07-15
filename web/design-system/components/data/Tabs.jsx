import React from "react";
import { injectStyle } from "../_lib/styleInjector.js";

const CSS = `
.gops-tabs { display: flex; gap: 4px; border-bottom: 1px solid var(--border-subtle); }
.gops-tabs--pills { border-bottom: none; gap: 6px; background: var(--surface-sunken); padding: 4px; border-radius: var(--radius-md); width: fit-content; }
.gops-tab {
  appearance: none; border: none; background: none; cursor: pointer;
  font-family: var(--font-sans); font-weight: var(--weight-semibold); font-size: var(--text-md);
  color: var(--text-muted); padding: 10px 14px; position: relative;
  display: inline-flex; align-items: center; gap: 7px;
  transition: color var(--dur-fast) var(--ease-out);
}
.gops-tab:hover { color: var(--text-strong); }
.gops-tab--active { color: var(--brand); }
.gops-tab--active::after { content: ""; position: absolute; left: 10px; right: 10px; bottom: -1px; height: 2.5px; background: var(--brand); border-radius: 2px 2px 0 0; }
.gops-tabs--pills .gops-tab { border-radius: var(--radius-sm); padding: 7px 14px; }
.gops-tabs--pills .gops-tab--active { background: var(--surface-card); color: var(--text-strong); box-shadow: var(--shadow-xs); }
.gops-tabs--pills .gops-tab--active::after { display: none; }
.gops-tab__count { font-size: var(--text-xs); font-weight: var(--weight-semibold); background: var(--slate-200); color: var(--text-muted); padding: 1px 7px; border-radius: var(--radius-pill); }
.gops-tab--active .gops-tab__count { background: var(--sprout-100); color: var(--sprout-700); }
`;

/** Tab bar. `variant="pills"` for segmented control style. */
export function Tabs({ tabs = [], value, onChange, variant = "underline", className = "" }) {
  injectStyle("gops-tabs", CSS);
  const cls = ["gops-tabs", variant === "pills" ? "gops-tabs--pills" : "", className].filter(Boolean).join(" ");
  return (
    <div className={cls} role="tablist">
      {tabs.map((t) => {
        const key = t.id ?? t.label;
        const active = key === value;
        return (
          <button key={key} role="tab" aria-selected={active}
            className={["gops-tab", active ? "gops-tab--active" : ""].filter(Boolean).join(" ")}
            onClick={() => onChange && onChange(key)}>
            {t.icon}
            {t.label}
            {t.count != null && <span className="gops-tab__count">{t.count}</span>}
          </button>
        );
      })}
    </div>
  );
}
