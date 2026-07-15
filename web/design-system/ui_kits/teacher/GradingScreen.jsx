// AI grading review for an open assessment: rubric indices, performance levels,
// AI-generated feedback with reasoning, teacher approves/edits before publishing.
const { Card, Badge, Button, Avatar, RubricLevel, ProgressBar, Textarea, IconButton, LoadingOverlay } = window.GradeOpsAIDesignSystem_fcd12b;

const STUDENTS = [
  { name: "Camila Rojas", score: "6,2", level: "exceeds", done: true },
  { name: "Diego Soto", score: "5,1", level: "meets", done: true, active: true },
  { name: "Fernanda Paz", score: "4,3", level: "developing", done: true },
  { name: "Tomás Vidal", score: "3,2", level: "beginning", done: true },
  { name: "Ignacia Lobos", score: "—", level: null, done: false },
];

const CRITERIA = [
  { name: "Comprensión del concepto", weight: "30%", level: "meets",
    reason: "Identifica correctamente las fases lumínica y oscura, pero confunde el rol del NADPH en la cadena. La definición es precisa aunque incompleta." },
  { name: "Uso de evidencia y ejemplos", weight: "25%", level: "exceeds",
    reason: "Aporta dos ejemplos pertinentes (plantas C3 y C4) y los relaciona con el contexto climático. Supera lo solicitado." },
  { name: "Estructura y argumentación", weight: "25%", level: "developing",
    reason: "La introducción es clara, pero la conclusión no retoma la tesis y el desarrollo presenta saltos lógicos entre párrafos." },
  { name: "Uso de lenguaje técnico", weight: "20%", level: "meets",
    reason: "Emplea vocabulario disciplinar adecuado (cloroplasto, estroma, tilacoide) con un uso impreciso de «energía» como sinónimo de ATP." },
];

function GradingScreen({ onPublish }) {
  const [regrading, setRegrading] = React.useState(false);

  return (
    <div style={{ display: "grid", gridTemplateColumns: "240px 1fr", gap: 18, maxWidth: "var(--content-max)", alignItems: "start" }}>
      <Card>
        <Card.Header><Card.Title style={{ fontSize: "var(--text-md)" }}>Entregas · 28</Card.Title></Card.Header>
        <div style={{ padding: "6px 0 8px" }}>
          {STUDENTS.map((s, i) => (
            <div key={i} style={{ ...gradeStyles.stuRow, ...(s.active ? gradeStyles.stuActive : {}) }}>
              <Avatar name={s.name} size="sm" />
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={gradeStyles.stuName}>{s.name}</div>
                <div style={gradeStyles.stuMeta}>{s.done ? "Corregida por IA" : "Sin entrega"}</div>
              </div>
              {s.done
                ? <span style={{ fontFamily: "var(--font-display)", fontWeight: 700, fontSize: "var(--text-md)", color: s.active ? "var(--brand)" : "var(--text-strong)" }}>{s.score}</span>
                : <Icon name="clock" size={16} color="var(--text-subtle)" />}
            </div>
          ))}
        </div>
      </Card>

      <div style={{ display: "flex", flexDirection: "column", gap: 16, position: "relative" }}>
        {regrading && <LoadingOverlay label="Recalificando con IA…" />}

        <Card>
          <div style={gradeStyles.head}>
            <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
              <Avatar name="Diego Soto" size="md" />
              <div>
                <div style={{ fontFamily: "var(--font-display)", fontWeight: 700, fontSize: "var(--text-lg)", color: "var(--text-strong)" }}>Diego Soto</div>
                <div style={{ fontSize: "var(--text-sm)", color: "var(--text-muted)" }}>Ensayo: Fotosíntesis · 2°A Biología · entregado a tiempo</div>
              </div>
            </div>
            <div style={{ textAlign: "right" }}>
              <div style={{ fontFamily: "var(--font-display)", fontWeight: 800, fontSize: "var(--text-3xl)", color: "var(--text-strong)", lineHeight: 1 }}>5,1</div>
              <RubricLevel level="meets" />
            </div>
          </div>

          <div style={{ padding: "0 20px 18px", display: "flex", alignItems: "center", gap: 10 }}>
            <Icon name="sparkles" size={16} color="var(--brand)" />
            <span style={{ fontSize: "var(--text-sm)", color: "var(--text-muted)" }}>Corregido automáticamente según la rúbrica de 4 índices. Revisa y publica.</span>
          </div>
        </Card>

        <Card>
          <Card.Header><Card.Title>Rúbrica e indicadores</Card.Title><Badge tone="brand">4 índices</Badge></Card.Header>
          <div>
            {CRITERIA.map((c, i) => (
              <div key={i} style={gradeStyles.crit}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: 12, marginBottom: 8 }}>
                  <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                    <span style={{ fontWeight: 600, color: "var(--text-strong)" }}>{c.name}</span>
                    <span style={{ fontSize: "var(--text-xs)", color: "var(--text-subtle)", fontFamily: "var(--font-mono)" }}>{c.weight}</span>
                  </div>
                  <RubricLevel level={c.level} />
                </div>
                <div style={gradeStyles.reason}>
                  <Icon name="quote" size={14} color="var(--text-subtle)" style={{ marginTop: 2 }} />
                  <span>{c.reason}</span>
                </div>
              </div>
            ))}
          </div>
        </Card>

        <Card>
          <Card.Header><Card.Title>Retroalimentación para el estudiante</Card.Title></Card.Header>
          <Card.Body>
            <Textarea defaultValue={"Buen dominio conceptual de la fotosíntesis, Diego. Tu mayor fortaleza fue el uso de ejemplos (plantas C3 y C4). Para mejorar, enfócate en cerrar tus ensayos retomando la tesis y en cuidar las transiciones entre párrafos. Repasa el rol del NADPH en la fase oscura."} style={{ minHeight: 110 }} />
            <div style={gradeStyles.lowpoints}>
              <Icon name="target" size={16} color="var(--gold-600)" />
              <span><b>Puntos a mejorar:</b> estructura y argumentación · rol del NADPH</span>
            </div>
          </Card.Body>
          <Card.Footer>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: 12 }}>
              <Button variant="secondary" size="sm" iconLeft={<Icon name="refresh-cw" size={15} />}
                onClick={() => { setRegrading(true); setTimeout(() => setRegrading(false), 1500); }}>
                Recalificar con IA
              </Button>
              <div style={{ display: "flex", gap: 10 }}>
                <Button variant="ghost">Guardar borrador</Button>
                <Button variant="primary" iconLeft={<Icon name="send" size={16} />} onClick={onPublish}>Publicar y enviar enlace</Button>
              </div>
            </div>
          </Card.Footer>
        </Card>
      </div>
    </div>
  );
}

const gradeStyles = {
  stuRow: { display: "flex", alignItems: "center", gap: 10, padding: "9px 14px", borderLeft: "3px solid transparent", cursor: "pointer" },
  stuActive: { background: "var(--surface-brand-soft)", borderLeftColor: "var(--brand)" },
  stuName: { fontWeight: 600, fontSize: "var(--text-sm)", color: "var(--text-strong)", whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" },
  stuMeta: { fontSize: "var(--text-xs)", color: "var(--text-subtle)" },
  head: { display: "flex", justifyContent: "space-between", alignItems: "flex-start", padding: "20px 20px 14px", gap: 16 },
  crit: { padding: "14px 20px", borderBottom: "1px solid var(--border-subtle)" },
  reason: { display: "flex", gap: 8, fontSize: "var(--text-sm)", color: "var(--text-muted)", lineHeight: 1.55, background: "var(--surface-sunken)", padding: "10px 12px", borderRadius: "var(--radius-sm)" },
  lowpoints: { display: "flex", alignItems: "center", gap: 8, marginTop: 12, padding: "10px 12px", background: "var(--gold-50)", border: "1px solid var(--gold-200)", borderRadius: "var(--radius-sm)", fontSize: "var(--text-sm)", color: "var(--gold-800)" },
};

window.GradingScreen = GradingScreen;
