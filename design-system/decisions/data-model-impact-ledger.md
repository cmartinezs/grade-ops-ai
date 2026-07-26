# Data Model Impact Ledger

## Propósito

Registrar capacidades originadas o expuestas por decisiones UI/UX que podrían afectar dominio, datos, API, eventos, seguridad, agentes o migraciones. El ledger no define el esquema ni autoriza implementación.

## Estados

- `Open`: impacto identificado, pendiente de análisis técnico.
- `In analysis`: alternativas e invariantes en evaluación.
- `Resolved`: decisión técnica documentada y enlazada.
- `Deferred`: aceptado fuera del alcance actual con condición de revisión.
- `Rejected`: la capacidad no se implementará.

## Registro

| ID | Decisión UI/UX | Capacidad requerida | Impacto potencial | Estado | Resolución técnica |
|---|---|---|---|---|---|
| DMI-001 | Control humano sobre IA | Origen, revisión y publicación diferenciados | Estados o dimensiones separadas; actor; timestamps; versión; `ApprovalEvent`; contratos Web/API/Agents | Open | Pendiente |
| DMI-002 | Acciones críticas | Borradores, autoguardado, historial, recuperación y operaciones masivas seguras | Versionado; optimistic locking; idempotency key; batch status; audit trail; eventos compensatorios | Open | Pendiente |
| DMI-003 | Privacidad y permisos | Autorización contextual y aislamiento de recursos | Organization, course, section, membership, ownership, permissions, retention y auditoría | Open | Pendiente |
| DMI-004 | Procesos asíncronos | Progreso real, resultados parciales, reintento y cancelación | Job, attempts, item results, error codes, idempotencia, polling/SSE y recuperación | Open | Pendiente |
| DMI-005 | Internacionalización | Locale, idioma de salida, zona horaria y configuración académica independientes | Preferencias de usuario e institución; resolución de locale; contratos `code + parameters`; contenido multilingüe | Open | Pendiente |
| DMI-006 | Responsive diferenciado | Capacidades variables por contexto de uso | Capabilities/read models; autorización no dependiente del cliente; continuidad entre dispositivos | Open | Pendiente |
| DMI-007 | WCAG 2.2 AA | Preferencias y alternativas accesibles cuando corresponda | Semántica de componentes, contenido alternativo, announcements y evidencia de pruebas; persistencia solo si se justifica | Open | Pendiente |

## Reglas de actualización

Cada entrada debe evolucionar con:

- entidad, agregado o módulo potencialmente afectado;
- estados, transiciones e invariantes;
- datos persistidos y política de retención;
- contrato, evento o mecanismo asíncrono;
- autorización, privacidad y auditoría;
- migración y compatibilidad;
- ADR técnico y pruebas que resuelven el impacto.

No consolidar elaboración, revisión, publicación, origen IA y estado de job en un único `status` sin demostrar que representan una sola máquina de estados.

## Gate de resolución

Antes de que una release implemente una capacidad registrada:

1. definir invariantes y fuente de verdad;
2. resolver el impacto técnico en el planning correspondiente;
3. crear o actualizar el ADR técnico cuando aplique;
4. enlazar contratos, migraciones, eventos y pruebas;
5. cambiar la entrada a `Resolved` o documentar un `Deferred` explícito.
