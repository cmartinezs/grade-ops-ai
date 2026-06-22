// Teacher control panel: KPIs, recent assessments, group performance, at-risk.
const { Card, StatCard, Badge, Button, Tabs, ProgressBar, RubricLevel, Avatar } = window.GradeOpsAIDesignSystem_fcd12b;

const RECENT = [
  { name: "Prueba Unidad 3 — Fotosíntesis", type: "Cerrada", tone: "brand", course: "1°B Biología", status: "Corregida", st: "success", count: 32, avg: "5,4" },
  { name: "Ensayo: Revolución Industrial", type: "Abierta", tone: "gold", course: "2°A Historia", status: "En revisión", st: "warning", count: 28, avg: "—" },
  { name: "Control de lectura — Quijote", type: "Mixta", tone: "info", course: "3°A Lenguaje", status: "En curso", st: "info", count: 30, avg: "—" },
  { name: "Quiz: Funciones cuadráticas", type: "Cerrada", tone: "brand", course: "2°B Matemática", status: "Corregida", st: "success", count: 31, avg: "4,9" },
];

const RISK = [
  { name: "Tomás Vidal", course: "1°B", avg: "3,2", trend: "down" },
  { name: "Javiera Lillo", course: "2°A", avg: "3,8", trend: "down" },
  { name: "Benjamín Ortiz", course: "3°A", avg: "3,9", trend: "flat" },
];

function DashboardScreen({ onOpenAssessment, onGoBank }) {
  const [tab, setTab] = React.useState("all");
  const I = (n, c) => <Icon name={n} size={19} color={c} />;
  const rows = tab === "all" ? RECENT : RECENT.filter((r) => r.type.toLowerCase() === ({ open: "abierta", closed: "cerrada", mixed: "mixta" }[tab]));

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 22, maxWidth: "var(--content-max)" }}>
      <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 16 }}>
        <StatCard label="Promedio general" value="5,2" icon={I("trending-up")} delta="+0,3" />
        <StatCard label="Por corregir" value="14" iconTone="gold" icon={I("clipboard-check")} />
        <StatCard label="Entregas a tiempo" value="92" unit="%" iconTone="info" icon={I("calendar-clock")} />
        <StatCard label="En riesgo" value="3" unit="alumnos" iconTone="danger" icon={I("alert-triangle")} delta="-1" deltaDir="down" />
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1.7fr 1fr", gap: 18, alignItems: "start" }}>
        <Card>
          <Card.Header>
            <Card.Title>Evaluaciones recientes</Card.Title>
            <Tabs value={tab} onChange={setTab} variant="pills" tabs={[
              { id: "all", label: "Todas" }, { id: "open", label: "Abiertas" },
              { id: "closed", label: "Cerradas" }, { id: "mixed", label: "Mixtas" },
            ]} />
          </Card.Header>
          <div>
            {rows.map((r, i) => (
              <button key={i} onClick={onOpenAssessment} style={rowStyles.row}
                onMouseEnter={(e) => (e.currentTarget.style.background = "var(--surface-sunken)")}
                onMouseLeave={(e) => (e.currentTarget.style.background = "transparent")}>
                <div style={{ minWidth: 0, flex: 1, textAlign: "left" }}>
                  <div style={rowStyles.name}>{r.name}</div>
                  <div style={rowStyles.meta}>
                    <Badge tone={r.tone}>{r.type}</Badge>
                    <span>{r.course}</span><span>·</span><span>{r.count} estudiantes</span>
                  </div>
                </div>
                <div style={{ textAlign: "right", display: "flex", alignItems: "center", gap: 16 }}>
                  <div>
                    <div style={rowStyles.avg}>{r.avg}</div>
                    <div style={rowStyles.avgLbl}>promedio</div>
                  </div>
                  <Badge tone={r.st} dot>{r.status}</Badge>
                  <Icon name="chevron-right" size={18} color="var(--text-subtle)" />
                </div>
              </button>
            ))}
          </div>
          <Card.Footer>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <span style={{ fontSize: "var(--text-sm)", color: "var(--text-muted)" }}>Mostrando {rows.length} de 24</span>
              <Button variant="ghost" size="sm" iconRight={<Icon name="arrow-right" size={16} />}>Ver todas</Button>
            </div>
          </Card.Footer>
        </Card>

        <div style={{ display: "flex", flexDirection: "column", gap: 18 }}>
          <Card>
            <Card.Header><Card.Title>Cobertura por curso</Card.Title></Card.Header>
            <Card.Body>
              <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
                <ProgressBar value={84} tone="success" label="1°B Biología" showValue />
                <ProgressBar value={61} label="2°A Historia" showValue />
                <ProgressBar value={47} tone="gold" label="3°A Lenguaje" showValue />
                <ProgressBar value={28} tone="danger" label="2°B Matemática" showValue />
              </div>
            </Card.Body>
          </Card>

          <Card>
            <Card.Header>
              <Card.Title>Estudiantes en riesgo</Card.Title>
              <Badge tone="danger">{RISK.length}</Badge>
            </Card.Header>
            <div style={{ padding: "6px 6px 10px" }}>
              {RISK.map((s, i) => (
                <div key={i} style={rowStyles.riskRow}>
                  <Avatar name={s.name} size="sm" />
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={rowStyles.name}>{s.name}</div>
                    <div style={rowStyles.avgLbl}>{s.course}</div>
                  </div>
                  <span style={{ fontFamily: "var(--font-display)", fontWeight: 700, color: "var(--danger-600)" }}>{s.avg}</span>
                  <Icon name={s.trend === "down" ? "trending-down" : "minus"} size={16} color="var(--danger-500)" />
                </div>
              ))}
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}

const rowStyles = {
  row: { width: "100%", display: "flex", alignItems: "center", gap: 16, padding: "14px 20px", border: "none", borderBottom: "1px solid var(--border-subtle)", background: "transparent", cursor: "pointer", fontFamily: "inherit", transition: "background 120ms" },
  name: { fontWeight: 600, fontSize: "var(--text-md)", color: "var(--text-strong)", whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis", marginBottom: 4 },
  meta: { display: "flex", alignItems: "center", gap: 8, fontSize: "var(--text-sm)", color: "var(--text-muted)" },
  avg: { fontFamily: "var(--font-display)", fontWeight: 700, fontSize: "var(--text-lg)", color: "var(--text-strong)", textAlign: "right" },
  avgLbl: { fontSize: "var(--text-xs)", color: "var(--text-subtle)", textAlign: "right" },
  riskRow: { display: "flex", alignItems: "center", gap: 11, padding: "9px 14px" },
};

window.DashboardScreen = DashboardScreen;
