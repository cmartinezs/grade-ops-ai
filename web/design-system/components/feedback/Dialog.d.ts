import * as React from "react";

export interface DialogProps {
  open: boolean;
  onClose?: () => void;
  title?: React.ReactNode;
  /** Header badge icon style. */
  icon?: "danger" | "warning" | "brand";
  /** @default "md" */
  size?: "md" | "lg";
  children?: React.ReactNode;
  footer?: React.ReactNode;
}

export interface ConfirmDialogProps {
  open: boolean;
  onCancel?: () => void;
  onConfirm?: () => void;
  title?: React.ReactNode;
  message?: React.ReactNode;
  confirmLabel?: string;
  cancelLabel?: string;
  /** @default "brand" — use "danger" for destructive confirms. */
  tone?: "brand" | "warning" | "danger";
  /** Keep open with a spinner while the action resolves. */
  loading?: boolean;
}

export declare function Dialog(props: DialogProps): JSX.Element | null;
export declare function ConfirmDialog(props: ConfirmDialogProps): JSX.Element | null;
