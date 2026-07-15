// BankScreen.jsx — standalone question bank browser with detail panel.
const { Badge, Button, Input, Select, Tabs, Card, IconButton } = window.GradeOpsAIDesignSystem_fcd12b;

const QUESTIONS = [
  { id:"q1", text:"¿Cuál es el principal producto energético de la fase lumínica?", type:"Única",    subject:"Biología",   unit:"Unidad 3", topic:"Fotosíntesis", scope:"global",   uses:124 },
  { id:"q2", text:"La clorofila absorbe principalmente luz verde.",                  type:"V/F",      subject:"Biología",   unit:"Unidad 3", topic:"Pigmentos",    scope:"global",   uses:89  },
  { id:"q3", text:"Selecciona las moléculas producidas en el ciclo de Calvin.",     type:"Múltiple", subject:"Biología",   unit:"Unidad 3", topic:"Fotosíntesis", scope:"personal", uses:18  },
  { id:"q4", text:"¿En qué organelo ocurre la fotosíntesis?",                      type:"Única",    subject:"Biología",   unit:"Unidad 2", topic:"Célula",       scope:"global",   uses:203 },
  { id:"q5", text:"Indica los factores que afectan la tasa de fotosíntesis.",      type:"Múltiple", subject:"Biología",   unit:"Unidad 3", topic:"Fotosíntesis", scope:"personal", uses:7   },
  { id:"q6", text:"Analiza la evolución del protagonista a lo largo del relato.",  type:"Abierta",  subject:"Lenguaje",   unit:"Unidad 1", topic:"Narrativa",    scope:"personal", uses:3   },
  { id:"q7", text:"¿Cuál es la derivada de f(x) = x² + 3x?",                     type:"Única",    subject:"Matemática", unit:"Unidad 2", topic:"Derivadas",    scope:"global",   uses:56  },
  { id:"q8", text:"Determina si los siguientes triángulos son semejantes.",        type:"Múltiple", subject:"Matemática", unit:"Unidad 1", topic:"Geometría",    scope:"global",   uses:41  },
];
const TT = { "Única":"brand", "Múltiple":"info", "V/F":"neutral", "Abierta":"gold" };

function BankScreen() {
  const [bankTab, setBankTab] = React.useState("global");
  const [subject, setSubject] = React.useState("all");
  const [sel, setSel] = React.useState(null);
  const questions = QUESTIONS.filter(q => q.scope === bankTab && (subject === "all" || q.subject === subject));
  const detail = sel ? QUESTIONS.find(q => q.id === sel) : null;

  return (
    <div style={{ maxWidth:"var(--content-max)", display:"flex", flexDirection:"column", gap:18 }}>
      <div style={{ display:"flex", alignItems:"center", gap:12, flexWrap:"wrap" }}>
        <Tabs value={bankTab} onChange={v => { setBankTab(v); setSel(null); }} variant="pills"
          tabs={[{ id:"global", label:"Banco global" }, { id:"personal", label:"Mis preguntas" }]} />
        <div style={{ flex:1 }} />
        <Input icon={<Icon name="search" size={17} />} placeholder="Buscar pregunta…" size="sm" style={{ width:220 }} />
        <Select size="sm" style={{ width:180 }} value={subject} onChange={e => setSubject(e.target.value)}>
          <option value="all">Todas las materias</option>
          <option value="Biología">Biología</option>
          <option value="Lenguaje">Lenguaje</option>
          <option value="Matemática">Matemática</option>
        </Select>
        <Button variant="secondary" size="sm" iconLeft={<Icon name="plus" size={15} />}>Nueva pregunta</Button>
      </div>

      <div style={{ display:"grid", gridTemplateColumns: detail ? "1fr 300px" : "1fr", gap:16, alignItems:"start" }}>
        <Card>
          {questions.length === 0 && (
            <div style={{ padding:40, textAlign:"center", color:"var(--text-subtle)", fontSize:"var(--text-sm)" }}>No hay preguntas que coincidan con los filtros.</div>
          )}
          {questions.map((q, i) => (
            <button key={q.id} onClick={() => setSel(sel === q.id ? null : q.id)}
              style={{ width:"100%", display:"flex", alignItems:"flex-start", gap:14, padding:"14px 18px", border:"none", borderBottom: i < questions.length-1 ? "1px solid var(--border-subtle)" : "none", background: sel===q.id ? "var(--surface-brand-soft)" : "transparent", cursor:"pointer", fontFamily:"inherit", textAlign:"left", transition:"background 120ms" }}
              onMouseEnter={e => { if(sel!==q.id) e.currentTarget.style.background="var(--surface-sunken)"; }}
              onMouseLeave={e => { if(sel!==q.id) e.currentTarget.style.background="transparent"; }}>
              <div style={{ flex:1, minWidth:0 }}>
                <p style={{ margin:"0 0 7px", fontWeight:500, fontSize:"var(--text-sm)", color:"var(--text-strong)", lineHeight:1.45 }}>{q.text}</p>
                <div style={{ display:"flex", alignItems:"center", gap:7, flexWrap:"wrap" }}>
                  <Badge tone={TT[q.type]}>{q.type}</Badge>
                  <span style={{ fontSize:"var(--text-xs)", color:"var(--text-subtle)" }}>{q.subject} · {q.unit} · {q.topic}</span>
                  <span style={{ fontSize:"var(--text-xs)", color:"var(--text-subtle)" }}>· {q.uses} usos</span>
                </div>
              </div>
              <Icon name="chevron-right" size={16} color={sel===q.id ? "var(--brand)" : "var(--text-subtle)"} />
            </button>
          ))}
          {bankTab === "personal" && (
            <div style={{ padding:"12px 18px", borderTop:"1px solid var(--border-subtle)" }}>
              <Button variant="secondary" size="sm" block iconLeft={<Icon name="sparkles" size={15} />}>Generar preguntas con IA</Button>
            </div>
          )}
        </Card>

        {detail && (
          <Card accent>
            <Card.Header>
              <Card.Title>Detalle</Card.Title>
              <IconButton label="Cerrar" size="sm" onClick={() => setSel(null)}><Icon name="x" size={15} /></IconButton>
            </Card.Header>
            <Card.Body>
              <div style={{ display:"flex", gap:8, marginBottom:12 }}>
                <Badge tone={TT[detail.type]}>{detail.type}</Badge>
                <Badge tone={detail.scope==="global" ? "gold" : "neutral"}>{detail.scope==="global" ? "Global" : "Personal"}</Badge>
              </div>
              <p style={{ fontWeight:600, fontSize:"var(--text-md)", color:"var(--text-strong)", lineHeight:1.5, margin:"0 0 14px" }}>{detail.text}</p>
              <div style={{ fontSize:"var(--text-xs)", color:"var(--text-muted)", lineHeight:2, borderTop:"1px solid var(--border-subtle)", paddingTop:12 }}>
                <div><b>Materia:</b> {detail.subject}</div>
                <div><b>Unidad:</b> {detail.unit}</div>
                <div><b>Temática:</b> {detail.topic}</div>
                <div><b>Usos en evaluaciones:</b> {detail.uses}</div>
              </div>
            </Card.Body>
            <Card.Footer>
              <div style={{ display:"flex", flexDirection:"column", gap:8 }}>
                <Button size="sm" block iconLeft={<Icon name="file-pen-line" size={15} />}>Añadir a evaluación</Button>
                <Button variant="secondary" size="sm" block iconLeft={<Icon name="pencil" size={15} />}>Editar pregunta</Button>
              </div>
            </Card.Footer>
          </Card>
        )}
      </div>
    </div>
  );
}
window.BankScreen = BankScreen;
