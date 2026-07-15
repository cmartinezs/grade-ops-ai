Filterable chip for active taxonomy filters (subject, unit, topic), question-bank facets, and selected student groups. Unlike `Badge`, `Tag` supports an `onRemove` × button and an `onClick` toggle state.

```jsx
// Active filter chips in question bank
<Tag tone="brand" onRemove={() => removeFilter("biologia")}>Biología</Tag>
<Tag tone="gold" onRemove={() => removeFilter("fotosintesis")}>Fotosíntesis</Tag>
<Tag onClick={() => toggleFilter("global")} dot>Global</Tag>
```

Tones: neutral / brand / gold / info. Use `dot` for live-status chips.
