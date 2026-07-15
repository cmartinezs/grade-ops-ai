The GradeOps async indicator. `Spinner` is an inline loader (inherits text color); `LoadingOverlay` covers a component (in a `position:relative` parent) while an async action runs — the convention for in-place async feedback so spinners aren't repeated everywhere.

```jsx
<Spinner size="sm" />
<div style={{position:'relative'}}>
  {grading && <LoadingOverlay label="Corrigiendo con IA…" />}
  <ResultsTable/>
</div>
```
