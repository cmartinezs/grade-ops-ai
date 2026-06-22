Modal surface. `Dialog` for custom content; `ConfirmDialog` is the required GradeOps gate before any data-mutating action (delete, publish, share) and before leaving a page with unsaved/async work.

```jsx
<ConfirmDialog
  open={open} tone="danger"
  title="¿Eliminar evaluación?"
  message="Se eliminarán 32 entregas y sus correcciones. Esta acción no se puede deshacer."
  confirmLabel="Eliminar" loading={deleting}
  onCancel={close} onConfirm={remove} />
```
`tone="danger"` for destructive confirms; `loading` keeps it open with a spinner.
