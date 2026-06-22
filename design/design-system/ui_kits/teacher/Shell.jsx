// Teacher portal app shell: sidebar + topbar. Composes DS primitives.
const { Avatar, Badge, IconButton, Button } = window.GradeOpsAIDesignSystem_fcd12b;

const NAV = [
  { id: "dashboard", label: "Panel", icon: "layout-dashboard" },
  { id: "assessments", label: "Evaluaciones", icon: "file-pen-line" },
  { id: "bank", label: "Banco de preguntas", icon: "library" },
  { id: "students", label: "Estudiantes", icon: "users" },
  { id: "reports", label: "Reportes", icon: "bar-chart-3" },
];

function Shell({ active, onNavigate, title, subtitle, actions, children }) {
  return (
    <div style={shellStyles.root}>
      <aside style={shellStyles.sidebar}>
        <div style={shellStyles.brand}>
          <img src="../../assets/logo-mark.svg" alt="" style={{ width: 34, height: 34 }} />
          <span style={shellStyles.brandText}>GradeOps<span style={{ color: "var(--brand)" }}>AI</span></span>
        </div>

        <nav style={shellStyles.nav}>
          {NAV.map((n) => {
            const on = n.id === active;
            return (
              <button key={n.id} onClick={() => onNavigate(n.id)}
                style={{ ...shellStyles.navItem, ...(on ? shellStyles.navItemActive : {}) }}
                onMouseEnter={(e) => { if (!on) e.currentTarget.style.background = "var(--surface-sunken)"; }}
                onMouseLeave={(e) => { if (!on) e.currentTarget.style.background = "transparent"; }}>
                <Icon name={n.icon} size={19} />
                {n.label}
              </button>
            );
          })}
        </nav>

        <div style={shellStyles.upsell}>
          <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 6 }}>
            <Icon name="sparkles" size={16} color="var(--gold-600)" />
            <span style={{ fontWeight: 700, fontSize: "var(--text-sm)", color: "var(--text-strong)" }}>Créditos IA</span>
          </div>
          <p style={{ margin: 0, fontSize: "var(--text-xs)", color: "var(--text-muted)", lineHeight: 1.5 }}>
            742 correcciones restantes este mes.
          </p>
        </div>

        <div style={shellStyles.userRow}>
          <Avatar name="Paula Méndez" size="sm" />
          <div style={{ minWidth: 0, flex: 1 }}>
            <div style={shellStyles.userName}>Paula Méndez</div>
            <div style={shellStyles.userMeta}>Liceo Andes</div>
          </div>
          <IconButton label="Cerrar sesión" size="sm"><Icon name="log-out" size={16} /></IconButton>
        </div>
      </aside>

      <div style={shellStyles.main}>
        <header style={shellStyles.topbar}>
          <div style={{ minWidth: 0 }}>
            <h1 style={shellStyles.title}>{title}</h1>
            {subtitle && <p style={shellStyles.subtitle}>{subtitle}</p>}
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <IconButton label="Notificaciones"><Icon name="bell" size={19} /></IconButton>
            {actions}
          </div>
        </header>
        <div style={shellStyles.content}>{children}</div>
      </div>
    </div>
  );
}

const shellStyles = {
  root: { display: "flex", height: "100%", minHeight: 0, background: "var(--surface-page)", fontFamily: "var(--font-sans)" },
  sidebar: { width: "var(--sidebar-width)", flex: "none", background: "var(--surface-card)", borderRight: "1px solid var(--border-subtle)", display: "flex", flexDirection: "column", padding: "18px 14px" },
  brand: { display: "flex", alignItems: "center", gap: 10, padding: "4px 8px 18px" },
  brandText: { fontFamily: "var(--font-display)", fontWeight: 700, fontSize: 19, color: "var(--text-strong)", letterSpacing: "-0.02em" },
  nav: { display: "flex", flexDirection: "column", gap: 2 },
  navItem: { display: "flex", alignItems: "center", gap: 11, padding: "9px 11px", border: "none", background: "transparent", borderRadius: "var(--radius-md)", cursor: "pointer", fontFamily: "inherit", fontSize: "var(--text-md)", fontWeight: 500, color: "var(--text-muted)", textAlign: "left", transition: "background 120ms, color 120ms" },
  navItemActive: { background: "var(--surface-brand-soft)", color: "var(--brand-hover)", fontWeight: 600 },
  upsell: { marginTop: "auto", background: "var(--gold-50)", border: "1px solid var(--gold-200)", borderRadius: "var(--radius-md)", padding: "12px", margin: "0 2px 14px" },
  userRow: { display: "flex", alignItems: "center", gap: 10, padding: "10px 8px 2px", borderTop: "1px solid var(--border-subtle)" },
  userName: { fontWeight: 600, fontSize: "var(--text-sm)", color: "var(--text-strong)", whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" },
  userMeta: { fontSize: "var(--text-xs)", color: "var(--text-subtle)" },
  main: { flex: 1, minWidth: 0, display: "flex", flexDirection: "column", overflow: "hidden" },
  topbar: { height: "var(--topbar-height)", flex: "none", display: "flex", alignItems: "center", justifyContent: "space-between", gap: 16, padding: "0 28px", borderBottom: "1px solid var(--border-subtle)", background: "var(--surface-card)" },
  title: { fontFamily: "var(--font-display)", fontWeight: 600, fontSize: "var(--text-xl)", color: "var(--text-strong)", margin: 0, lineHeight: 1.1, letterSpacing: "-0.01em" },
  subtitle: { margin: "2px 0 0", fontSize: "var(--text-sm)", color: "var(--text-muted)" },
  content: { flex: 1, overflow: "auto", padding: "28px" },
};

window.Shell = Shell;
window.TEACHER_NAV = NAV;
