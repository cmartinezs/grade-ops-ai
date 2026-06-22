// Student portal orchestrator: answer an assessment → submit → view results.
const { Button, ToastViewport, ConfirmDialog } = window.GradeOpsAIDesignSystem_fcd12b;

function StudentApp() {
  const [view, setView] = React.useState("answer"); // answer | results
  const [confirm, setConfirm] = React.useState(false);
  const [submitting, setSubmitting] = React.useState(false);
  const [toasts, setToasts] = React.useState([]);
  const idRef = React.useRef(0);
  const push = (t) => { const id = ++idRef.current; setToasts((x) => [...x, { id, ...t }]); if (!t.loading) setTimeout(() => setToasts((x) => x.filter((o) => o.id !== id)), 3200); return id; };

  const submit = () => {
    setSubmitting(true);
    setTimeout(() => {
      setSubmitting(false); setConfirm(false); setView("results");
      push({ tone: "success", title: "Respuestas enviadas", message: "Tu profesora recibió tu entrega." });
    }, 1400);
  };

  return (
    <>
      {view === "answer"
        ? <AnswerScreen onSubmit={() => setConfirm(true)} />
        : <ResultsScreen onBack={() => setView("answer")} />}

      <ConfirmDialog
        open={confirm} tone="brand"
        title="¿Enviar tus respuestas?"
        message="Una vez enviadas no podrás modificarlas. Revisa que hayas respondido todas las preguntas."
        confirmLabel="Enviar" cancelLabel="Seguir respondiendo" loading={submitting}
        onCancel={() => setConfirm(false)} onConfirm={submit} />

      <ToastViewport toasts={toasts} onDismiss={(id) => setToasts((x) => x.filter((t) => t.id !== id))} />
    </>
  );
}

ReactDOM.createRoot(document.getElementById("root")).render(<StudentApp />);
