# Buenas practicas de desarrollo para proyectos Spring Boot

Documentacion de buenas practicas aplicables a cualquier backend Spring Boot, independiente del dominio funcional que resuelva. Esta guia esta pensada para equipos humanos y agentes AI que deban mantener sistemas Java con arquitectura por capas o hexagonal, seguridad, persistencia, testing, CI/CD y operacion.

## Contenido

1. [Resumen ejecutivo](01-resumen-ejecutivo.md)
2. [Arquitectura y diseno](02-arquitectura-y-diseno.md)
3. [Calidad y limpieza de codigo](03-calidad-y-limpieza-de-codigo.md)
4. [Seguridad](04-seguridad.md)
5. [Testing y calidad automatizada](05-testing-y-calidad-automatizada.md)
6. [Automatizaciones, operacion y versionamiento](06-automatizaciones-operacion-y-versionamiento.md)
7. [Guia para desarrollo asistido por AI](07-guia-ai-assisted-development.md)
8. [Checklists operativos](08-checklists-operativos.md)

## Alcance

Esta documentacion describe practicas transferibles para proyectos Spring Boot con Java moderno, Maven o Gradle, arquitectura modular, dominio separado de infraestructura, APIs HTTP, seguridad con Spring Security, persistencia con JPA/Flyway, configuracion por perfiles, observabilidad, Docker, CI y procesos de contribucion.

No asume un negocio especifico. Donde un proyecto use otros nombres de modulos o paquetes, adapte las referencias genericas:

- `<service>-domain`: dominio puro.
- `<service>-app`: casos de uso y puertos.
- `<service>-infra`: adaptadores tecnicos transversales.
- `<service>-api`: controllers, DTOs y contrato HTTP.
- `<service>-persistence`: JPA, repositorios, mappers y migraciones.
- `<service>-run`: aplicacion Spring Boot, wiring, filtros y configuracion.

## Lectura sugerida

Para usarlo como contexto de AI, empieza por:

- `01-resumen-ejecutivo.md`
- `07-guia-ai-assisted-development.md`
- `08-checklists-operativos.md`

Para usarlo como guia tecnica completa, lee en orden del 01 al 06.

## Nota sobre drift documental

En cualquier repositorio real puede existir drift entre README, guias internas, configuracion y codigo. La regla operacional es simple: cuando una guia referencia otro documento, endpoint, modulo, migracion, perfil o comando, verifique que existe antes de asumir su contenido.
