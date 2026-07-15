// Assessment builder: settings + question bank picker (closed-question example).
const { Card, Badge, Button, Input, Select, Field, Switch, Checkbox, Tabs, IconButton, Radio } = window.GradeOpsAIDesignSystem_fcd12b;

const BANK = [
  { q: "¿Cuál es el principal producto de la fase lumínica?", type: "Única", scope: "global", topic: "Fotosíntesis", used: 124, added: true },
  { q: "Selecciona las moléculas producidas en el ciclo de Calvin.", type: "Múltiple", scope: "personal", topic: "Fotosíntesis", used: 18, added: true },
  { q: "La clorofila absorbe principalmente luz verde. (V/F)", type: "V/F", scope: "global", topic: "Pigmentos", used: 89, added: false },
  { q: "¿En qué organelo ocurre la fotosíntesis?", type: "Única", scope: "global", topic: "Célula", used: 203, added: false },
  { q: "Indica las etapas correctas de la respiración celular.", type: "Múltiple", scope: "personal", topic: "Metabolismo", used: 7, added: false },
];

function BuilderScreen({ onCancel, onSave }) {
  const [bankTab, setBankTab] = React.useState("global");
  const [added, setAdded] = React.useState(() => BANK.filter((b) => b.added).length);

  return (
    <div style={{ display: "grid", gridTemplateColumns: "1fr 360px", gap: 18, maxWidth: "var(--content-max)", alignItems: "start" }}>
      <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
        <Card>
          <Card.Header><Card.Title>Detalles de la evaluación</Card.Title><Badge tone="brand">Cerrada</Badge></Card.Header>
          <Card.Body>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
              <Field label="Título" htmlFor="t" required><Input id="t" defaultValue="Prueba Unidad 3 — Fotosíntesis" /></Field>
              <Field label="Curso / Asignatura" htmlFor="c"><Select id="c" defaultValue="bio"><option value="bio">1°B · Biología</option><option value="his">2°A · Historia</option></Select></Field>
              <Field label="Inicio" htmlFor="d1"><Input id="d1" type="datetime-local" defaultValue="2026-06-20T08:30" /></Field>
              <Field label="Cierre" htmlFor="d2"><Input id="d2" type="datetime-local" defaultValue="2026-06-20T09:30" /></Field>
            </div>
            <div style={{ display: "flex", flexDirection: "column", gap: 12, marginTop: 16, paddingTop: 16, borderTop: "1px solid var(--border-subtle)" }}>
              <Switch label="Permitir respuestas fuera de tiempo" defaultChecked />
              <Switch label="Penalizar entregas atrasadas (−10%)" />
              <Switch label="Compartir en el banco global al publicar" />
            </div>
          </Card.Body>
        </Card>

        <Card>
          <Card.Header>
            <Card.Title>Preguntas seleccionadas</Card.Title>
            <Badge tone="success" dot>{added} preguntas · 60 pts</Badge>
          </Card.Header>
          <Card.Body>
            <div style={builderStyles.empty}>
              <Icon name="list-checks" size={22} color="var(--brand)" />
              <span>Arrastra desde el banco o usa <b>+</b> para añadir. La IA corrige con la clave de respuestas.</span>
            </div>
          </Card.Body>
        </Card>
      </div>

      <Card>
        <Card.Header><Card.Title>Banco de preguntas</Card.Title></Card.Header>
        <div style={{ padding: "12px 16px 0" }}>
          <Input icon={<Icon name="search" size={17} />} placeholder="Buscar por tema o enunciado…" size="sm" />
          <div style={{ margin: "12px 0 6px" }}>
            <Tabs value={bankTab} onChange={setBankTab} variant="pills" tabs={[{ id: "global", label: "Global" }, { id: "personal", label: "Personal" }]} />
          </div>
        </div>
        <div style={{ maxHeight: 340, overflow: "auto", padding: "4px 10px 12px" }}>
          {BANK.filter((b) => b.scope === bankTab).map((b, i) => (
            <div key={i} style={builderStyles.bankItem}>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={builderStyles.bankQ}>{b.q}</div>
                <div style={builderStyles.bankMeta}>
                  <Badge tone="neutral">{b.type}</Badge>
                  <span>{b.topic}</span><span>·</span><span>{b.used} usos</span>
                </div>
              </div>
              <IconButton label={b.added ? "Quitar" : "Añadir"} variant={b.added ? "ghost" : "outline"} size="sm">
                <Icon name={b.added ? "check" : "plus"} size={16} color={b.added ? "var(--success-600)" : undefined} />
              </IconButton>
            </div>
          ))}
        </div>
        <Card.Footer>
          <Button variant="secondary" size="sm" block iconLeft={<Icon name="sparkles" size={15} />}>Generar preguntas con IA</Button>
        </Card.Footer>
      </Card>

      <div style={{ gridColumn: "1 / -1", display: "flex", justifyContent: "flex-end", gap: 10 }}>
        <Button variant="ghost" onClick={onCancel}>Cancelar</Button>
        <Button variant="secondary">Vista previa</Button>
        <Button variant="primary" iconLeft={<Icon name="check" size={16} />} onClick={onSave}>Guardar evaluación</Button>
      </div>
    </div>
  );
}

const builderStyles = {
  empty: { display: "flex", alignItems: "center", gap: 12, padding: "16px", background: "var(--surface-brand-soft)", border: "1px dashed var(--sprout-300)", borderRadius: "var(--radius-md)", fontSize: "var(--text-sm)", color: "var(--text-muted)", lineHeight: 1.5 },
  bankItem: { display: "flex", alignItems: "center", gap: 10, padding: "11px 8px", borderBottom: "1px solid var(--border-subtle)" },
  bankQ: { fontWeight: 500, fontSize: "var(--text-sm)", color: "var(--text-strong)", lineHeight: 1.4, marginBottom: 5 },
  bankMeta: { display: "flex", alignItems: "center", gap: 7, fontSize: "var(--text-xs)", color: "var(--text-subtle)" },
};

window.BuilderScreen = BuilderScreen;
