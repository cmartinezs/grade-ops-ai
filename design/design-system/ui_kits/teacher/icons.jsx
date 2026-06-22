// Lucide → React icon helper for GradeOps UI kits.
// Builds the SVG from lucide's icon-node data (idempotent across re-renders).
function Icon({ name, size = 20, strokeWidth = 2, color = "currentColor", style, className }) {
  const pascal = String(name).split("-").map((s) => s[0].toUpperCase() + s.slice(1)).join("");
  const node = (window.lucide && window.lucide.icons && window.lucide.icons[pascal]) || [];
  const children = node.map(([tag, attrs], i) => React.createElement(tag, { key: i, ...attrs }));
  return React.createElement(
    "svg",
    {
      className,
      width: size, height: size, viewBox: "0 0 24 24", fill: "none",
      stroke: color, strokeWidth, strokeLinecap: "round", strokeLinejoin: "round",
      style: { flex: "none", display: "block", ...style },
    },
    children
  );
}

window.Icon = Icon;
