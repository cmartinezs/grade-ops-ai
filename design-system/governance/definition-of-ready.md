# Definition of Ready UI/UX

## Propósito

Evitar que una etapa comience con dependencias, autoridad o evidencia insuficientes. El criterio se aplica al inicio de cada etapa y se complementa con el gate de salida de la etapa anterior.

## Criterios generales

Una etapa está lista para comenzar cuando:

- el problema y el resultado esperado están declarados;
- existe un responsable de aprobación;
- entradas, dependencias y restricciones están disponibles;
- las decisiones previas aplicables están identificadas;
- las hipótesis y riesgos abiertos son visibles;
- los impactos potenciales en dominio, datos, API, eventos, seguridad e infraestructura están registrados;
- se conoce qué evidencia permitirá aceptar o rechazar el resultado;
- no existe un bloqueo crítico oculto detrás de datos simulados o supuestos no declarados.

## Definition of Ready — Etapa 02

El descubrimiento UX puede comenzar cuando:

- [`01-principios-y-gobierno.md`](../workflow/01-principios-y-gobierno.md) cumple su criterio de salida;
- Carlos mantiene la autoridad inicial de producto, UX y técnica;
- el feedback docente externo está clasificado como opcional;
- WCAG 2.2 AA, responsive diferenciado, control humano sobre IA, privacidad, procesos asíncronos e i18n transversal están aceptados;
- existe la plantilla de ADR UI/UX;
- existe el Data Model Impact Ledger;
- el alcance de descubrimiento y las fuentes disponibles pueden identificarse antes de entrevistar o diseñar;
- no se ha seleccionado una dirección visual.

## Evidencia de cumplimiento

El responsable de la etapa registra en su documento:

- fecha de revisión;
- criterios cumplidos;
- riesgos aceptados;
- bloqueos;
- decisión `Ready` o `Not ready`.

Un criterio incumplido solo puede tratarse como excepción mediante el proceso definido en [`roles-and-authority.md`](roles-and-authority.md).
