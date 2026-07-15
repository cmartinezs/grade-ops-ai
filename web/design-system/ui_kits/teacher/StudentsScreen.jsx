// StudentsScreen.jsx — drill-down: Global → Curso → Asignatura → Detalle alumno
const { Avatar, Badge, Button, Card, Select, ProgressBar, StatCard } = window.GradeOpsAIDesignSystem_fcd12b;
const { students, SUBJECTS_BY_COURSE, COURSES, subjectStats, _avg, _r1 } = window.TEACHER_DATA;

const HDR = { fontSize:"var(--text-xs)", fontWeight:700, color:"var(--text-subtle)", textTransform:"uppercase", letterSpacing:".05em" };

// ── Global view ───────────────────────────────────────────────────────────────
function StuGlobalView({ onCourse, onStudent }) {
  const [filterCourse, setFilterCourse] = React.useState("all");
  const list = filterCourse==="all" ? students : students.filter(s=>s.course===filterCourse);

  return (
    <div style={{ display:"flex", flexDirection:"column", gap:18 }}>
      {/* KPIs */}
      <div style={{ display:"grid", gridTemplateColumns:"repeat(4,1fr)", gap:14 }}>
        <StatCard label="Total estudiantes" value={students.length} icon={<Icon name="users" size={19}/>} iconTone="info"/>
        <StatCard label="Cursos activos" value={COURSES.length} icon={<Icon name="book-open" size={19}/>}/>
        <StatCard label="Promedio general" value={_r1(_avg(students.map(s=>s.avg))).toFixed(1)} icon={<Icon name="trending-up" size={19}/>} iconTone="success"/>
        <StatCard label="En riesgo" value={students.filter(s=>s.risk).length} unit="alumnos" icon={<Icon name="alert-triangle" size={19}/>} iconTone="danger"/>
      </div>

      {/* Course cards */}
      <div style={{ display:"grid", gridTemplateColumns:"repeat(4,1fr)", gap:14 }}>
        {COURSES.map(c=>{
          const cs = students.filter(s=>s.course===c);
          const ca = cs.length ? _r1(_avg(cs.map(s=>s.avg))) : 0;
          const cr = cs.filter(s=>s.risk).length;
          return (
            <button key={c} onClick={()=>onCourse(c)} style={{
              border:"1px solid var(--border-subtle)", borderRadius:"var(--radius-md)",
              background:"var(--surface-card)", padding:"16px 18px",
              cursor:"pointer", textAlign:"left", transition:"box-shadow 140ms, border-color 140ms",
            }}
            onMouseEnter={e=>{e.currentTarget.style.borderColor="var(--brand)";e.currentTarget.style.boxShadow="var(--shadow-md)";}}
            onMouseLeave={e=>{e.currentTarget.style.borderColor="var(--border-subtle)";e.currentTarget.style.boxShadow="none";}}>
              <div style={{ display:"flex", justifyContent:"space-between", alignItems:"flex-start" }}>
                <span style={{ fontFamily:"var(--font-display)", fontWeight:700, fontSize:"var(--text-xl)", color:"var(--text-strong)" }}>{c}</span>
                {cr>0&&<Badge tone="danger" dot>{cr}</Badge>}
              </div>
              <div style={{ fontFamily:"var(--font-display)", fontWeight:700, fontSize:"var(--text-3xl)", color:scoreColor(ca), margin:"10px 0 6px" }}>{ca.toFixed(1)}</div>
              <div style={{ fontSize:"var(--text-xs)", color:"var(--text-subtle)", marginBottom:10 }}>{cs.length} estudiantes · {(SUBJECTS_BY_COURSE[c]||[]).length} asignaturas</div>
              <ProgressBar value={(ca/7)*100} tone={ca<4?"danger":ca>=5.5?"success":"brand"}/>
              <div style={{ display:"flex", gap:6, marginTop:10, flexWrap:"wrap" }}>
                {(SUBJECTS_BY_COURSE[c]||[]).map(sub=>(
                  <span key={sub} style={{ fontSize:"var(--text-xs)", background:"var(--surface-sunken)", border:"1px solid var(--border-subtle)", borderRadius:"var(--radius-sm)", padding:"2px 7px", color:"var(--text-muted)" }}>{sub}</span>
                ))}
              </div>
            </button>
          );
        })}
      </div>

      {/* All students table */}
      <Card>
        <Card.Header>
          <Card.Title>Todos los estudiantes</Card.Title>
          <div style={{ display:"flex", gap:10, alignItems:"center" }}>
            {list.filter(s=>s.risk).length>0 && <span style={{ fontSize:"var(--text-sm)", color:"var(--danger-600)", fontWeight:600 }}>{list.filter(s=>s.risk).length} en riesgo</span>}
            <Select style={{ width:200 }} value={filterCourse} onChange={e=>setFilterCourse(e.target.value)}>
              <option value="all">Todos los cursos ({students.length})</option>
              {COURSES.map(c=><option key={c} value={c}>{c} ({students.filter(s=>s.course===c).length} alumnos)</option>)}
            </Select>
          </div>
        </Card.Header>
        <div style={{ display:"grid", gridTemplateColumns:"1fr 80px 110px 70px 90px", gap:14, padding:"10px 20px", borderBottom:"1px solid var(--border-subtle)", background:"var(--surface-sunken)" }}>
          <span style={HDR}>Estudiante</span><span style={{...HDR,textAlign:"center"}}>Promedio</span>
          <span style={HDR}>Evaluaciones</span><span style={{...HDR,textAlign:"center"}}>Tendencia</span><span style={HDR}></span>
        </div>
        {list.map((s,i)=>{
          const tot = Object.values(s.grades).reduce((n,g)=>n+g.evals,0);
          return (
            <div key={s.id} onClick={()=>onStudent(s)}
              style={{ display:"grid", gridTemplateColumns:"1fr 80px 110px 70px 90px", gap:14, alignItems:"center", padding:"12px 20px", borderBottom:i<list.length-1?"1px solid var(--border-subtle)":"none", cursor:"pointer", transition:"background 100ms" }}
              onMouseEnter={e=>e.currentTarget.style.background="var(--surface-sunken)"}
              onMouseLeave={e=>e.currentTarget.style.background="transparent"}>
              <div style={{ display:"flex", alignItems:"center", gap:12 }}>
                <Avatar name={s.name} size="sm"/>
                <div>
                  <div style={{ fontWeight:600, fontSize:"var(--text-md)", color:"var(--text-strong)" }}>{s.name}</div>
                  <div style={{ fontSize:"var(--text-xs)", color:"var(--text-subtle)" }}>{s.course} · {s.email}</div>
                </div>
              </div>
              <div style={{ textAlign:"center", fontFamily:"var(--font-display)", fontWeight:700, fontSize:"var(--text-xl)", color:scoreColor(s.avg) }}>{s.avg.toFixed(1)}</div>
              <div style={{ fontSize:"var(--text-sm)", color:"var(--text-muted)" }}>{tot} realizadas</div>
              <div style={{ display:"flex", justifyContent:"center" }}><TrendIcon t={s.trend}/></div>
              <div>{s.risk?<Badge tone="danger" dot>En riesgo</Badge>:<span style={{fontSize:"var(--text-xs)",color:"var(--text-subtle)"}}>—</span>}</div>
            </div>
          );
        })}
      </Card>
    </div>
  );
}

// ── Course view ───────────────────────────────────────────────────────────────
function StuCourseView({ course, onSubject, onStudent }) {
  const cs = students.filter(s=>s.course===course);
  const subjects = SUBJECTS_BY_COURSE[course]||[];
  const ca = cs.length ? _r1(_avg(cs.map(s=>s.avg))) : 0;

  return (
    <div style={{ display:"flex", flexDirection:"column", gap:18 }}>
      {/* Course KPIs */}
      <div style={{ display:"grid", gridTemplateColumns:"repeat(4,1fr)", gap:14 }}>
        <StatCard label="Estudiantes" value={cs.length} icon={<Icon name="users" size={19}/>} iconTone="info"/>
        <StatCard label="Promedio curso" value={ca.toFixed(1)} icon={<Icon name="bar-chart-3" size={19}/>} iconTone="success"/>
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
              <div style={{ display:"flex", justifyContent:"space-between", alignItems:"flex-start" }}>
                <span style={{ fontWeight:700, fontSize:"var(--text-md)", color:"var(--text-strong)" }}>{sub}</span>
                {st.riskCount>0&&<Badge tone="danger" dot>{st.riskCount}</Badge>}
              </div>
              <div style={{ fontFamily:"var(--font-display)", fontWeight:700, fontSize:"var(--text-3xl)", color:scoreColor(st.avg), margin:"10px 0 6px" }}>{st.avg.toFixed(1)}</div>
              <div style={{ fontSize:"var(--text-xs)", color:"var(--text-subtle)", marginBottom:10 }}>{st.count} alumnos · {st.totalEvals} eval.</div>
              <ProgressBar value={(st.avg/7)*100} tone={st.avg<4?"danger":st.avg>=5.5?"success":"brand"}/>
            </button>
          );
        })}
      </div>

      {/* Students table with per-subject grades */}
      <Card>
        <Card.Header><Card.Title>Estudiantes · {course}</Card.Title></Card.Header>
        <div style={{ display:"grid", gridTemplateColumns:`1fr ${subjects.map(()=>"80px").join(" ")} 70px 90px`, gap:12, padding:"10px 20px", borderBottom:"1px solid var(--border-subtle)", background:"var(--surface-sunken)" }}>
          <span style={HDR}>Estudiante</span>
          {subjects.map(s=><span key={s} style={{...HDR,textAlign:"center"}}>{s}</span>)}
          <span style={{...HDR,textAlign:"center"}}>Tendencia</span>
          <span style={HDR}></span>
        </div>
        {cs.map((s,i)=>(
          <div key={s.id} onClick={()=>onStudent(s)}
            style={{ display:"grid", gridTemplateColumns:`1fr ${subjects.map(()=>"80px").join(" ")} 70px 90px`, gap:12, alignItems:"center", padding:"12px 20px", borderBottom:i<cs.length-1?"1px solid var(--border-subtle)":"none", cursor:"pointer", transition:"background 100ms" }}
            onMouseEnter={e=>e.currentTarget.style.background="var(--surface-sunken)"}
            onMouseLeave={e=>e.currentTarget.style.background="transparent"}>
            <div style={{ display:"flex", alignItems:"center", gap:10 }}>
              <Avatar name={s.name} size="sm"/>
              <div style={{ fontWeight:600, fontSize:"var(--text-sm)", color:"var(--text-strong)" }}>{s.name}</div>
            </div>
            {subjects.map(sub=>{
              const g = s.grades[sub];
              return g ? (
                <div key={sub} style={{ textAlign:"center", fontFamily:"var(--font-display)", fontWeight:700, fontSize:"var(--text-lg)", color:scoreColor(g.avg) }}>{g.avg.toFixed(1)}</div>
              ) : <div key={sub} style={{ textAlign:"center", color:"var(--text-subtle)" }}>—</div>;
            })}
            <div style={{ display:"flex", justifyContent:"center" }}><TrendIcon t={s.trend}/></div>
            <div>{s.risk?<Badge tone="danger" dot>En riesgo</Badge>:<span style={{fontSize:"var(--text-xs)",color:"var(--text-subtle)"}}>—</span>}</div>
          </div>
        ))}
      </Card>
    </div>
  );
}

// ── Subject view ──────────────────────────────────────────────────────────────
function StuSubjectView({ course, subject, onStudent }) {
  const st = subjectStats(course, subject);
  if(!st) return null;
  const sorted = [...st.students].sort((a,b)=>b.grades[subject].avg-a.grades[subject].avg);

  return (
    <div style={{ display:"flex", flexDirection:"column", gap:18 }}>
      {/* KPIs */}
      <div style={{ display:"grid", gridTemplateColumns:"repeat(4,1fr)", gap:14 }}>
        <StatCard label="Promedio" value={st.avg.toFixed(1)} icon={<Icon name="bar-chart-3" size={19}/>} iconTone="success"/>
        <StatCard label="Nota más alta" value={st.max.toFixed(1)} icon={<Icon name="trending-up" size={19}/>} iconTone="info"/>
        <StatCard label="Nota más baja" value={st.min.toFixed(1)} icon={<Icon name="trending-down" size={19}/>} iconTone="danger"/>
        <StatCard label="En riesgo" value={st.riskCount} unit="alumnos" icon={<Icon name="alert-triangle" size={19}/>} iconTone="danger"/>
      </div>

      {/* Ranked students table */}
      <Card>
        <Card.Header>
          <Card.Title>Rendimiento · {course} {subject}</Card.Title>
          <Badge tone="info">{st.totalEvals} evaluaciones</Badge>
        </Card.Header>
        <div style={{ display:"grid", gridTemplateColumns:"40px 1fr 120px 70px 80px 90px", gap:12, padding:"10px 20px", borderBottom:"1px solid var(--border-subtle)", background:"var(--surface-sunken)" }}>
          <span style={HDR}>#</span><span style={HDR}>Estudiante</span>
          <span style={HDR}>Progreso</span><span style={{...HDR,textAlign:"center"}}>Nota</span>
          <span style={{...HDR,textAlign:"center"}}>Tendencia</span><span style={HDR}></span>
        </div>
        {sorted.map((s,i)=>{
          const g = s.grades[subject];
          return (
            <div key={s.id} onClick={()=>onStudent(s)}
              style={{ display:"grid", gridTemplateColumns:"40px 1fr 120px 70px 80px 90px", gap:12, alignItems:"center", padding:"12px 20px", borderBottom:i<sorted.length-1?"1px solid var(--border-subtle)":"none", cursor:"pointer", transition:"background 100ms" }}
              onMouseEnter={e=>e.currentTarget.style.background="var(--surface-sunken)"}
              onMouseLeave={e=>e.currentTarget.style.background="transparent"}>
              <div style={{ fontFamily:"var(--font-display)", fontWeight:700, fontSize:"var(--text-lg)", color:"var(--text-subtle)", textAlign:"center" }}>{i+1}</div>
              <div style={{ display:"flex", alignItems:"center", gap:10 }}>
                <Avatar name={s.name} size="sm"/>
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
    </div>
  );
}

// ── Orchestrator ──────────────────────────────────────────────────────────────
function StudentsScreen() {
  const [view, setView] = React.useState({ level:"global" });
  // levels: "global" | "course" | "subject" | "student"

  const crumbs = [{ label:"Estudiantes" }];
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

      {view.level==="global"  && <StuGlobalView
        onCourse={c=>setView({level:"course",course:c})}
        onStudent={s=>setView({level:"student",student:s})}/>}

      {view.level==="course"  && <StuCourseView
        course={view.course}
        onSubject={sub=>setView({level:"subject",course:view.course,subject:sub})}
        onStudent={s=>setView({level:"student",student:s,course:view.course})}/>}

      {view.level==="subject" && <StuSubjectView
        course={view.course} subject={view.subject}
        onStudent={s=>setView({level:"student",student:s,course:view.course,subject:view.subject})}/>}

      {view.level==="student" && <StudentDetail student={view.student}/>}
    </div>
  );
}

window.StudentsScreen = StudentsScreen;
