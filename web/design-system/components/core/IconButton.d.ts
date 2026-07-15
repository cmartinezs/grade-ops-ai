import * as React from "react";

export interface IconButtonProps
  extends Omit<React.ButtonHTMLAttributes<HTMLButtonElement>, "className"> {
  /** @default "ghost" */
  variant?: "ghost" | "solid" | "outline";
  /** @default "md" */
  size?: "sm" | "md";
  /** Accessible label (also used as tooltip title). */
  label: string;
  className?: string;
}

export declare function IconButton(props: IconButtonProps): JSX.Element;
