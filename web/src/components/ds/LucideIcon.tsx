export type LucideIconName =
  | "layout-dashboard"
  | "file-pen-line"
  | "library"
  | "users"
  | "bar-chart-3"
  | "bell"
  | "sparkles"
  | "log-out"
  | "chevron-right"
  | "trending-up"
  | "trending-down"
  | "alert-triangle"
  | "calendar-clock"
  | "clipboard-check"
  | "mail"
  | "lock"
  | "graduation-cap"
  | "minus"
  | "user"
  | "eye"
  | "eye-off";

interface LucideIconProps {
  name: LucideIconName;
  size?: number;
  color?: string;
  className?: string;
  style?: React.CSSProperties;
  "aria-hidden"?: boolean;
}

const ICON_PATHS: Record<LucideIconName, string> = {
  "layout-dashboard": `<rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/>`,
  "file-pen-line": `<path d="M12 20h9"/><path d="M16.5 3.5a2.12 2.12 0 013 3L7 19l-4 1 1-4L16.5 3.5z"/>`,
  "library": `<path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/>`,
  "users": `<path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/>`,
  "bar-chart-3": `<line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/>`,
  "bell": `<path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/>`,
  "sparkles": `<path d="M12 3l1.9 5.8H20l-4.9 3.6 1.9 5.7L12 14.6l-5 3.5 1.9-5.7L4 8.8h6.1L12 3z"/>`,
  "log-out": `<path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>`,
  "chevron-right": `<polyline points="9 18 15 12 9 6"/>`,
  "trending-up": `<polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/>`,
  "trending-down": `<polyline points="23 18 13.5 8.5 8.5 13.5 1 6"/><polyline points="17 18 23 18 23 12"/>`,
  "alert-triangle": `<path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>`,
  "calendar-clock": `<path d="M21 7.5V6a2 2 0 00-2-2H5a2 2 0 00-2 2v14a2 2 0 002 2h3.5"/><path d="M16 2v4"/><path d="M8 2v4"/><path d="M3 10h5"/><circle cx="17.5" cy="17.5" r="4.5"/><path d="M17.5 15.5v2l1 1"/>`,
  "clipboard-check": `<path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2"/><rect x="9" y="3" width="6" height="4" rx="1"/><path d="M9 14l2 2 4-4"/>`,
  "mail": `<path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/>`,
  "lock": `<rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0110 0v4"/>`,
  "graduation-cap": `<path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c3 3 9 3 12 0v-5"/>`,
  "minus": `<line x1="5" y1="12" x2="19" y2="12"/>`,
  "user": `<path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/>`,
  "eye": `<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>`,
  "eye-off": `<path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94"/><path d="M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/>`,
};

export default function LucideIcon({
  name,
  size = 24,
  color = "currentColor",
  className = "",
  style,
  "aria-hidden": ariaHidden = true,
}: LucideIconProps) {
  const paths = ICON_PATHS[name];
  if (!paths) return null;
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke={color}
      strokeWidth={2}
      strokeLinecap="round"
      strokeLinejoin="round"
      className={className}
      style={style}
      aria-hidden={ariaHidden}
      dangerouslySetInnerHTML={{ __html: paths }}
    />
  );
}
