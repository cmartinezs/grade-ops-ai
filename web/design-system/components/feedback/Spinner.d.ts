import * as React from "react";

export interface SpinnerProps extends React.HTMLAttributes<HTMLSpanElement> {
  /** @default "sm" */
  size?: "xs" | "sm" | "md" | "lg";
}

export interface LoadingOverlayProps {
  /** Caption under the spinner. @default "Procesando…" */
  label?: string;
  /** @default "md" */
  size?: "xs" | "sm" | "md" | "lg";
}

export declare function Spinner(props: SpinnerProps): JSX.Element;
export declare function LoadingOverlay(props: LoadingOverlayProps): JSX.Element;
