Action button for GradeOps AI — use for any primary or secondary action; pass `loading` for async work (it disables and shows an in-place spinner, the GradeOps async convention).

```jsx
<Button variant="primary" onClick={save}>Guardar evaluación</Button>
<Button variant="secondary" iconLeft={<Icon name="plus" />}>Añadir pregunta</Button>
<Button variant="danger" loading={deleting}>Eliminando…</Button>
```

Variants: `primary` (green, default), `accent` (gold — celebratory / highlight CTAs), `secondary` (outlined), `ghost` (toolbar), `danger`, `quiet-danger`. Sizes: `sm` / `md` / `lg`. Use `block` for full-width forms and `iconLeft`/`iconRight` for affordances.
