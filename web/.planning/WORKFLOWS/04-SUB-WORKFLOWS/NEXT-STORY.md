# [NEXT-STORY]

> [← README](README.md)

Identifies the next story to be executed within the current planning's DEEPENING phase.

---

## Steps

1. Open the planning's `02-deepening/` directory and list all story files.
2. Find the story with the lowest sequence number that has status `TODO`.
3. Check that all dependencies of that story (if any) are `DONE`.
4. If dependencies are not done: find the next story with satisfied dependencies.
5. Return the identified story as the next to execute.

---

**Called by:** [`ADVANCE-PLANNING`](../01-PLANNING-WORKFLOWS/ADVANCE-PLANNING.md)

---

> [← README](README.md)
