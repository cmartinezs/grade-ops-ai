import * as React from "react";

export interface SelectProps
  extends Omit<React.SelectHTMLAttributes<HTMLSelectElement>, "size"> {
  /** @default "md" */
  size?: "sm" | "md";
}

export declare function Select(props: SelectProps): JSX.Element;
