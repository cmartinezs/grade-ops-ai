import * as React from "react";

export interface InputProps
  extends Omit<React.InputHTMLAttributes<HTMLInputElement>, "size"> {
  /** @default "md" */
  size?: "sm" | "md" | "lg";
  /** Error border + aria-invalid. */
  invalid?: boolean;
  /** Monospace (codes, IDs, magic-link tokens). */
  mono?: boolean;
  /** Leading icon node. */
  icon?: React.ReactNode;
}

export declare function Input(props: InputProps): JSX.Element;
