// Teacher login — Google SSO + email/password.
const { Button, Input, Field, Spinner } = window.GradeOpsAIDesignSystem_fcd12b;

function LoginScreen({ onLogin }) {
  const [loading, setLoading] = React.useState(false);
  const go = () => { setLoading(true); setTimeout(onLogin, 1100); };

  return (
    <div style={loginStyles.root}>
      <div style={loginStyles.left}>
        <div style={loginStyles.brand}>
          <img src="../../assets/logo-mark.svg" alt="" style={{ width: 40, height: 40 }} />
          <span style={loginStyles.brandText}>GradeOps<span style={{ color: "var(--brand)" }}>AI</span></span>
        </div>
        <div style={loginStyles.card}>
          <h1 style={loginStyles.title}>Bienvenida de vuelta</h1>
          <p style={loginStyles.sub}>Entra para crear y corregir evaluaciones con IA.</p>

          <button style={loginStyles.google} onClick={go}
            onMouseEnter={(e) => (e.currentTarget.style.background = "var(--surface-sunken)")}
            onMouseLeave={(e) => (e.currentTarget.style.background = "var(--surface-card)")}>
            <svg width="18" height="18" viewBox="0 0 48 48"><path fill="#EA4335" d="M24 9.5c3.5 0 6.6 1.2 9 3.6l6.7-6.7C35.6 2.4 30.1 0 24 0 14.6 0 6.4 5.4 2.5 13.2l7.8 6.1C12.2 13.3 17.6 9.5 24 9.5z"/><path fill="#4285F4" d="M46.5 24.5c0-1.6-.1-3.1-.4-4.5H24v9h12.7c-.5 3-2.2 5.5-4.7 7.2l7.3 5.7c4.3-4 6.8-9.9 6.8-17.4z"/><path fill="#FBBC05" d="M10.3 28.7c-.5-1.5-.8-3-.8-4.7s.3-3.2.8-4.7l-7.8-6.1C.9 16.5 0 20.1 0 24s.9 7.5 2.5 10.8l7.8-6.1z"/><path fill="#34A853" d="M24 48c6.1 0 11.3-2 15-5.5l-7.3-5.7c-2 1.4-4.7 2.3-7.7 2.3-6.4 0-11.8-3.8-13.7-9.3l-7.8 6.1C6.4 42.6 14.6 48 24 48z"/></svg>
            {loading ? "Conectando…" : "Continuar con Google"}
            {loading && <Spinner size="xs" />}
          </button>

          <div style={loginStyles.divider}>
            <span style={loginStyles.divLine} /><span style={{ padding: "0 12px" }}>o con tu correo</span><span style={loginStyles.divLine} />
          </div>

          <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
            <Field label="Correo institucional" htmlFor="em">
              <Input id="em" type="email" placeholder="paula.mendez@liceoandes.cl" icon={<Icon name="mail" size={18} />} defaultValue="paula.mendez@liceoandes.cl" />
            </Field>
            <Field label="Contraseña" htmlFor="pw">
              <Input id="pw" type="password" placeholder="••••••••" icon={<Icon name="lock" size={18} />} defaultValue="demodemo" />
            </Field>
            <Button block loading={loading} onClick={go}>Iniciar sesión</Button>
            <a href="#" style={loginStyles.forgot}>¿Olvidaste tu contraseña?</a>
          </div>
        </div>
        <p style={loginStyles.foot}>Portal docente · ¿Eres estudiante? Usa el enlace de tu correo.</p>
      </div>

      <div style={loginStyles.right}>
        <div style={loginStyles.quoteWrap}>
          <Icon name="graduation-cap" size={40} color="rgba(255,255,255,0.9)" />
          <p style={loginStyles.quote}>“Corrijo una prueba de 32 alumnos en lo que antes me tomaba una tarde entera.”</p>
          <div style={loginStyles.quoteAuthor}>
            <div style={{ fontWeight: 700 }}>Rodrigo Salinas</div>
            <div style={{ opacity: 0.8 }}>Profesor de Historia · Colegio del Valle</div>
          </div>
          <div style={loginStyles.statRow}>
            <div><div style={loginStyles.statNum}>+8.400</div><div style={loginStyles.statLbl}>docentes</div></div>
            <div><div style={loginStyles.statNum}>1,2M</div><div style={loginStyles.statLbl}>correcciones</div></div>
            <div><div style={loginStyles.statNum}>9 min</div><div style={loginStyles.statLbl}>ahorro/prueba</div></div>
          </div>
        </div>
      </div>
    </div>
  );
}

const loginStyles = {
  root: { display: "flex", height: "100%", fontFamily: "var(--font-sans)", background: "var(--surface-page)" },
  left: { flex: "1 1 52%", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: "40px 24px", position: "relative" },
  brand: { display: "flex", alignItems: "center", gap: 11, marginBottom: 22 },
  brandText: { fontFamily: "var(--font-display)", fontWeight: 700, fontSize: 23, color: "var(--text-strong)", letterSpacing: "-0.02em" },
  card: { width: "100%", maxWidth: 380 },
  title: { fontFamily: "var(--font-display)", fontWeight: 700, fontSize: "var(--text-3xl)", color: "var(--text-strong)", margin: "0 0 6px", letterSpacing: "-0.02em" },
  sub: { margin: "0 0 24px", color: "var(--text-muted)", fontSize: "var(--text-md)" },
  google: { width: "100%", height: 46, display: "flex", alignItems: "center", justifyContent: "center", gap: 10, border: "1px solid var(--border-default)", background: "var(--surface-card)", borderRadius: "var(--radius-md)", cursor: "pointer", fontFamily: "inherit", fontWeight: 600, fontSize: "var(--text-md)", color: "var(--text-body)", transition: "background 120ms" },
  divider: { display: "flex", alignItems: "center", justifyContent: "center", color: "var(--text-subtle)", fontSize: "var(--text-xs)", margin: "20px 0" },
  divLine: { flex: 1, height: 1, background: "var(--border-subtle)" },
  forgot: { textAlign: "center", fontSize: "var(--text-sm)", color: "var(--text-link)", marginTop: 2 },
  foot: { marginTop: 28, fontSize: "var(--text-xs)", color: "var(--text-subtle)", textAlign: "center" },
  right: { flex: "1 1 48%", background: "linear-gradient(150deg, var(--sprout-600), var(--sprout-800))", display: "flex", alignItems: "center", justifyContent: "center", padding: 40 },
  quoteWrap: { maxWidth: 420, color: "#fff" },
  quote: { fontFamily: "var(--font-display)", fontWeight: 600, fontSize: "var(--text-2xl)", lineHeight: 1.3, margin: "22px 0 20px", letterSpacing: "-0.01em" },
  quoteAuthor: { fontSize: "var(--text-md)", lineHeight: 1.5 },
  statRow: { display: "flex", gap: 28, marginTop: 40, paddingTop: 24, borderTop: "1px solid rgba(255,255,255,0.2)" },
  statNum: { fontFamily: "var(--font-display)", fontWeight: 700, fontSize: "var(--text-2xl)" },
  statLbl: { fontSize: "var(--text-xs)", opacity: 0.8, marginTop: 2 },
};

window.LoginScreen = LoginScreen;
