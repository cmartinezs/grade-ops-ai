Tab bar for switching views — e.g. evaluation types (Abiertas / Cerradas / Mixtas) or banco personal vs global. Controlled via `value` + `onChange`.

```jsx
<Tabs value={tab} onChange={setTab} tabs={[
  {id:'all', label:'Todas', count:24},
  {id:'open', label:'Abiertas', count:8},
  {id:'closed', label:'Cerradas', count:16},
]} />
```
`variant="pills"` renders a segmented control.
