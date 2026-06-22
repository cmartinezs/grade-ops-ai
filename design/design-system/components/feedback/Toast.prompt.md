Toasts are the GradeOps confirmation channel for async results (saved, shared, graded, error). Mount one `ToastViewport` near the app root and feed it state; show a `loading` toast while the work runs, then swap to success/error.

```jsx
const [toasts, setToasts] = useState([]);
// on save: setToasts(t => [...t, {id, loading:true, title:'Guardando…'}])
// on done: replace with {id, tone:'success', title:'Evaluación guardada'}
<ToastViewport toasts={toasts} onDismiss={id => setToasts(t => t.filter(x => x.id !== id))} />
```
Tones: success / error / info, plus `loading`.
