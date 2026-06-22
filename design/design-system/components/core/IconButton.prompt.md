Icon-only square button for toolbars, table-row actions, and dense controls. Always pass `label` for accessibility.

```jsx
<IconButton label="Editar" onClick={edit}><Icon name="pencil" /></IconButton>
<IconButton variant="solid" label="Nueva"><Icon name="plus" /></IconButton>
```

Variants: `ghost` (default), `outline`, `solid`. Sizes: `sm` / `md`.
