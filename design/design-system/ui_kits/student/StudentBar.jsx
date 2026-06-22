// Student top bar — minimal, branded, shows who's signed in via magic link.
function StudentBar({ right }) {
  return (
    <header style={barStyles.bar}>
      <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
        <img src="../../assets/logo-mark.svg" alt="" style={{ width: 30, height: 30 }} />
        <span style={barStyles.brand}>GradeOps<span style={{ color: "var(--brand)" }}>AI</span></span>
      </div>
      <div style={{ display: "flex", alignItems: "center", gap: 12 }}>{right}</div>
    </header>
  );
}
const barStyles = {
  bar: { height: 60, flex: "none", display: "flex", alignItems: "center", justifyContent: "space-between", padding: "0 24px", borderBottom: "1px solid var(--border-subtle)", background: "var(--surface-card)" },
  brand: { fontFamily: "var(--font-display)", fontWeight: 700, fontSize: 18, color: "var(--text-strong)", letterSpacing: "-0.02em" },
};
window.StudentBar = StudentBar;
