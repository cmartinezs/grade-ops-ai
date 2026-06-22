"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { forgotPassword } from "@/lib/api/auth";
import AppLogo from "@/components/brand/AppLogo";
import { Button, Input, FieldWithHelper, LucideIcon } from "@/components/ds";

const forgotSchema = z.object({
  email: z.string().min(1, "Ingresa tu correo.").email("Ingresa una dirección de correo válida."),
});

type ForgotFields = z.infer<typeof forgotSchema>;

export default function ForgotPasswordPage() {
  const router = useRouter();
  const [submitted, setSubmitted] = useState(false);

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<ForgotFields>({ resolver: zodResolver(forgotSchema) });

  async function onSubmit(data: ForgotFields) {
    try {
      await forgotPassword(data.email);
    } catch {
      // Always show confirmation — enumeration protection
    } finally {
      setSubmitted(true);
    }
  }

  return (
    <main style={{ display: "flex", height: "100vh", fontFamily: "var(--font-sans)", background: "var(--surface-page)" }}>

      {/* LEFT — Formulario o confirmación */}
      <div style={{ flex: "1 1 52%", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: "40px 24px", overflowY: "auto" }}>

        <div style={{ marginBottom: 22 }}>
          <AppLogo size="md" />
        </div>

        <div style={{ width: "100%", maxWidth: 380 }}>
          {submitted ? (
            <div role="status">
              <h1 style={{ fontFamily: "var(--font-display)", fontWeight: 700, fontSize: "var(--text-3xl)", color: "var(--text-strong)", margin: "0 0 12px", letterSpacing: "-0.02em" }}>
                Revisa tu correo
              </h1>
              <p style={{ color: "var(--text-muted)", fontSize: "var(--text-md)", marginBottom: 28, lineHeight: 1.6 }}>
                Si existe una cuenta con ese correo, recibirás un enlace en los próximos minutos. Revisa también tu carpeta de spam.
              </p>
              <Button variant="outline" block onClick={() => router.push("/login")}>
                Volver a iniciar sesión
              </Button>
            </div>
          ) : (
            <>
              <h1 style={{ fontFamily: "var(--font-display)", fontWeight: 700, fontSize: "var(--text-3xl)", color: "var(--text-strong)", margin: "0 0 6px", letterSpacing: "-0.02em" }}>
                Recupera tu contraseña
              </h1>
              <p style={{ margin: "0 0 24px", color: "var(--text-muted)", fontSize: "var(--text-md)" }}>
                Te enviaremos un enlace para restablecer tu contraseña.
              </p>

              <form onSubmit={handleSubmit(onSubmit)}>
                <FieldWithHelper
                  label="Correo electrónico"
                  htmlFor="email"
                  helper="Ingresa el correo con el que te registraste en GradeOps AI. Te enviaremos un enlace para restablecer tu contraseña."
                >
                  <Input
                    id="email"
                    type="email"
                    placeholder="tu@correo.com"
                    icon={<LucideIcon name="mail" size={16} />}
                    error={errors.email?.message}
                    {...register("email")}
                  />
                </FieldWithHelper>

                <Button
                  type="submit"
                  variant="primary"
                  block
                  loading={isSubmitting}
                  style={{ marginTop: 16 }}
                >
                  {isSubmitting ? "Enviando…" : "Enviar enlace"}
                </Button>
              </form>
            </>
          )}
        </div>

        {!submitted && (
          <div style={{ width: "100%", maxWidth: 380, borderTop: "1px solid var(--border-subtle)", marginTop: 28, paddingTop: 24 }}>
            <Button variant="outline" block onClick={() => router.push("/login")}>
              Volver a iniciar sesión
            </Button>
          </div>
        )}

        <p style={{ marginTop: 16, fontSize: "var(--text-xs)", color: "var(--text-subtle)", textAlign: "center" }}>
          Portal docente · ¿Eres estudiante? Usa el enlace de tu correo.
        </p>
      </div>

      {/* RIGHT — Panel de marca Sprout */}
      <div
        className="login-panel-right"
        style={{
          flex: "1 1 48%",
          background: "linear-gradient(150deg, var(--sprout-600), var(--sprout-800))",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          padding: 40,
        }}
      >
        <div style={{ maxWidth: 420, color: "#fff" }}>
          <LucideIcon name="graduation-cap" size={40} color="rgba(255,255,255,0.9)" />
          <p style={{ fontFamily: "var(--font-display)", fontWeight: 600, fontSize: "var(--text-2xl)", lineHeight: 1.3, margin: "22px 0 20px", letterSpacing: "-0.01em" }}>
            "Corrijo una prueba de 32 alumnos en lo que antes me tomaba una tarde entera."
          </p>
          <div style={{ fontSize: "var(--text-md)", lineHeight: 1.5 }}>
            <div style={{ fontWeight: 700 }}>Rodrigo Salinas</div>
            <div style={{ opacity: 0.8 }}>Profesor de Historia · Colegio del Valle</div>
          </div>
          <div style={{ display: "flex", gap: 28, marginTop: 40, paddingTop: 24, borderTop: "1px solid rgba(255,255,255,0.2)" }}>
            <div>
              <div style={{ fontFamily: "var(--font-display)", fontWeight: 700, fontSize: "var(--text-2xl)" }}>+8.400</div>
              <div style={{ fontSize: "var(--text-xs)", opacity: 0.8, marginTop: 2 }}>docentes</div>
            </div>
            <div>
              <div style={{ fontFamily: "var(--font-display)", fontWeight: 700, fontSize: "var(--text-2xl)" }}>1,2M</div>
              <div style={{ fontSize: "var(--text-xs)", opacity: 0.8, marginTop: 2 }}>correcciones</div>
            </div>
            <div>
              <div style={{ fontFamily: "var(--font-display)", fontWeight: 700, fontSize: "var(--text-2xl)" }}>9 min</div>
              <div style={{ fontSize: "var(--text-xs)", opacity: 0.8, marginTop: 2 }}>ahorro/prueba</div>
            </div>
          </div>
        </div>
      </div>

    </main>
  );
}
