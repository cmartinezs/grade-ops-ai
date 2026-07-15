import * as React from "react";

export interface AvatarProps extends React.HTMLAttributes<HTMLSpanElement> {
  /** Full name — used for initials + tooltip. */
  name?: string;
  /** Optional image URL. */
  src?: string | null;
  /** @default "md" */
  size?: "xs" | "sm" | "md" | "lg";
}

export declare function Avatar(props: AvatarProps): JSX.Element;
