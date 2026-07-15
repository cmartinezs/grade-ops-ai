Single-line text input. Wrap in `Field` for labels/errors. Use `icon` for search/email affordances, `mono` for codes.

```jsx
<Input placeholder="Buscar pregunta…" icon={<SearchIcon/>} />
<Input mono value="GOPS-7K2A-9F" readOnly />
<Input invalid placeholder="correo@colegio.cl" />
```
Sizes sm / md / lg.
