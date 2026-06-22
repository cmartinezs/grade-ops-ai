import * as React from "react";

export type ToastTone = "success" | "error" | "info";

export interface ToastData {
  id: string | number;
  tone?: ToastTone;
  title?: React.ReactNode;
  message?: React.ReactNode;
  loading?: boolean;
}

export interface ToastProps extends Omit<ToastData, "id"> {
  onClose?: () => void;
}

export interface ToastViewportProps {
  toasts?: ToastData[];
  onDismiss?: (id: string | number) => void;
}

export declare function Toast(props: ToastProps): JSX.Element;
export declare function ToastViewport(props: ToastViewportProps): JSX.Element | null;
