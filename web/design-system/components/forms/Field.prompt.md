Wraps any input with a label, required/optional marker, and hint or error text. Pair its `htmlFor` with the input `id`.

```jsx
<Field label="Nombre de la evaluación" htmlFor="t" required hint="Visible para los estudiantes">
  <Input id="t" placeholder="Ej. Prueba Unidad 3" />
</Field>
<Field label="Correo" htmlFor="e" error="Correo no válido"><Input id="e" invalid /></Field>
```
