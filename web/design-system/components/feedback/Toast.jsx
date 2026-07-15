import React from "react";
import { injectStyle } from "../_lib/styleInjector.js";

const CSS = `
@keyframes gops-toast-in { from { opacity: 0; transform: translateY(10px) scale(0.98); } to { opacity: 1; transform: none; } }
.gops-toastviewport {
  position: fixed; bottom: 20px; right: 20px;
  display: flex; flex-direction: column; gap: 10px;
  z-index: var(--z-toast); width: min(380px, calc(100vw - 40px));
  pointer-events: none;
}
.gops-toast {
  pointer-events: auto;
  display: flex; align-items: flex-start; gap: 12px;
  background: var(--surface-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  padding: 12px 14px;
  animation: gops-toast-in var(--dur-base) var(--ease-spring);
}
.gops-toast__icon { flex: none; width: 22px; height: 22px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin-top: 1px; }
.gops-toast__icon svg { width: 14px; height: 14px; }
.gops-toast__body { flex: 1; min-width: 0; }
.gops-toast__title { font-family: var(--font-sans); font-weight: var(--weight-semibold); font-size: var(--text-md); color: var(--text-strong); line-height: 1.3; }
.gops-toast__msg { font-size: var(--text-sm); color: var(--text-muted); margin-top: 2px; }
.gops-toast__close { flex: none; background: none; border: none; cursor: pointer; color: var(--text-subtle); padding: 2px; border-radius: var(--radius-xs); display: flex; }
.gops-toast__close:hover { color: var(--text-strong); background: var(--surface-sunken); }
.gops-toast__close svg { width: 16px; height: 16px; }
.gops-toast--success .gops-toast__icon { background: var(--success-50); color: var(--success-600); }
.gops-toast--error   .gops-toast__icon { background: var(--danger-50); color: var(--danger-600); }
.gops-toast--info    .gops-toast__icon { background: var(--info-50); color: var(--info-600); }
.gops-toast--loading .gops-toast__icon { background: var(--sprout-50); color: var(--brand); }
`;

const ICONS = {
  success: <path d="M5 12l5 5L20 7" />,
  error: <path d="M18 6L6 18M6 6l12 12" />,
  info: <path d="M12 8h.01M11 12h1v4h1" />,
};

/** Single toast. Used by ToastViewport; `loading` shows a spinner icon. */
export function Toast({ tone = "info", title, message, loading = false, onClose }) {
  injectStyle("gops-toast", CSS);
  return (
    <div className={`gops-toast gops-toast--${loading ? "loading" : tone}`} role="status">
      <span className="gops-toast__icon" aria-hidden="true">
        {loading
          ? <span style={{ width: 14, height: 14, border: "2px solid currentColor", borderRightColor: "transparent", borderRadius: "50%", animation: "gops-spin 0.6s linear infinite" }} />
          : <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">{ICONS[tone] || ICONS.info}</svg>}
      </span>
      <div className="gops-toast__body">
        {title && <div className="gops-toast__title">{title}</div>}
        {message && <div className="gops-toast__msg">{message}</div>}
      </div>
      {onClose && (
        <button className="gops-toast__close" onClick={onClose} aria-label="Cerrar">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><path d="M18 6L6 18M6 6l12 12"/></svg>
        </button>
      )}
    </div>
  );
}

/** Fixed bottom-right stack. Pass an array of toast objects + onDismiss(id). */
export function ToastViewport({ toasts = [], onDismiss }) {
  injectStyle("gops-toast", CSS);
  if (!toasts.length) return null;
  return (
    <div className="gops-toastviewport">
      {toasts.map((t) => (
        <Toast key={t.id} {...t} onClose={onDismiss ? () => onDismiss(t.id) : undefined} />
      ))}
    </div>
  );
}
