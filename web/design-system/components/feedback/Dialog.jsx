import React from "react";
import { injectStyle } from "../_lib/styleInjector.js";
import { Button } from "../core/Button.jsx";

const CSS = `
@keyframes gops-dialog-overlay { from { opacity: 0; } to { opacity: 1; } }
@keyframes gops-dialog-pop { from { opacity: 0; transform: translateY(12px) scale(0.97); } to { opacity: 1; transform: none; } }
.gops-dialog__overlay {
  position: fixed; inset: 0; z-index: var(--z-modal);
  background: color-mix(in srgb, var(--slate-950) 45%, transparent);
  backdrop-filter: blur(2px);
  display: flex; align-items: center; justify-content: center;
  padding: 20px;
  animation: gops-dialog-overlay var(--dur-base) var(--ease-out);
}
.gops-dialog {
  background: var(--surface-card);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  width: 100%; max-width: 440px;
  max-height: calc(100vh - 40px); overflow: auto;
  animation: gops-dialog-pop var(--dur-base) var(--ease-spring);
}
.gops-dialog--lg { max-width: 640px; }
.gops-dialog__head { display: flex; align-items: flex-start; gap: 14px; padding: 22px 22px 0; }
.gops-dialog__badge { flex: none; width: 44px; height: 44px; border-radius: var(--radius-md); display: flex; align-items: center; justify-content: center; }
.gops-dialog__badge svg { width: 24px; height: 24px; }
.gops-dialog__badge--danger { background: var(--danger-50); color: var(--danger-600); }
.gops-dialog__badge--warning { background: var(--warning-50); color: var(--warning-600); }
.gops-dialog__badge--brand { background: var(--sprout-50); color: var(--brand); }
.gops-dialog__title { font-family: var(--font-display); font-weight: var(--weight-semibold); font-size: var(--text-xl); color: var(--text-strong); line-height: 1.2; }
.gops-dialog__body { padding: 8px 22px 0; color: var(--text-muted); font-size: var(--text-md); line-height: var(--leading-normal); }
.gops-dialog__body--inset { padding-left: 80px; }
.gops-dialog__foot { display: flex; justify-content: flex-end; gap: 10px; padding: 22px; }
`;

const BADGE_ICON = {
  danger: <path d="M12 9v4m0 4h.01M10.3 4.3L2.6 18a2 2 0 001.7 3h15.4a2 2 0 001.7-3L13.7 4.3a2 2 0 00-3.4 0z" />,
  warning: <path d="M12 9v4m0 4h.01M10.3 4.3L2.6 18a2 2 0 001.7 3h15.4a2 2 0 001.7-3L13.7 4.3a2 2 0 00-3.4 0z" />,
  brand: <path d="M9 11l3 3L22 4M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11" />,
};

/** Modal surface. Use Dialog for custom content; ConfirmDialog for confirms. */
export function Dialog({ open, onClose, title, icon, size = "md", children, footer }) {
  injectStyle("gops-dialog", CSS);
  if (!open) return null;
  return (
    <div className="gops-dialog__overlay" onMouseDown={(e) => { if (e.target === e.currentTarget && onClose) onClose(); }}>
      <div className={["gops-dialog", size === "lg" ? "gops-dialog--lg" : ""].filter(Boolean).join(" ")} role="dialog" aria-modal="true">
        {(title || icon) && (
          <div className="gops-dialog__head">
            {icon && <div className={`gops-dialog__badge gops-dialog__badge--${icon}`}><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">{BADGE_ICON[icon]}</svg></div>}
            {title && <div className="gops-dialog__title">{title}</div>}
          </div>
        )}
        <div className={["gops-dialog__body", icon ? "gops-dialog__body--inset" : ""].filter(Boolean).join(" ")}>{children}</div>
        {footer && <div className="gops-dialog__foot">{footer}</div>}
      </div>
    </div>
  );
}

/**
 * Confirmation dialog — the GradeOps "confirm before any data-mutating or
 * leave-with-unsaved action" pattern. Pass `loading` to keep it open with a
 * spinner while the async action resolves.
 */
export function ConfirmDialog({
  open, onCancel, onConfirm,
  title = "¿Confirmar acción?",
  message,
  confirmLabel = "Confirmar",
  cancelLabel = "Cancelar",
  tone = "brand",
  loading = false,
}) {
  const confirmVariant = tone === "danger" ? "danger" : "primary";
  return (
    <Dialog
      open={open}
      onClose={loading ? undefined : onCancel}
      title={title}
      icon={tone}
      footer={
        <>
          <Button variant="ghost" onClick={onCancel} disabled={loading}>{cancelLabel}</Button>
          <Button variant={confirmVariant} onClick={onConfirm} loading={loading}>{confirmLabel}</Button>
        </>
      }
    >
      {message}
    </Dialog>
  );
}
