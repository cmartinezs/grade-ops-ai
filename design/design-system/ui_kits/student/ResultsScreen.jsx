// Student results via magic link — shows every assessment across teachers,
// with grade, rubric levels, AI feedback, and lowest points to improve.
const { Card, Badge, Button, RubricLevel, ProgressBar, Avatar } = window.GradeOpsAIDesignSystem_fcd12b;

const OTHER = [
  { name: "Ensayo: Revolución Industrial", course: "Historia", score: "5,8", st: "meets" },
  { name: "Quiz: Funciones cuadráticas", course: "Matemática", score: "4,1", st: "developing" },
  { name: "Control de lectura — Quijote", course: "Lenguaje", score: "6,4", st: "exceeds" },
];

const RUBRIC = [
  { name: "Comprensión del concepto", level: "meets" },
  { name: "Uso de evidencia y ejemplos", level: "exceeds" },
  { name: "Estructura y argumentación", level: "developing" },
  { name: "Uso de lenguaje técnico", level: "meets" },
];

function ResultsScreen({ onBack }) {
  return (
    <div style={resStyles.page}>
      <StudentBar right={<><Avatar name="Diego Soto" size="sm" /><span style={{ fontSize: "var(--text-sm)", fontWeight: 600, color: "var(--text-strong)" }}>Diego Soto</span></>} />
      <div style={resStyles.scroll}>
        <div style={resStyles.wrap}>
          <div style={resStyles.scoreCard}>
            <div>
              <Badge tone="brand" style={{ marginBottom: 10 }}>Fotosíntesis · Biología</Badge>
              <h1 style={resStyles.title}>Tus resultados</h1>
              <p style={resStyles.sub}>Prof. Paula Méndez · publicado hoy</p>
            </div>
            <div style={resStyles.scoreBig}>
              <div style={resStyles.scoreNum}>5,1</div>
              <RubricLevel level="meets" solid />
            </div>
          </div>

          <div style={{ display: "grid", gridTemplateColumns: "1.5fr 1fr", gap: 16, marginTop: 18, alignItems: "start" }}>
            <Card>
              <Card.Header><Card.Title>Retroalimentación</Card.Title><Icon name="sparkles" size={18} color="var(--brand)" /></Card.Header>
              <Card.Body>
                <p style={resStyles.feedback}>
                  Buen dominio conceptual, Diego. Tu mayor fortaleza fue el uso de ejemplos
                  (plantas C3 y C4), que aplicaste con precisión. Para subir tu nota, enfócate
                  en cerrar tus ensayos retomando la tesis y en cuidar las transiciones entre párrafos.
                </p>
                <div style={resStyles.lowbox}>
                  <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 8 }}>
                    <Icon name="target" size={17} color="var(--gold-600)" />
                    <span style={{ fontWeight: 700, color: "var(--gold-800)" }}>Puntos a mejorar</span>
                  </div>
                  <ul style={resStyles.lowlist}>
                    <li>Estructura y argumentación — cierra retomando la tesis.</li>
                    <li>Rol del NADPH en la fase oscura — repasa el material de la clase 4.</li>
                  </ul>
                </div>
              </Card.Body>
            </Card>

            <Card>
              <Card.Header><Card.Title>Rúbrica</Card.Title></Card.Header>
              <div style={{ padding: "6px 16px 14px" }}>
                {RUBRIC.map((r, i) => (
                  <div key={i} style={resStyles.rubRow}>
                    <span style={{ fontSize: "var(--text-sm)", color: "var(--text-body)", flex: 1 }}>{r.name}</span>
                    <RubricLevel level={r.level} />
                  </div>
                ))}
              </div>
            </Card>
          </div>

          <Card style={{ marginTop: 18 }}>
            <Card.Header>
              <Card.Title>Todas tus evaluaciones</Card.Title>
              <Badge tone="neutral">{OTHER.length + 1} en total</Badge>
            </Card.Header>
            <div>
              <div style={resStyles.evalRow}>
                <div style={{ flex: 1 }}>
                  <div style={resStyles.evalName}>Prueba Unidad 3 — Fotosíntesis</div>
                  <div style={resStyles.evalCourse}>Biología · Prof. Méndez</div>
                </div>
                <span style={resStyles.evalScore}>5,1</span>
                <RubricLevel level="meets" />
              </div>
              {OTHER.map((o, i) => (
                <div key={i} style={resStyles.evalRow}>
                  <div style={{ flex: 1 }}>
                    <div style={resStyles.evalName}>{o.name}</div>
                    <div style={resStyles.evalCourse}>{o.course}</div>
                  </div>
                  <span style={resStyles.evalScore}>{o.score}</span>
                  <RubricLevel level={o.st} />
                </div>
              ))}
            </div>
          </Card>

          <div style={{ textAlign: "center", marginTop: 22, color: "var(--text-subtle)", fontSize: "var(--text-xs)" }}>
            Este enlace es personal y reúne todas tus evaluaciones, sin importar el profesor.
          </div>
        </div>
      </div>
    </div>
  );
}

const resStyles = {
  page: { display: "flex", flexDirection: "column", height: "100%", background: "var(--surface-page)", fontFamily: "var(--font-sans)" },
  scroll: { flex: 1, overflow: "auto", padding: "28px 20px 48px" },
  wrap: { maxWidth: 760, margin: "0 auto" },
  scoreCard: { display: "flex", justifyContent: "space-between", alignItems: "center", gap: 20, background: "linear-gradient(140deg, var(--sprout-700), var(--sprout-800))", borderRadius: "var(--radius-xl)", padding: "28px 30px", color: "#fff" },
  title: { fontFamily: "var(--font-display)", fontWeight: 700, fontSize: "var(--text-3xl)", margin: 0, letterSpacing: "-0.02em" },
  sub: { margin: "6px 0 0", opacity: 0.85, fontSize: "var(--text-md)" },
  scoreBig: { textAlign: "center", display: "flex", flexDirection: "column", alignItems: "center", gap: 8 },
  scoreNum: { fontFamily: "var(--font-display)", fontWeight: 800, fontSize: 56, lineHeight: 1 },
  feedback: { margin: 0, fontSize: "var(--text-md)", lineHeight: 1.6, color: "var(--text-body)" },
  lowbox: { marginTop: 16, padding: "14px 16px", background: "var(--gold-50)", border: "1px solid var(--gold-200)", borderRadius: "var(--radius-md)" },
  lowlist: { margin: 0, paddingLeft: 18, color: "var(--gold-800)", fontSize: "var(--text-sm)", lineHeight: 1.7 },
  rubRow: { display: "flex", alignItems: "center", justifyContent: "space-between", gap: 10, padding: "9px 0", borderBottom: "1px solid var(--border-subtle)" },
  evalRow: { display: "flex", alignItems: "center", gap: 14, padding: "13px 20px", borderBottom: "1px solid var(--border-subtle)" },
  evalName: { fontWeight: 600, fontSize: "var(--text-md)", color: "var(--text-strong)" },
  evalCourse: { fontSize: "var(--text-xs)", color: "var(--text-subtle)", marginTop: 2 },
  evalScore: { fontFamily: "var(--font-display)", fontWeight: 700, fontSize: "var(--text-lg)", color: "var(--text-strong)" },
};

window.ResultsScreen = ResultsScreen;
