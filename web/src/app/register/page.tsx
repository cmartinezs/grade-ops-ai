"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { createUserWithEmailAndPassword, User } from "firebase/auth";
import { auth } from "@/lib/firebase/client";
import { registerTeacher } from "@/lib/api/auth";
import AppLogo from "@/components/brand/AppLogo";
import { Button, Input, FieldWithHelper, GoogleButton, LucideIcon } from "@/components/ds";

const registerSchema = z.object({
  firstName: z.string().min(2, "Ingresa tu nombre."),
  lastName: z.string().min(2, "Ingresa tu apellido."),
  email: z.string().min(1, "Ingresa tu correo.").email("Ingresa una dirección de correo válida."),
  password: z.string().min(6, "La contraseña debe tener al menos 6 caracteres."),
});

type RegisterFields = z.infer<typeof registerSchema>;

const FIREBASE_ERRORS: Record<string, string> = {
  "auth/email-already-in-use": "Ya existe una cuenta con ese correo. Intenta iniciar sesión.",
  "auth/weak-password": "La contraseña debe tener al menos 6 caracteres.",
  "auth/invalid-email": "Ingresa una dirección de correo válida.",
  "auth/operation-not-allowed": "El registro con email y contraseña no está habilitado. Contacta al administrador.",
};

export default function RegisterPage() {
  const router = useRouter();
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFields>({ resolver: zodResolver(registerSchema) });

  async function onSubmit(data: RegisterFields) {
    setServerError(null);
    let firebaseUser: User | null = null;
    try {
      const credential = await createUserWithEmailAndPassword(auth, data.email, data.password);
      firebaseUser = credential.user;
      const idToken = await credential.user.getIdToken();
      await registerTeacher(idToken, data.firstName, data.lastName);
      router.push("/verify-email");
    } catch (err: unknown) {
      if (firebaseUser) {
        await firebaseUser.delete().catch(() => {});
      }
      const code = (err as { code?: string }).code;
      setServerError(FIREBASE_ERRORS[code ?? ""] ?? "No pudimos crear tu cuenta. Intenta de nuevo.");
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
            Crea tu cuenta
          </h1>
          <p style={{ margin: "0 0 24px", color: "var(--text-muted)", fontSize: "var(--text-md)" }}>
            Empieza gratis. Sin tarjeta de crédito.
          </p>

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
            <div style={{ display: "flex", gap: 10 }}>
              <FieldWithHelper
                label="Nombres"
                htmlFor="firstName"
                helper="Tu nombre o nombres de pila, tal como aparece en tu identificación."
                style={{ flex: 1 }}
              >
                <Input
                  id="firstName"
                  type="text"
                  placeholder="Rodrigo"
                  icon={<LucideIcon name="user" size={16} />}
                  error={errors.firstName?.message}
                  {...register("firstName")}
                />
              </FieldWithHelper>
              <FieldWithHelper
                label="Apellidos"
                htmlFor="lastName"
                helper="Tu apellido o apellidos, tal como aparece en tu identificación."
                style={{ flex: 1 }}
              >
                <Input
                  id="lastName"
                  type="text"
                  placeholder="Salinas"
                  error={errors.lastName?.message}
                  {...register("lastName")}
                />
              </FieldWithHelper>
            </div>

            <FieldWithHelper
              label="Correo electrónico"
              htmlFor="email"
              helper="Te enviaremos un enlace de verificación a este correo. También lo usarás para iniciar sesión."
              style={{ marginTop: 14 }}
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
              helper="Mínimo 6 caracteres. Elige algo que recuerdes; lo necesitarás cada vez que inicies sesión con correo."
              style={{ marginTop: 14 }}
            >
              <Input
                id="password"
                type="password"
                placeholder="Mínimo 6 caracteres"
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
              {isSubmitting ? "Creando cuenta…" : "Crear cuenta"}
            </Button>
          </form>
        </div>

        <div style={{ width: "100%", maxWidth: 380, borderTop: "1px solid var(--border-subtle)", marginTop: 28, paddingTop: 24 }}>
          <Button variant="outline" block onClick={() => router.push("/login")}>
            ¿Ya tienes cuenta? Inicia sesión
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
