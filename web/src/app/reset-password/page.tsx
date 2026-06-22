"use client";

import { useState, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { resetPassword, PasswordResetError } from "@/lib/api/auth";
import AppLogo from "@/components/brand/AppLogo";
import { Button, Input, FieldWithHelper, LucideIcon } from "@/components/ds";

const resetSchema = z
  .object({
    email: z
      .string()
      .min(1, "Ingresa tu correo.")
      .email("Ingresa una dirección de correo válida."),
    password: z
      .string()
      .min(6, "La contraseña debe tener al menos 6 caracteres."),
    confirmPassword: z.string().min(1, "Confirma tu contraseña."),
  })
  .refine((d) => d.password === d.confirmPassword, {
    message: "Las contraseñas no coinciden.",
    path: ["confirmPassword"],
  });

type ResetFields = z.infer<typeof resetSchema>;

const CODE_ERROR_MESSAGES: Record<string, string> = {
  RESET_CODE_NOT_FOUND: "El enlace no es válido o fue manipulado. Solicita uno nuevo.",
  RESET_CODE_EXPIRED: "El enlace expiró. Solicita uno nuevo desde la pantalla de inicio de sesión.",
  RESET_CODE_USED: "El enlace ya fue utilizado. Solicita uno nuevo si olvidaste tu contraseña.",
};

function ResetPasswordForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const code = searchParams.get("code");

  const [codeError, setCodeError] = useState<string | null>(
    code ? null : "El enlace no es válido o ya expiró."
  );
  const [done, setDone] = useState(false);

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<ResetFields>({ resolver: zodResolver(resetSchema) });

  async function onSubmit(data: ResetFields) {
    try {
      await resetPassword(code!, {
        email: data.email,
        password: data.password,
        passwordRepeat: data.confirmPassword,
      });
      setDone(true);
    } catch (err: unknown) {
      if (err instanceof PasswordResetError) {
        if (err.code === "RESET_CODE_EMAIL_MISMATCH") {
          setError("email", { message: "El correo no coincide con el del enlace." });
        } else {
          setCodeError(
            CODE_ERROR_MESSAGES[err.code] ??
            "No se pudo cambiar la contraseña. Por favor intenta de nuevo."
          );
        }
      } else {
        setCodeError("No se pudo procesar tu solicitud. Intenta de nuevo.");
      }
    }
  }

  return (
    <main
      style={{
        display: "flex",
        height: "100vh",
        fontFamily: "var(--font-sans)",
        background: "var(--surface-page)",
      }}
    >
      {/* LEFT */}
      <div
        style={{
          flex: "1 1 52%",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          padding: "40px 24px",
          overflowY: "auto",
        }}
      >
        <div style={{ marginBottom: 22 }}>
          <AppLogo size="md" />
        </div>

        <div style={{ width: "100%", maxWidth: 380 }}>
          {codeError ? (
            <div role="alert">
              <h1
                style={{
                  fontFamily: "var(--font-display)",
                  fontWeight: 700,
                  fontSize: "var(--text-3xl)",
                  color: "var(--text-strong)",
                  margin: "0 0 12px",
                  letterSpacing: "-0.02em",
                }}
              >
                Enlace inválido
              </h1>
              <p
                style={{
                  color: "var(--text-muted)",
                  fontSize: "var(--text-md)",
                  marginBottom: 28,
                  lineHeight: 1.6,
                }}
              >
                {codeError}
              </p>
              <Button
                variant="primary"
                block
                onClick={() => router.push("/forgot-password")}
              >
                Solicitar nuevo enlace
              </Button>
            </div>
          ) : done ? (
            <div role="status">
              <h1
                style={{
                  fontFamily: "var(--font-display)",
                  fontWeight: 700,
                  fontSize: "var(--text-3xl)",
                  color: "var(--text-strong)",
                  margin: "0 0 12px",
                  letterSpacing: "-0.02em",
                }}
              >
                Contraseña actualizada
              </h1>
              <p
                style={{
                  color: "var(--text-muted)",
                  fontSize: "var(--text-md)",
                  marginBottom: 28,
                  lineHeight: 1.6,
                }}
              >
                Tu contraseña fue cambiada exitosamente. Ya puedes iniciar sesión
                con tu nueva contraseña.
              </p>
              <Button variant="primary" block onClick={() => router.push("/login")}>
                Ir a iniciar sesión
              </Button>
            </div>
          ) : (
            <>
              <h1
                style={{
                  fontFamily: "var(--font-display)",
                  fontWeight: 700,
                  fontSize: "var(--text-3xl)",
                  color: "var(--text-strong)",
                  margin: "0 0 6px",
                  letterSpacing: "-0.02em",
                }}
              >
                Nueva contraseña
              </h1>

              <p
                style={{
                  color: "var(--text-muted)",
                  fontSize: "var(--text-md)",
                  marginBottom: 20,
                }}
              >
                Ingresa tu correo y elige una nueva contraseña.
              </p>

              <form onSubmit={handleSubmit(onSubmit)}>
                <FieldWithHelper
                  label="Correo electrónico"
                  htmlFor="email"
                  helper="Ingresa el correo con el que te registraste. Lo usamos para verificar que el enlace te corresponde."
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
                  label="Nueva contraseña"
                  htmlFor="password"
                  helper="Elige una contraseña de al menos 6 caracteres."
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

                <FieldWithHelper
                  label="Confirmar contraseña"
                  htmlFor="confirmPassword"
                  helper="Ingresa la misma contraseña nuevamente."
                  style={{ marginTop: 14 }}
                >
                  <Input
                    id="confirmPassword"
                    type="password"
                    placeholder="••••••••"
                    icon={<LucideIcon name="lock" size={16} />}
                    error={errors.confirmPassword?.message}
                    showToggle
                    {...register("confirmPassword")}
                  />
                </FieldWithHelper>

                <Button
                  type="submit"
                  variant="primary"
                  block
                  loading={isSubmitting}
                  style={{ marginTop: 16 }}
                >
                  {isSubmitting ? "Guardando…" : "Guardar nueva contraseña"}
                </Button>
              </form>
            </>
          )}
        </div>

        {!codeError && !done && (
          <div
            style={{
              width: "100%",
              maxWidth: 380,
              borderTop: "1px solid var(--border-subtle)",
              marginTop: 28,
              paddingTop: 24,
            }}
          >
            <Button variant="outline" block onClick={() => router.push("/login")}>
              Volver a iniciar sesión
            </Button>
          </div>
        )}

        <p
          style={{
            marginTop: 16,
            fontSize: "var(--text-xs)",
            color: "var(--text-subtle)",
            textAlign: "center",
          }}
        >
          Portal docente · ¿Eres estudiante? Usa el enlace de tu correo.
        </p>
      </div>

      {/* RIGHT — Brand panel */}
      <div
        className="login-panel-right"
        style={{
          flex: "1 1 48%",
          background:
            "linear-gradient(150deg, var(--sprout-600), var(--sprout-800))",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          padding: 40,
        }}
      >
        <div style={{ maxWidth: 420, color: "#fff" }}>
          <LucideIcon name="graduation-cap" size={40} color="rgba(255,255,255,0.9)" />
          <p
            style={{
              fontFamily: "var(--font-display)",
              fontWeight: 600,
              fontSize: "var(--text-2xl)",
              lineHeight: 1.3,
              margin: "22px 0 20px",
              letterSpacing: "-0.01em",
            }}
          >
            "Corrijo una prueba de 32 alumnos en lo que antes me tomaba una tarde entera."
          </p>
          <div style={{ fontSize: "var(--text-md)", lineHeight: 1.5 }}>
            <div style={{ fontWeight: 700 }}>Rodrigo Salinas</div>
            <div style={{ opacity: 0.8 }}>Profesor de Historia · Colegio del Valle</div>
          </div>
          <div
            style={{
              display: "flex",
              gap: 28,
              marginTop: 40,
              paddingTop: 24,
              borderTop: "1px solid rgba(255,255,255,0.2)",
            }}
          >
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

export default function ResetPasswordPage() {
  return (
    <Suspense>
      <ResetPasswordForm />
    </Suspense>
  );
}
