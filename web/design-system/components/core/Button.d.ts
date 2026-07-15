import * as React from "react";

export type ButtonVariant =
  | "primary"
  | "accent"
  | "secondary"
  | "ghost"
  | "danger"
  | "quiet-danger";

export type ButtonSize = "sm" | "md" | "lg";

/**
 * Primary action button for GradeOps AI.
 *
 * @startingPoint section="Core" subtitle="Action button with variants, sizes, async loading state" viewport="700x180"
 */
export interface ButtonProps
  extends Omit<React.ButtonHTMLAttributes<HTMLButtonElement>, "className"> {
  /** Visual style. @default "primary" */
  variant?: ButtonVariant;
  /** Control height. @default "md" */
  size?: ButtonSize;
  /** Stretch to full container width. */
  block?: boolean;
  /** Show inline spinner and disable — GradeOps async-action convention. */
  loading?: boolean;
  /** Icon node rendered before the label. */
  iconLeft?: React.ReactNode;
  /** Icon node rendered after the label. */
  iconRight?: React.ReactNode;
  className?: string;
}

export declare function Button(props: ButtonProps): JSX.Element;
