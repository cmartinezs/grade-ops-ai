# Flujo B — Planning desde cero (sin epic)

> [← Tutorial](README.md)

**Cuándo usar este flujo:** el trabajo que quieres planificar es transversal, técnico, o de infraestructura — no corresponde a ninguna user story del producto. Ejemplos: configurar CI/CD, implementar autenticación JWT en la capa API, migrar base de datos, refactorizar un módulo.

---

## Escenario

Implementar JWT authentication en la capa API — trabajo técnico que habilita múltiples epics pero no pertenece a ninguno específico.

---

## Paso 1 (opcional) — Capturar la idea

Si la iniciativa está bien clara en tu cabeza, puedes saltar directo al Paso 2. Usa `/plan-template` cuando quieras pensar en voz alta primero.

### Modo interactivo

```
/plan-template jwt-auth-api
```

Claude hace 6 preguntas una por una:

```
[1/6] ¿Qué necesita hacerse? (una sola oración)
> Implement JWT-based authentication for the GradeOps API layer

[2/6] ¿Por qué existe esta iniciativa? ¿Qué problema resuelve?
> The API has no auth layer yet. All epics that involve teacher actions
  (creating assessments, approving AI outputs) require an authenticated
  teacher identity before any other work can proceed.

[3/6] ¿Cuál es el resultado concreto cuando esté listo?
> Teacher can sign in, receive a JWT token, and all protected API
  endpoints reject requests without a valid token.

[4/6] ¿Qué áreas de repos afecta?
> api/, web/ (login page), docs/ (auth contract)

[5/6] ¿Restricciones conocidas?
> Must use Spring Security. Gemini API key must never be in the token.
  Teacher email is the identity source.

[6/6] ¿Qué no sabes todavía?
> Whether to use a dedicated auth service or embed in the API monolith.
```

Guarda `.planning/ideas/jwt-auth-api.md`.

### Modo plantilla en blanco

```
/plan-template jwt-auth-api --blank
```

Genera `.planning/ideas/jwt-auth-api.md` vacío para llenar a mano y cerrar el editor.

---

## Paso 2 — Crear el planning

### Desde documento de idea (si hiciste el Paso 1)

```
/plan-new 001-jwt-auth-api @.planning/ideas/jwt-auth-api.md
```

Claude puebla `00-initial.md` con todo lo que encontró en el documento e indica qué quedó como placeholder.

### Inline (captura rápida)

```
/plan-new 001-jwt-auth-api -- Implement JWT authentication for the API layer
```

Solo llena el campo `Intent`. Las demás secciones quedan para completar a mano.

**Resultado en ambos casos:**

```
.planning/
├── README.md                      ← entry "001-jwt-auth-api" bajo ### 🆕 Initial
├── ideas/
│   └── jwt-auth-api.md            ← (si usaste /plan-template)
└── 001-jwt-auth-api/
    ├── 00-initial.md              ← pre-llenado
    ├── 01-expansion.md            ← vacío
    ├── 02-deepening/
    │   └── story-NN-name.md       ← plantilla vacía
    └── TRACEABILITY.md
```

Abre `00-initial.md` y completa o revisa cualquier sección que haya quedado como placeholder antes de avanzar.

---

## Paso 3 — Dimensionar los stories

Cuando tengas claridad sobre el alcance:

```
/plan-expand 001-jwt-auth-api
```

Claude lee `00-initial.md` y te propone los stories (o te pregunta si los quieres definir tú):

```
Propuesta de stories para 001-jwt-auth-api:

story-01-docs        DO  Documentar el contrato de auth en docs/
story-02-api-domain  AP  Entidades Teacher, JwtToken, AuthEvent en api/
story-03-api-auth    AP  Endpoints /auth/login y /auth/refresh + Spring Security
story-04-web-login   WB  Página de login en web/, llamada al endpoint

Dependencias:
  story-03 → story-02
  story-04 → story-03

¿Confirmas estos stories, o quieres ajustar? (confirmar / editar)
```

Al confirmar, Claude:
1. Llena `01-expansion.md` con la tabla y el mapa de dependencias
2. Crea `02-deepening/story-0N-*.md` para cada story con status `TODO`
3. Mueve el folder a `.planning/active/001-jwt-auth-api/`
4. Actualiza `.planning/README.md` y `active/README.md`

**`/plan-status` después:**

```
ACTIVE
  001-jwt-auth-api — Implement JWT authentication for the API layer
    story-01-docs        [TODO]
    story-02-api-domain  [TODO]
    story-03-api-auth    [TODO]
    story-04-web-login   [TODO]
```

---

## Paso 4 (opcional) — Atomizar una story

Si las tareas de una story siguen siendo demasiado gruesas para implementarlas directamente, descomponlo en tareas atómicas:

```
/plan-atomize 001-jwt-auth-api story-03
```

Claude propone el desglose y, al confirmar, crea un archivo por tarea:

```
.planning/active/001-jwt-auth-api/02-deepening/
├── story-03-api-auth.md             ← la tabla de tareas pasa a ser el índice
└── story-03-api-auth/
    ├── task-01-jwt-filter.md        ← diseño técnico + pasos + tests + done criteria
    ├── task-02-login-endpoint.md
    └── task-03-refresh-endpoint.md
```

Cada tarea es ejecutable en una sola sesión y cumple el checklist de atomicidad (`[CHECK-ATOMICITY]`): un solo entregable verificable, diseño técnico decidido, pasos concretos, plan de tests unitarios y done criteria binarios.

```
# Ejecutar una tarea individual
/plan-task 001-jwt-auth-api story-03 task-01

# Auditar que las tareas están bien formadas (solo lectura)
/plan-task-validate 001-jwt-auth-api story-03
```

---

## Paso 5 — Ejecutar

```
/plan-story 001-jwt-auth-api story-01
/plan-story 001-jwt-auth-api story-02
/plan-story 001-jwt-auth-api story-03
/plan-story 001-jwt-auth-api story-04
```

Si ejecutas alguna story a mano, márcalo después:

```
/plan-done 001-jwt-auth-api story-02
```

---

## Paso 6 — Archivar

Valida la integridad estructural y, si no hay FAIL, archiva:

```
/plan-validate 001-jwt-auth-api
/plan-retrospective 001-jwt-auth-api
/plan-archive 001-jwt-auth-api
```

---

## Diferencia clave con el Flujo A

| | Flujo A (desde epic) | Flujo B (desde cero) |
|--|---------------------|---------------------|
| Fuente de los stories | User stories del epic | Definición manual o inferida por Claude |
| Pasa por INITIAL | No — va directo a `active/` | Sí — requiere `/plan-expand` |
| Done criteria | Vienen de los AC + DoD de cada story | Hay que definirlos en cada story |
| Trazabilidad al producto | Enlace explícito `Source: US-NNN` | Sin enlace a product backlog |

---

> [← Tutorial](README.md)
