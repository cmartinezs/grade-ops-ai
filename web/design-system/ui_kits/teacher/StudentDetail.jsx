// StudentDetail.jsx — shared student profile view + Breadcrumb + shared helpers
const { Avatar, Badge, Card, ProgressBar, RubricLevel, StatCard } = window.GradeOpsAIDesignSystem_fcd12b;
const { getEvals } = window.TEACHER_DATA;

// ── Shared helpers (exposed to sibling screens) ───────────────────────────────
function scoreColor(n) {
  return n < 4 ? "var(--danger-600)" : n >= 5.5 ? "var(--success-600)" : "var(--text-strong)";
}
function TrendIcon({ t, size }) {
  const sz = size || 16;
  return <Icon name={t==="up"?"trending-up":t==="down"?"trending-down":"minus"} size={sz}
    color={t==="up"?"var(--success-500)":t==="down"?"var(--danger-500)":"var(--text-subtle)"} />;
}

// ── Breadcrumb (used by both StudentsScreen and ReportsScreen) ────────────────
function Breadcrumb({ items, onNavigate }) {
  return (
    <nav style={{ display:"flex", alignItems:"center", gap:5, marginBottom:20, flexWrap:"wrap" }}>
      {items.map((item, i) => (
        <React.Fragment key={i}>
          {i < items.length - 1 ? (
            <button onClick={()=>onNavigate(i)} style={{
              border:"none", background:"none", cursor:"pointer", padding:0,
              fontSize:"var(--text-sm)", color:"var(--brand)", fontWeight:500,
              fontFamily:"var(--font-sans)", lineHeight:1,
            }}
            onMouseEnter={e=>e.currentTarget.style.textDecoration="underline"}
            onMouseLeave={e=>e.currentTarget.style.textDecoration="none"}>
              {item.label}
            </button>
          ) : (
            <span style={{ fontSize:"var(--text-sm)", color:"var(--text-strong)", fontWeight:600 }}>{item.label}</span>
          )}
          {i < items.length-1 && <Icon name="chevron-right" size={13} color="var(--text-subtle)" />}
        </React.Fragment>
      ))}
    </nav>
  );
}

// ── StudentDetail ─────────────────────────────────────────────────────────────
function StudentDetail({ student }) {
  const [activeSubject, setActiveSubject] = React.useState(()=>Object.keys(student.grades)[0]);
  const subjGrade = student.grades[activeSubject];
  const evals     = getEvals(student.id, activeSubject);
  const riskSubjs = Object.entries(student.grades).filter(([,g])=>g.risk);

  return (
    <div style={{ display:"flex", flexDirection:"column", gap:18 }}>

      {/* ── Profile header ── */}
      <Card>
        <div style={{ padding:"20px 24px", display:"flex", gap:20, alignItems:"center" }}>
          <Avatar name={student.name} size="lg" />
          <div style={{ flex:1 }}>
            <div style={{ fontFamily:"var(--font-display)", fontWeight:700, fontSize:"var(--text-2xl)", color:"var(--text-strong)", letterSpacing:"-0.02em" }}>{student.name}</div>
            <div style={{ fontSize:"var(--text-sm)", color:"var(--text-subtle)", marginTop:3 }}>{student.course} · {student.email}</div>
          </div>
          <div style={{ display:"flex", gap:14, alignItems:"center" }}>
            <StatCard label="Promedio general" value={student.avg.toFixed(1)}
              icon={<TrendIcon t={student.trend} size={18}/>}
              iconTone={student.trend==="up"?"success":student.trend==="down"?"danger":"info"} />
            {riskSubjs.length>0 && <Badge tone="danger" dot>{riskSubjs.length} asig. en riesgo</Badge>}
          </div>
        </div>
      </Card>

      {/* ── Layout: subject tabs (left) + eval table (right) ── */}
      <div style={{ display:"grid", gridTemplateColumns:"220px 1fr", gap:18, alignItems:"start" }}>

        {/* Subject selector */}
        <div style={{ display:"flex", flexDirection:"column", gap:8 }}>
          {Object.entries(student.grades).map(([subj, g]) => {
            const active = subj===activeSubject;
            return (
              <button key={subj} onClick={()=>setActiveSubject(subj)} style={{
                border:`2px solid ${active?"var(--brand)":"var(--border-subtle)"}`,
                borderRadius:"var(--radius-md)",
                background: active?"var(--surface-brand-soft)":"var(--surface-card)",
                padding:"12px 14px", cursor:"pointer", textAlign:"left",
                transition:"border-color 120ms, background 120ms",
              }}>
                <div style={{ display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:5 }}>
                  <span style={{ fontWeight:600, fontSize:"var(--text-sm)", color:"var(--text-strong)" }}>{subj}</span>
                  <TrendIcon t={g.trend} />
                </div>
                <div style={{ display:"flex", alignItems:"baseline", gap:6 }}>
                  <span style={{ fontFamily:"var(--font-display)", fontWeight:700, fontSize:"var(--text-2xl)", color:scoreColor(g.avg) }}>{g.avg.toFixed(1)}</span>
                  <span style={{ fontSize:"var(--text-xs)", color:"var(--text-subtle)" }}>{g.evals} eval.</span>
                </div>
                <div style={{ marginTop:8 }}>
                  <ProgressBar value={(g.avg/7)*100} tone={g.avg<4?"danger":g.avg>=5.5?"success":"brand"} />
                </div>
                {g.risk && <div style={{ marginTop:7 }}><Badge tone="danger" dot>En riesgo</Badge></div>}
              </button>
            );
          })}
        </div>

        {/* Evaluation table */}
        <Card>
          <Card.Header>
            <Card.Title>Evaluaciones · {activeSubject}</Card.Title>
            <div style={{ display:"flex", gap:8 }}>
              <Badge tone="info">{subjGrade.evals} realizadas</Badge>
              <Badge tone={subjGrade.avg<4?"danger":subjGrade.avg>=5.5?"success":"warning"}>
                Prom. {subjGrade.avg.toFixed(1)}
              </Badge>
            </div>
          </Card.Header>
          <div>
            {evals.length>0 ? evals.map((ev,i)=>(
              <div key={i} style={{ display:"flex", alignItems:"center", gap:14, padding:"12px 20px", borderBottom:i<evals.length-1?"1px solid var(--border-subtle)":"none" }}>
                <div style={{ flex:1 }}>
                  <div style={{ fontWeight:600, fontSize:"var(--text-sm)", color:"var(--text-strong)" }}>{ev.title}</div>
                  <div style={{ fontSize:"var(--text-xs)", color:"var(--text-subtle)", marginTop:2 }}>{ev.date}</div>
                </div>
                <RubricLevel level={ev.rubric} />
                <div style={{ fontFamily:"var(--font-display)", fontWeight:700, fontSize:"var(--text-xl)", color:scoreColor(ev.score), minWidth:36, textAlign:"right" }}>{ev.score.toFixed(1)}</div>
              </div>
            )) : (
              <p style={{ padding:"20px", color:"var(--text-subtle)", fontSize:"var(--text-sm)", margin:0 }}>Sin evaluaciones registradas.</p>
            )}
          </div>
        </Card>
      </div>
    </div>
  );
}

window.Breadcrumb    = Breadcrumb;
window.StudentDetail = StudentDetail;
window.scoreColor    = scoreColor;
window.TrendIcon     = TrendIcon;
