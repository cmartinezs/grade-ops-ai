// ReportsScreen.jsx — drill-down: Global → Curso → Asignatura → Detalle alumno
const { Card, StatCard, Badge, Button, ProgressBar, RubricLevel } = window.GradeOpsAIDesignSystem_fcd12b;
const { students, SUBJECTS_BY_COURSE, COURSES, subjectStats, _avg, _r1 } = window.TEACHER_DATA;

// ── Global View ───────────────────────────────────────────────────────────────
const WEAK_TOPICS = [
  { topic:"Derivadas implícitas",           course:"2°B", subject:"Matemática", pct:38, level:"beginning" },
  { topic:"Revolución Industrial · causas", course:"2°A", subject:"Historia",   pct:44, level:"beginning" },
  { topic:"Análisis literario",             course:"3°A", subject:"Lenguaje",   pct:51, level:"developing" },
  { topic:"Fotosíntesis fase lumínica",     course:"1°B", subject:"Biología",   pct:57, level:"developing" },
];

function RptGlobalView({ onCourse }) {
  const allAvg = _r1(_avg(students.map(s=>s.avg)));
  const riskCount = students.filter(s=>s.risk).length;

  return (
    <div style={{ display:"flex", flexDirection:"column", gap:22 }}>
      {/* KPIs */}
      <div style={{ display:"grid", gridTemplateColumns:"repeat(4,1fr)", gap:16 }}>
        <StatCard label="Promedio global" value={allAvg.toFixed(1)} delta="+0,2" icon={<Icon name="trending-up" size={19}/>}/>
        <StatCard label="Total estudiantes" value={students.length} icon={<Icon name="users" size={19}/>} iconTone="info"/>
        <StatCard label="Evaluaciones activas" value="6" icon={<Icon name="file-pen-line" size={19}/>} iconTone="gold"/>
        <StatCard label="En riesgo" value={riskCount} unit="alumnos" icon={<Icon name="alert-triangle" size={19}/>} iconTone="danger" delta="-1" deltaDir="down"/>
      </div>

      <div style={{ display:"grid", gridTemplateColumns:"1.4fr 1fr", gap:18, alignItems:"start" }}>
        {/* Course performance */}
        <Card>
          <Card.Header>
            <Card.Title>Rendimiento por curso</Card.Title>
            <Button variant="ghost" size="sm" iconLeft={<Icon name="download" size={15}/>}>Exportar PDF</Button>
          </Card.Header>
          <Card.Body>
            <div style={{ display:"flex", flexDirection:"column", gap:18 }}>
              {COURSES.map(c=>{
                const cs = students.filter(s=>s.course===c);
                const ca = cs.length?_r1(_avg(cs.map(s=>s.avg))):0;
                const cr = cs.filter(s=>s.risk).length;
                return (
                  <div key={c}>
                    <div style={{ display:"flex", alignItems:"baseline", justifyContent:"space-between", marginBottom:8 }}>
                      <div>
                        <button onClick={()=>onCourse(c)} style={{ fontWeight:700, fontSize:"var(--text-md)", color:"var(--brand)", background:"none", border:"none", cursor:"pointer", padding:0, fontFamily:"var(--font-sans)" }}
                          onMouseEnter={e=>e.currentTarget.style.textDecoration="underline"}
                          onMouseLeave={e=>e.currentTarget.style.textDecoration="none"}>
                          {c}
                        </button>
                        <span style={{ fontSize:"var(--text-xs)", color:"var(--text-subtle)", marginLeft:10 }}>
                          {cs.length} alumnos · {(SUBJECTS_BY_COURSE[c]||[]).length} asignaturas
                          {cr>0&&<> · <span style={{ color:"var(--danger-600)", fontWeight:600 }}>{cr} en riesgo</span></>}
                        </span>
                      </div>
                      <span style={{ fontFamily:"var(--font-display)", fontWeight:700, fontSize:"var(--text-xl)", color:scoreColor(ca) }}>{ca.toFixed(1)}</span>
                    </div>
                    <ProgressBar value={(ca/7)*100} tone={ca<4.5?"danger":ca>=5.5?"success":"brand"}/>
                  </div>
                );
              })}
            </div>
          </Card.Body>
        </Card>

        {/* Weak topics */}
        <Card>
          <Card.Header><Card.Title>Temas con menor logro</Card.Title></Card.Header>
          <Card.Body>
            <div style={{ display:"flex", flexDirection:"column", gap:18 }}>
              {WEAK_TOPICS.map((w,i)=>(
                <div key={i}>
                  <div style={{ display:"flex", alignItems:"center", justifyContent:"space-between", marginBottom:8 }}>
                    <div>
                      <div style={{ fontWeight:600, fontSize:"var(--text-sm)", color:"var(--text-strong)" }}>{w.topic}</div>
                      <div style={{ fontSize:"var(--text-xs)", color:"var(--text-subtle)", marginTop:1 }}>{w.course} · {w.subject}</div>
                    </div>
                    <RubricLevel level={w.level} solid/>
                  </div>
                  <ProgressBar value={w.pct} tone={w.pct<45?"danger":"gold"} showValue/>
                </div>
              ))}
            </div>
          </Card.Body>
        </Card>
      </div>
    </div>
  );
}

// ── Course View ───────────────────────────────────────────────────────────────
function RptCourseView({ course, onSubject, onStudent }) {
  const cs = students.filter(s=>s.course===course);
  const subjects = SUBJECTS_BY_COURSE[course]||[];
  const ca = cs.length?_r1(_avg(cs.map(s=>s.avg))):0;

  return (
    <div style={{ display:"flex", flexDirection:"column", gap:22 }}>
      {/* KPIs */}
      <div style={{ display:"grid", gridTemplateColumns:"repeat(4,1fr)", gap:14 }}>
        <StatCard label="Promedio curso" value={ca.toFixed(1)} icon={<Icon name="bar-chart-3" size={19}/>} iconTone="success"/>
        <StatCard label="Estudiantes" value={cs.length} icon={<Icon name="users" size={19}/>} iconTone="info"/>
        <StatCard label="Asignaturas" value={subjects.length} icon={<Icon name="book-open" size={19}/>}/>
        <StatCard label="En riesgo" value={cs.filter(s=>s.risk).length} unit="alumnos" icon={<Icon name="alert-triangle" size={19}/>} iconTone="danger"/>
      </div>

      {/* Subject cards */}
      <div style={{ display:"grid", gridTemplateColumns:`repeat(${subjects.length},1fr)`, gap:14 }}>
        {subjects.map(sub=>{
          const st = subjectStats(course, sub);
          if(!st) return null;
          return (
            <button key={sub} onClick={()=>onSubject(sub)} style={{
              border:"1px solid var(--border-subtle)", borderRadius:"var(--radius-md)",
              background:"var(--surface-card)", padding:"16px 18px", cursor:"pointer", textAlign:"left",
              transition:"box-shadow 140ms, border-color 140ms",
            }}
            onMouseEnter={e=>{e.currentTarget.style.borderColor="var(--brand)";e.currentTarget.style.boxShadow="var(--shadow-md)";}}
            onMouseLeave={e=>{e.currentTarget.style.borderColor="var(--border-subtle)";e.currentTarget.style.boxShadow="none";}}>
              <div style={{ display:"flex", justifyContent:"space-between" }}>
                <span style={{ fontWeight:700, fontSize:"var(--text-md)", color:"var(--text-strong)" }}>{sub}</span>
                {st.riskCount>0&&<Badge tone="danger" dot>{st.riskCount}</Badge>}
              </div>
              <div style={{ fontFamily:"var(--font-display)", fontWeight:700, fontSize:"var(--text-3xl)", color:scoreColor(st.avg), margin:"10px 0 6px" }}>{st.avg.toFixed(1)}</div>
              <div style={{ display:"flex", gap:12, fontSize:"var(--text-xs)", color:"var(--text-subtle)", marginBottom:10 }}>
                <span>Máx <b style={{color:"var(--success-600)"}}>{st.max.toFixed(1)}</b></span>
                <span>Mín <b style={{color:"var(--danger-600)"}}>{st.min.toFixed(1)}</b></span>
              </div>
              <ProgressBar value={(st.avg/7)*100} tone={st.avg<4?"danger":st.avg>=5.5?"success":"brand"}/>
            </button>
          );
        })}
      </div>

      {/* Per-subject breakdown table */}
      <Card>
        <Card.Header><Card.Title>Desglose por asignatura · {course}</Card.Title></Card.Header>
        <div style={{ display:"grid", gridTemplateColumns:"1fr 90px 80px 80px 80px 80px", gap:14, padding:"10px 20px", borderBottom:"1px solid var(--border-subtle)", background:"var(--surface-sunken)" }}>
          {["Asignatura","Promedio","Máx","Mín","Eval.","Riesgo"].map((h,i)=>(
            <span key={i} style={{ fontSize:"var(--text-xs)", fontWeight:700, color:"var(--text-subtle)", textTransform:"uppercase", letterSpacing:".05em", textAlign:i>0?"center":"left" }}>{h}</span>
          ))}
        </div>
        {subjects.map((sub,i)=>{
          const st = subjectStats(course,sub);
          if(!st) return null;
          return (
            <div key={sub} onClick={()=>onSubject(sub)}
              style={{ display:"grid", gridTemplateColumns:"1fr 90px 80px 80px 80px 80px", gap:14, alignItems:"center", padding:"13px 20px", borderBottom:i<subjects.length-1?"1px solid var(--border-subtle)":"none", cursor:"pointer", transition:"background 100ms" }}
              onMouseEnter={e=>e.currentTarget.style.background="var(--surface-sunken)"}
              onMouseLeave={e=>e.currentTarget.style.background="transparent"}>
              <span style={{ fontWeight:600, fontSize:"var(--text-md)", color:"var(--brand)" }}>{sub}</span>
              <div style={{ textAlign:"center", fontFamily:"var(--font-display)", fontWeight:700, fontSize:"var(--text-xl)", color:scoreColor(st.avg) }}>{st.avg.toFixed(1)}</div>
              <div style={{ textAlign:"center", fontWeight:600, color:"var(--success-600)" }}>{st.max.toFixed(1)}</div>
              <div style={{ textAlign:"center", fontWeight:600, color:"var(--danger-600)" }}>{st.min.toFixed(1)}</div>
              <div style={{ textAlign:"center", color:"var(--text-muted)" }}>{st.totalEvals}</div>
              <div style={{ textAlign:"center" }}>{st.riskCount>0?<Badge tone="danger" dot>{st.riskCount}</Badge>:<span style={{color:"var(--text-subtle)"}}>—</span>}</div>
            </div>
          );
        })}
      </Card>
    </div>
  );
}

// ── Subject View ──────────────────────────────────────────────────────────────
function RptSubjectView({ course, subject, onStudent }) {
  const st = subjectStats(course, subject);
  if(!st) return null;
  const sorted = [...st.students].sort((a,b)=>b.grades[subject].avg-a.grades[subject].avg);
  const weakTopics = WEAK_TOPICS.filter(w=>w.course===course&&w.subject===subject);

  return (
    <div style={{ display:"flex", flexDirection:"column", gap:22 }}>
      {/* KPIs */}
      <div style={{ display:"grid", gridTemplateColumns:"repeat(4,1fr)", gap:14 }}>
        <StatCard label="Promedio" value={st.avg.toFixed(1)} icon={<Icon name="bar-chart-3" size={19}/>} iconTone="success"/>
        <StatCard label="Nota más alta" value={st.max.toFixed(1)} icon={<Icon name="trending-up" size={19}/>} iconTone="info"/>
        <StatCard label="Nota más baja" value={st.min.toFixed(1)} icon={<Icon name="trending-down" size={19}/>} iconTone="danger"/>
        <StatCard label="En riesgo" value={st.riskCount} unit="alumnos" icon={<Icon name="alert-triangle" size={19}/>} iconTone="danger"/>
      </div>

      <div style={{ display:"grid", gridTemplateColumns:weakTopics.length?"1.6fr 1fr":"1fr", gap:18, alignItems:"start" }}>
        {/* Student ranking */}
        <Card>
          <Card.Header>
            <Card.Title>Ranking · {course} {subject}</Card.Title>
            <Badge tone="info">{st.totalEvals} evaluaciones</Badge>
          </Card.Header>
          <div style={{ display:"grid", gridTemplateColumns:"36px 1fr 130px 70px 80px 90px", gap:12, padding:"10px 20px", borderBottom:"1px solid var(--border-subtle)", background:"var(--surface-sunken)" }}>
            {["#","Estudiante","Progreso","Nota","Tendencia",""].map((h,i)=>(
              <span key={i} style={{ fontSize:"var(--text-xs)", fontWeight:700, color:"var(--text-subtle)", textTransform:"uppercase", letterSpacing:".05em", textAlign:i>1&&i<5?"center":"left" }}>{h}</span>
            ))}
          </div>
          {sorted.map((s,i)=>{
            const g = s.grades[subject];
            return (
              <div key={s.id} onClick={()=>onStudent(s)}
                style={{ display:"grid", gridTemplateColumns:"36px 1fr 130px 70px 80px 90px", gap:12, alignItems:"center", padding:"12px 20px", borderBottom:i<sorted.length-1?"1px solid var(--border-subtle)":"none", cursor:"pointer", transition:"background 100ms" }}
                onMouseEnter={e=>e.currentTarget.style.background="var(--surface-sunken)"}
                onMouseLeave={e=>e.currentTarget.style.background="transparent"}>
                <div style={{ fontFamily:"var(--font-display)", fontWeight:700, fontSize:"var(--text-lg)", color:i===0?"var(--gold-600)":i===1?"var(--slate-400)":i===2?"var(--brand-soft-fg, var(--brand))":"var(--text-subtle)", textAlign:"center" }}>{i+1}</div>
                <div style={{ display:"flex", alignItems:"center", gap:10 }}>
                  <div style={{ width:28, height:28, borderRadius:"50%", background:"var(--surface-sunken)", border:"1px solid var(--border-subtle)", display:"flex", alignItems:"center", justifyContent:"center", fontSize:11, fontWeight:700, color:"var(--text-muted)", flexShrink:0 }}>
                    {s.name.split(" ").map(n=>n[0]).join("").slice(0,2)}
                  </div>
                  <div>
                    <div style={{ fontWeight:600, fontSize:"var(--text-sm)", color:"var(--text-strong)" }}>{s.name}</div>
                    <div style={{ fontSize:"var(--text-xs)", color:"var(--text-subtle)" }}>{g.evals} eval.</div>
                  </div>
                </div>
                <ProgressBar value={(g.avg/7)*100} tone={g.avg<4?"danger":g.avg>=5.5?"success":"brand"}/>
                <div style={{ textAlign:"center", fontFamily:"var(--font-display)", fontWeight:700, fontSize:"var(--text-xl)", color:scoreColor(g.avg) }}>{g.avg.toFixed(1)}</div>
                <div style={{ display:"flex", justifyContent:"center" }}><TrendIcon t={g.trend}/></div>
                <div>{g.risk?<Badge tone="danger" dot>En riesgo</Badge>:<span style={{fontSize:"var(--text-xs)",color:"var(--text-subtle)"}}>—</span>}</div>
              </div>
            );
          })}
        </Card>

        {/* Weak topics (if any) */}
        {weakTopics.length>0&&(
          <Card>
            <Card.Header><Card.Title>Temas con menor logro</Card.Title></Card.Header>
            <Card.Body>
              <div style={{ display:"flex", flexDirection:"column", gap:18 }}>
                {weakTopics.map((w,i)=>(
                  <div key={i}>
                    <div style={{ display:"flex", alignItems:"center", justifyContent:"space-between", marginBottom:8 }}>
                      <div style={{ fontWeight:600, fontSize:"var(--text-sm)", color:"var(--text-strong)" }}>{w.topic}</div>
                      <RubricLevel level={w.level} solid/>
                    </div>
                    <ProgressBar value={w.pct} tone={w.pct<45?"danger":"gold"} showValue/>
                  </div>
                ))}
              </div>
            </Card.Body>
          </Card>
        )}
      </div>
    </div>
  );
}

// ── Orchestrator ──────────────────────────────────────────────────────────────
function ReportsScreen() {
  const [view, setView] = React.useState({ level:"global" });

  const crumbs = [{ label:"Reportes" }];
  if(view.course)  crumbs.push({ label: view.course });
  if(view.subject) crumbs.push({ label: view.subject });
  if(view.student) crumbs.push({ label: view.student.name });

  const goTo = (idx) => {
    if(idx===0) setView({ level:"global" });
    if(idx===1) setView({ level:"course", course:view.course });
    if(idx===2) setView({ level:"subject", course:view.course, subject:view.subject });
  };

  return (
    <div style={{ maxWidth:"var(--content-max)", display:"flex", flexDirection:"column", gap:0 }}>
      {crumbs.length>1 && <Breadcrumb items={crumbs} onNavigate={goTo}/>}

      {view.level==="global"  && <RptGlobalView
        onCourse={c=>setView({level:"course",course:c})}/>}

      {view.level==="course"  && <RptCourseView
        course={view.course}
        onSubject={sub=>setView({level:"subject",course:view.course,subject:sub})}
        onStudent={s=>setView({level:"student",student:s,course:view.course})}/>}

      {view.level==="subject" && <RptSubjectView
        course={view.course} subject={view.subject}
        onStudent={s=>setView({level:"student",student:s,course:view.course,subject:view.subject})}/>}

      {view.level==="student" && <StudentDetail student={view.student}/>}
    </div>
  );
}

window.ReportsScreen = ReportsScreen;
