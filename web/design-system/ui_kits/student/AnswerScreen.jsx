// Student answering a closed/mixed assessment via magic link.
const { Card, Badge, Button, Radio, Checkbox, Textarea, ProgressBar } = window.GradeOpsAIDesignSystem_fcd12b;

const QUESTIONS = [
  { kind: "single", text: "¿Cuál es el principal producto energético de la fase lumínica?",
    opts: ["Glucosa", "ATP y NADPH", "Dióxido de carbono", "Oxígeno molecular"], pts: 10 },
  { kind: "multi", text: "Selecciona las moléculas producidas en el ciclo de Calvin.",
    opts: ["G3P", "ATP (consumido)", "Glucosa", "Clorofila"], pts: 15 },
  { kind: "tf", text: "La clorofila absorbe principalmente la luz de color verde.",
    opts: ["Verdadero", "Falso"], pts: 5 },
  { kind: "open", text: "Explica con tus palabras por qué la fotosíntesis es clave para los ecosistemas.", pts: 20 },
];

function AnswerScreen({ onSubmit }) {
  const [answers, setAnswers] = React.useState({});
  const answered = Object.keys(answers).length;
  const pct = Math.round((answered / QUESTIONS.length) * 100);

  return (
    <div style={ansStyles.page}>
      <StudentBar right={<Badge tone="info" dot>En curso · cierra 09:30</Badge>} />
      <div style={ansStyles.scroll}>
        <div style={ansStyles.wrap}>
          <div style={ansStyles.hero}>
            <Badge tone="brand">Mixta</Badge>
            <h1 style={ansStyles.title}>Prueba Unidad 3 — Fotosíntesis</h1>
            <p style={ansStyles.meta}>2°A Biología · Prof. Paula Méndez · 4 preguntas · 50 puntos</p>
            <div style={{ marginTop: 16 }}>
              <ProgressBar value={answered} max={QUESTIONS.length} label="Progreso" showValue striped={answered < QUESTIONS.length} tone={pct === 100 ? "success" : "brand"} />
            </div>
          </div>

          {QUESTIONS.map((q, i) => (
            <Card key={i} style={{ marginBottom: 16 }}>
              <Card.Body>
                <div style={ansStyles.qhead}>
                  <span style={ansStyles.qnum}>{i + 1}</span>
                  <div style={{ flex: 1 }}>
                    <div style={ansStyles.qtext}>{q.text}</div>
                    <div style={ansStyles.qpts}>{q.pts} pts · {({ single: "Alternativa única", multi: "Selección múltiple", tf: "Verdadero / Falso", open: "Respuesta abierta" })[q.kind]}</div>
                  </div>
                </div>
                <div style={{ display: "flex", flexDirection: "column", gap: 10, marginTop: 14, paddingLeft: 42 }}>
                  {q.kind === "open" ? (
                    <Textarea placeholder="Escribe tu respuesta…" onChange={(e) => setAnswers((a) => ({ ...a, [i]: e.target.value || undefined }))} />
                  ) : q.kind === "multi" ? (
                    q.opts.map((o, j) => <Checkbox key={j} label={o} onChange={() => setAnswers((a) => ({ ...a, [i]: true }))} />)
                  ) : (
                    q.opts.map((o, j) => <Radio key={j} name={"q" + i} label={o} onChange={() => setAnswers((a) => ({ ...a, [i]: true }))} />)
                  )}
                </div>
              </Card.Body>
            </Card>
          ))}

          <div style={ansStyles.footer}>
            <span style={{ fontSize: "var(--text-sm)", color: "var(--text-muted)", display: "flex", alignItems: "center", gap: 7 }}>
              <Icon name="save" size={15} color="var(--text-subtle)" /> Guardado automático
            </span>
            <Button size="lg" iconRight={<Icon name="send" size={18} />} onClick={onSubmit}>Enviar respuestas</Button>
          </div>
        </div>
      </div>
    </div>
  );
}

const ansStyles = {
  page: { display: "flex", flexDirection: "column", height: "100%", background: "var(--surface-page)", fontFamily: "var(--font-sans)" },
  scroll: { flex: 1, overflow: "auto", padding: "28px 20px 48px" },
  wrap: { maxWidth: 720, margin: "0 auto" },
  hero: { marginBottom: 22 },
  title: { fontFamily: "var(--font-display)", fontWeight: 700, fontSize: "var(--text-3xl)", color: "var(--text-strong)", margin: "12px 0 6px", letterSpacing: "-0.02em" },
  meta: { margin: 0, color: "var(--text-muted)", fontSize: "var(--text-md)" },
  qhead: { display: "flex", gap: 12, alignItems: "flex-start" },
  qnum: { flex: "none", width: 30, height: 30, borderRadius: "var(--radius-sm)", background: "var(--surface-brand-soft)", color: "var(--brand-hover)", fontFamily: "var(--font-display)", fontWeight: 700, display: "flex", alignItems: "center", justifyContent: "center", fontSize: "var(--text-md)" },
  qtext: { fontWeight: 600, fontSize: "var(--text-lg)", color: "var(--text-strong)", lineHeight: 1.35 },
  qpts: { fontSize: "var(--text-xs)", color: "var(--text-subtle)", marginTop: 4, fontFamily: "var(--font-mono)" },
  footer: { display: "flex", justifyContent: "space-between", alignItems: "center", marginTop: 24 },
};

window.AnswerScreen = AnswerScreen;
