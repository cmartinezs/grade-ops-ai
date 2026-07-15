// Teacher portal orchestrator: login → shell with routed screens + toasts.
const { Button, ToastViewport, ConfirmDialog } = window.GradeOpsAIDesignSystem_fcd12b;

function TeacherApp() {
  const [authed, setAuthed] = React.useState(false);
  const [route, setRoute] = React.useState("dashboard");
  const [toasts, setToasts] = React.useState([]);
  const [confirmPublish, setConfirmPublish] = React.useState(false);
  const [publishing, setPublishing] = React.useState(false);
  const idRef = React.useRef(0);

  const push = (t) => {
    const id = ++idRef.current;
    setToasts((x) => [...x, { id, ...t }]);
    if (!t.loading) setTimeout(() => dismiss(id), 3200);
    return id;
  };
  const dismiss = (id) => setToasts((x) => x.filter((t) => t.id !== id));
  const replace = (id, t) => setToasts((x) => x.map((o) => (o.id === id ? { id, ...t } : o)));

  const saveAssessment = () => {
    const id = push({ loading: true, title: "Guardando evaluación…" });
    setTimeout(() => { replace(id, { tone: "success", title: "Evaluación guardada", message: "Prueba Unidad 3 — Fotosíntesis" }); setTimeout(() => dismiss(id), 3000); setRoute("dashboard"); }, 1300);
  };
  const doPublish = () => {
    setPublishing(true);
    setTimeout(() => {
      setPublishing(false); setConfirmPublish(false);
      const id = push({ tone: "success", title: "Resultados publicados", message: "Se enviaron 28 enlaces mágicos a los estudiantes." });
      setTimeout(() => dismiss(id), 3400);
    }, 1500);
  };

  const TITLES = {
    dashboard:   ["Panel de control",    "Resumen de tus cursos y evaluaciones"],
    assessments: ["Evaluaciones",         "Gestiona y revisa todas tus evaluaciones"],
    builder:     ["Nueva evaluación",     "Configura tipo, preguntas y fechas"],
    bank:        ["Banco de preguntas",   "Tus preguntas personales y el banco global"],
    students:    ["Estudiantes",          "Gestiona cursos y progreso académico"],
    reports:     ["Reportes",             "Rendimiento por grupo y evaluación"],
    grading:     ["Corrección con IA",    "Ensayo: Fotosíntesis · 2°A Biología"],
  };
  const [t, st] = TITLES[route] || TITLES.dashboard;

  if (!authed) return <LoginScreen onLogin={() => { setAuthed(true); push({ tone: "success", title: "¡Hola, Paula!", message: "Sesión iniciada correctamente." }); }} />;

  const headerAction =
    route === "dashboard" || route === "assessments"
      ? <Button iconLeft={<Icon name="plus" size={17} />} onClick={() => setRoute("builder")}>Nueva evaluación</Button>
      : route === "grading" || route === "builder"
      ? <Button variant="secondary" iconLeft={<Icon name="arrow-left" size={16} />} onClick={() => setRoute(route === "builder" ? "assessments" : "dashboard")}>Volver</Button>
      : route === "bank"
      ? <Button variant="secondary" iconLeft={<Icon name="plus" size={16} />} onClick={() => {}}>Nueva pregunta</Button>
      : route === "students"
      ? <Button variant="secondary" iconLeft={<Icon name="user-plus" size={16} />} onClick={() => {}}>Añadir estudiante</Button>
      : null;

  return (
    <>
      <Shell active={route === "grading" || route === "builder" ? "assessments" : route} onNavigate={setRoute} title={t} subtitle={st} actions={headerAction}>
        {route === "dashboard"   && <DashboardScreen onOpenAssessment={() => setRoute("grading")} onGoBank={() => setRoute("bank")} />}
        {route === "assessments" && <AssessmentsScreen onGrade={() => setRoute("grading")} />}
        {route === "builder"     && <BuilderScreen onCancel={() => setRoute("assessments")} onSave={saveAssessment} />}
        {route === "bank"        && <BankScreen />}
        {route === "grading"     && <GradingScreen onPublish={() => setConfirmPublish(true)} />}
        {route === "students"    && <StudentsScreen />}
        {route === "reports"     && <ReportsScreen />}
      </Shell>

      <ConfirmDialog
        open={confirmPublish} tone="brand"
        title="¿Publicar resultados?"
        message="Se enviará un enlace mágico de resultados a 28 estudiantes. Podrán ver su nota, la rúbrica y la retroalimentación."
        confirmLabel="Publicar y enviar" loading={publishing}
        onCancel={() => setConfirmPublish(false)} onConfirm={doPublish} />

      <ToastViewport toasts={toasts} onDismiss={dismiss} />
    </>
  );
}

function Placeholder({ name }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 12, padding: 80, color: "var(--text-subtle)" }}>
      <Icon name="hammer" size={36} color="var(--slate-400)" />
      <p style={{ margin: 0, fontFamily: "var(--font-display)", fontWeight: 600, fontSize: "var(--text-lg)", color: "var(--text-muted)" }}>{name}</p>
      <p style={{ margin: 0, fontSize: "var(--text-sm)" }}>Vista no incluida en este UI kit.</p>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById("root")).render(<TeacherApp />);
