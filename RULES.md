<a id="top"></a>

# Reglas del repositorio

Este archivo establece reglas transversales para mantener el repositorio comprensible, navegable y actualizado. Las reglas aplican de forma incremental: no obligan a normalizar archivos históricos ajenos a un cambio, pero sí a todo archivo o carpeta que se cree, modifique, mueva o elimine.

## Documentación de carpetas

### README obligatorio

Toda carpeta mantenida dentro del repositorio debe contener un `README.md` en su raíz.

El `README.md` debe:

- explicar en pocas líneas el propósito, alcance y responsabilidad de la carpeta;
- indexar y enlazar los archivos que se encuentran directamente en esa carpeta;
- resumir cada archivo directo con una descripción breve y concreta;
- indexar las subcarpetas directas como unidades y enlazar sus propios `README.md`;
- indicar reglas, dependencias o fuentes de verdad específicas de la carpeta cuando existan;
- evitar duplicar el índice interno de las subcarpetas.

Cada subcarpeta mantiene su propio índice. Un `README.md` documenta solamente su nivel inmediato; no construye un catálogo recursivo e indefinido de todo el árbol.

### Actualización obligatoria

Crear, modificar, mover, renombrar o eliminar un archivo obliga a revisar y actualizar el `README.md` de su carpeta directa en el mismo cambio.

- Si el `README.md` no existe, debe crearse en ese momento.
- Si cambia el propósito o contenido de una subcarpeta, debe actualizarse también el `README.md` de su carpeta padre.
- Los enlaces, nombres, orden y resúmenes deben representar el estado resultante del cambio.
- No se acepta un archivo nuevo o modificado con un índice conocido como obsoleto.

La adopción es incremental: tocar un archivo activa esta regla para su carpeta directa, pero no obliga a reparar carpetas históricas fuera del alcance del cambio.

### Excepciones

No se crean `README.md` manuales dentro de:

- dependencias descargadas o vendorizadas;
- artefactos generados;
- directorios de compilación, cobertura o distribución;
- cachés, temporales y metadatos de herramientas;
- carpetas cuyo contenido sea administrado íntegramente por una herramienta externa.

Una excepción adicional debe justificarse en el `README.md` de la carpeta padre. Una carpeta de código fuente no queda exceptuada solo por contener muchos archivos.

## Navegación de Markdown

Todo archivo `*.md` creado o modificado debe incluir navegación homogénea:

1. un ancla `<a id="top"></a>` al inicio;
2. un enlace a su índice o archivo padre;
3. un enlace al siguiente archivo definido por el orden del `README.md`;
4. un enlace para volver al inicio del archivo.

Formato estándar al final del documento:

```markdown
---

[← Índice de la carpeta](README.md) · [Siguiente →](siguiente-archivo.md) · [↑ Volver al inicio](#top)
```

Reglas complementarias:

- el orden de navegación se declara en el `README.md`, no se infiere silenciosamente;
- el último archivo de una secuencia vuelve al índice de la carpeta;
- un `README.md` enlaza como siguiente el primer documento de su secuencia;
- los enlaces son relativos y deben funcionar desde GitHub;
- el `README.md` raíz puede usar la raíz del repositorio como parent;
- no se exige navegación retroactiva en Markdown fuera del alcance del cambio.

## Validación del cambio

Antes de aprobar o publicar un cambio:

1. identificar carpetas con archivos creados, modificados, movidos o eliminados;
2. comprobar que cada una tenga `README.md`;
3. confirmar que los índices y resúmenes coincidan con los archivos directos resultantes;
4. comprobar que las subcarpetas estén enlazadas sin copiar sus índices internos;
5. verificar en cada Markdown afectado los enlaces a parent, siguiente e inicio;
6. ejecutar una comprobación de enlaces Markdown cuando exista una herramienta automatizada;
7. rechazar el cambio si la documentación correspondiente quedó desactualizada.

La validación automática futura complementará esta regla, pero no reemplaza la revisión del contenido y la calidad de los resúmenes.

---

[← README del repositorio](README.md) · [Siguiente: Design System →](design-system/README.md) · [↑ Volver al inicio](#top)
