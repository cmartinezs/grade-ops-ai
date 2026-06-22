Hover/focus tooltip wrapper. Wrap any trigger element — the tooltip appears above (or below with `placement="bottom"`). Use for icon-only buttons, truncated text, and contextual info that doesn't need a full `Dialog`.

```jsx
<Tooltip label="Compartir globalmente con todos los docentes">
  <IconButton label="Compartir"><GlobeIcon /></IconButton>
</Tooltip>
<Tooltip label="Se enviará un magic link al correo del estudiante" placement="bottom">
  <Button>Publicar resultados</Button>
</Tooltip>
```
