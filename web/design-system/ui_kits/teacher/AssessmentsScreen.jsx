// AssessmentsScreen.jsx — full evaluation list with filters, progress and actions.
const { Badge, Button, Input, Select, Tabs, Card, ProgressBar, IconButton } = window.GradeOpsAIDesignSystem_fcd12b;

const EVALS = [
  { id:1, title:"Prueba Unidad 3 — Fotosíntesis",     subject:"Biología",   course:"1°B", type:"Cerrada", status:"corregida",   done:32, total:32, avg:"5,4", due:"5 jun",       shared:true  },
  { id:2, title:"Ensayo: Revolución Industrial",       subject:"Historia",   course:"2°A", type:"Abierta", status:"en_revision", done:14, total:28, avg:null,  due:"Hoy 23:59",   shared:false },
  { id:3, title:"Control — Álgebra lineal",            subject:"Matemática", course:"3°A", type:"Mixta",   status:"en_curso",    done:8,  total:25, avg:null,  due:"20 jun",       shared:false },
  { id:4, title:"Prueba diagnóstica — Lenguaje",       subject:"Lenguaje",   course:"1°B", type:"Cerrada", status:"corregida",   done:30, total:30, avg:"4,9", due:"1 jun",        shared:false },
  { id:5, title:"Informe de laboratorio: Células",     subject:"Biología",   course:"2°A", type:"Abierta", status:"borrador",    done:0,  total:0,  avg:null,  due:"—",            shared:false },
  { id:6, title:"Quiz: Funciones cuadráticas",         subject:"Matemática", course:"2°B", type:"Cerrada", status:"corrigiendo", done:18, total:31, avg:null,  due:"Hoy 18:00",   shared:true  },
];
const ST = { corregida:{tone:"success",label:"Corregida"}, en_revision:{tone:"warning",label:"En revisión"}, en_curso:{tone:"info",label:"En curso"}, corrigiendo:{tone:"info",label:"Corrigiendo"}, borrador:{tone:"neutral",label:"Borrador"} };
const TT = { Cerrada:"brand", Abierta:"gold", Mixta:"info" };
const TYPE_MAP = { open:"Abierta", closed:"Cerrada", mixed:"Mixta" };

function AssessmentsScreen({ onGrade }) {
  const [tab, setTab] = React.useState("all");
  const rows = tab === "all" ? EVALS : EVALS.filter(e => e.type === TYPE_MAP[tab]);

  return (
    <div style={{ maxWidth:"var(--content-max)", display:"flex", flexDirection:"column", gap:18 }}>
      <div style={{ display:"flex", alignItems:"center", gap:12, flexWrap:"wrap" }}>
        <Tabs value={tab} onChange={setTab} tabs={[
          { id:"all", label:"Todas", count:EVALS.length },
          { id:"open", label:"Abiertas" },
          { id:"closed", label:"Cerradas" },
          { id:"mixed", label:"Mixtas" },
        ]} />
        <div style={{ flex:1 }} />
        <Input icon={<Icon name="search" size={17} />} placeholder="Buscar evaluación…" size="sm" style={{ width:200 }} />
        <Select size="sm" style={{ width:160 }}>
          <option>Todos los cursos</option>
          <option>1°B</option><option>2°A</option><option>3°A</option><option>2°B</option>
        </Select>
      </div>

      <Card>
        {rows.map((e, i) => {
          const st = ST[e.status];
          const pct = e.total > 0 ? Math.round((e.done / e.total) * 100) : 0;
          return (
            <div key={e.id} style={{ display:"grid", gridTemplateColumns:"1fr 140px 88px 180px", gap:16, alignItems:"center", padding:"14px 20px", borderBottom: i < rows.length-1 ? "1px solid var(--border-subtle)" : "none" }}>
              <div style={{ minWidth:0 }}>
                <div style={{ display:"flex", alignItems:"center", gap:8, marginBottom:4 }}>
                  <span style={{ fontWeight:600, fontSize:"var(--text-md)", color:"var(--text-strong)", whiteSpace:"nowrap", overflow:"hidden", textOverflow:"ellipsis" }}>{e.title}</span>
                  {e.shared && <Icon name="globe" size={13} color="var(--gold-600)" />}
                </div>
                <div style={{ display:"flex", alignItems:"center", gap:8, fontSize:"var(--text-sm)", color:"var(--text-muted)" }}>
                  <Badge tone={TT[e.type]}>{e.type}</Badge>
                  <span>{e.subject} · {e.course}</span>
                  <span style={{ color: e.due.includes("Hoy") ? "var(--warning-600)" : "inherit" }}>· {e.due}</span>
                </div>
              </div>
              <div>
                {e.total > 0
                  ? <ProgressBar value={pct} label={`${e.done}/${e.total}`} showValue />
                  : <span style={{ fontSize:"var(--text-xs)", color:"var(--text-subtle)" }}>Sin entregas</span>}
              </div>
              <div style={{ textAlign:"center" }}>
                {e.avg
                  ? <><div style={{ fontFamily:"var(--font-display)", fontWeight:700, fontSize:"var(--text-xl)", color:"var(--text-strong)" }}>{e.avg}</div><div style={{ fontSize:"var(--text-xs)", color:"var(--text-subtle)" }}>promedio</div></>
                  : <span style={{ fontSize:"var(--text-xs)", color:"var(--text-subtle)" }}>—</span>}
              </div>
              <div style={{ display:"flex", alignItems:"center", gap:8, justifyContent:"flex-end" }}>
                <Badge tone={st.tone} dot>{st.label}</Badge>
                {e.status !== "borrador"
                  ? <IconButton label="Ver / Corregir" size="sm" onClick={onGrade}><Icon name="arrow-right" size={15} /></IconButton>
                  : <IconButton label="Editar borrador" size="sm"><Icon name="pencil" size={15} /></IconButton>}
              </div>
            </div>
          );
        })}
      </Card>
    </div>
  );
}
window.AssessmentsScreen = AssessmentsScreen;
