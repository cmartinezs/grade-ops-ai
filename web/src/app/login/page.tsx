"use client";

import { useState, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { signInWithEmailAndPassword } from "firebase/auth";
import { auth } from "@/lib/firebase/client";
import { registerTeacher } from "@/lib/api/auth";
import AppLogo from "@/components/brand/AppLogo";
import { Button, Input, FieldWithHelper, GoogleButton, LucideIcon } from "@/components/ds";
import Link from "next/link";

const loginSchema = z.object({
  email: z.string().min(1, "Ingresa tu correo.").email("Ingresa una dirección de correo válida."),
  password: z.string().min(1, "Ingresa tu contraseña."),
});

type LoginFields = z.infer<typeof loginSchema>;

const FIREBASE_ERRORS: Record<string, string> = {
  "auth/wrong-password": "Correo o contraseña incorrectos.",
  "auth/invalid-credential": "Correo o contraseña incorrectos.",
  "auth/user-not-found": "No encontramos una cuenta con ese correo.",
  "auth/invalid-email": "Ingresa una dirección de correo válida.",
};

function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const reason = searchParams.get("reason");
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFields>({ resolver: zodResolver(loginSchema) });

  async function onSubmit(data: LoginFields) {
    setServerError(null);
    try {
      const credential = await signInWithEmailAndPassword(auth, data.email, data.password);
      if (credential.user.emailVerified === false) {
        router.push("/verify-email");
      } else {
        router.push("/dashboard");
      }
    } catch (err: unknown) {
      const code = (err as { code?: string }).code;
      setServerError(FIREBASE_ERRORS[code ?? ""] ?? "Inicio de sesión fallido. Por favor intenta de nuevo.");
    }
  }

  return (
    <main style={{ display: "flex", height: "100vh", fontFamily: "var(--font-sans)", background: "var(--surface-page)" }}>

      {/* LEFT — Formulario */}
      <div style={{ flex: "1 1 52%", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: "40px 24px", overflowY: "auto" }}>

        <div style={{ marginBottom: 22 }}>
          <AppLogo size="md" />
        </div>

        <div style={{ width: "100%", maxWidth: 380 }}>
          <h1 style={{ fontFamily: "var(--font-display)", fontWeight: 700, fontSize: "var(--text-3xl)", color: "var(--text-strong)", margin: "0 0 6px", letterSpacing: "-0.02em" }}>
            Bienvenida de vuelta
          </h1>
          <p style={{ margin: "0 0 24px", color: "var(--text-muted)", fontSize: "var(--text-md)" }}>
            Entra para crear y corregir evaluaciones con IA.
          </p>

          {reason === "expired" && (
            <div
              role="alert"
              style={{
                marginBottom: 20,
                padding: "12px 14px",
                background: "var(--warning-50)",
                border: "1px solid var(--warning-200)",
                borderRadius: "var(--radius-md)",
                color: "var(--warning-700)",
                fontSize: "var(--text-sm)",
              }}
            >
              Tu sesión expiró. Por favor inicia sesión de nuevo.
            </div>
          )}

          <GoogleButton
            onSuccess={async (idToken, displayName) => {
              const spaceIdx = displayName.indexOf(" ");
              const firstName = spaceIdx < 0 ? displayName : displayName.slice(0, spaceIdx);
              const lastName  = spaceIdx < 0 ? "" : displayName.slice(spaceIdx + 1);
              await registerTeacher(idToken, firstName, lastName);
              router.push("/dashboard");
            }}
          />

          {/* Divisor */}
          <div style={{ display: "flex", alignItems: "center", color: "var(--text-subtle)", fontSize: "var(--text-xs)", margin: "20px 0" }}>
            <span style={{ flex: 1, height: 1, background: "var(--border-subtle)" }} />
            <span style={{ padding: "0 12px" }}>o con tu correo</span>
            <span style={{ flex: 1, height: 1, background: "var(--border-subtle)" }} />
          </div>

          <form onSubmit={handleSubmit(onSubmit)}>
            <FieldWithHelper
              label="Correo electrónico"
              htmlFor="email"
              helper="Ingresa el correo con el que creaste tu cuenta en GradeOps AI."
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

            <FieldWithHelper
              label="Contraseña"
              htmlFor="password"
              helper="Ingresa la contraseña que elegiste al registrarte. Usa el enlace de abajo si la olvidaste."
              style={{ marginTop: 14 }}
            >
              <Input
                id="password"
                type="password"
                placeholder="••••••••"
                icon={<LucideIcon name="lock" size={16} />}
                error={errors.password?.message}
                showToggle
                {...register("password")}
              />
            </FieldWithHelper>

            {serverError && (
              <p role="alert" style={{ fontSize: "var(--text-sm)", color: "var(--danger-600)", margin: "8px 0 0" }}>
                {serverError}
              </p>
            )}

            <Button
              type="submit"
              variant="primary"
              block
              loading={isSubmitting}
              style={{ marginTop: 16 }}
            >
              {isSubmitting ? "Iniciando sesión…" : "Iniciar sesión"}
            </Button>

            <Link
              href="/forgot-password"
              style={{ display: "block", textAlign: "center", fontSize: "var(--text-sm)", color: "var(--text-link)", marginTop: 10 }}
            >
              ¿Olvidaste tu contraseña?
            </Link>
          </form>
        </div>

        <div style={{ width: "100%", maxWidth: 380, borderTop: "1px solid var(--border-subtle)", marginTop: 28, paddingTop: 24 }}>
          <Button variant="outline" block onClick={() => router.push("/register")}>
            Crear cuenta nueva
          </Button>
        </div>

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

export default function LoginPage() {
  return (
    <Suspense>
      <LoginForm />
    </Suspense>
  );
}
