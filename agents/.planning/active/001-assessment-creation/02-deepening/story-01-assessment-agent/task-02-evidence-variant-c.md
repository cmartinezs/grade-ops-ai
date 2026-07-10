# Task 02 evidence — Variant C (few-shot, winner) and the final refined version

> [← task file](task-02-prompt-template.md)

Testing command and environment notes: same as [Variant A's evidence](task-02-evidence-variant-a.md#task-02-evidence--variant-a-instruction-heavy). Same sample brief.

## Variant C — rendered prompt (generation case)

```text
You generate programming assessments as strict JSON. Example (unrelated topic, for format reference only):

{
  "title": "Array Rotation Challenge",
  "context": "Students practice manipulating arrays in-place under a Python course on data structures.",
  "instructions": "Implement a function that rotates an array left by k positions without using extra space.",
  "objectives": ["Apply in-place array manipulation", "Reason about time and space complexity"],
  "deliverables": ["A single Python file with the rotate function", "A short complexity analysis in comments"],
  "constraints": ["No built-in rotation functions", "Must run in O(n) time and O(1) extra space", "Submission deadline: end of lab session"]
}

Now generate a new assessment — same JSON shape, no markdown, no prose, no code fences — for this brief:
- Learning goal: Evaluate whether students can implement iterative algorithms correctly
- Topic: Array manipulation and loops
- Level: introductory
- Duration: 60 minutes
- Language: Python
```

### Output — gen run 1 (no fence)

```json
{
  "title": "Array Element Shift",
  "context": "Students demonstrate fundamental loop constructs and array manipulation in an introductory Python programming course.",
  "instructions": "Write a function `shift_even_left(nums)` that moves all even integers to the front of the list while preserving relative order, using only loops and basic operations.",
  "objectives": ["Apply iterative logic to transform arrays", "Practice in-place list modification with loops"],
  "deliverables": ["A single Python file containing the function and a short main() test block"],
  "constraints": ["No list comprehensions, sorted(), or filter()", "Must use a manual loop (for/while) to rearrange elements", "Do not create a new list for the result", "Submit within 60 minutes"]
}
```

### Output — gen run 2 (no fence)

```json
{
  "title": "Array Sum and Average Calculator",
  "context": "Students demonstrate foundational iterative algorithms and array manipulation in an introductory Python course.",
  "instructions": "Write a program that reads a list of integers, computes the sum and average, and counts how many elements are above the average.",
  "objectives": ["Implement loops to traverse arrays", "Accumulate values using iteration", "Differentiate between sum, average, and threshold counting"],
  "deliverables": ["A single Python file with the complete program", "Inline comments documenting the loop logic"],
  "constraints": ["No use of sum(), len(), or statistics modules", "Must use explicit for or while loops", "Input must be read from stdin (one integer per line, terminated by empty line)", "Submission deadline: 60 minutes"]
}
```

## Variant C — rendered prompt (regeneration case)

Same generation prompt, plus:

```text
This is a regeneration of a previous draft below. Apply the requested adjustments; keep the rest consistent with the brief.

Previous draft:
Title: Array Rotation Practice. Context: Practice iterative array manipulation. Instructions: Implement a function that rotates an array left by k positions, and write unit tests covering edge cases. Objectives: Apply iterative array manipulation; Write correct unit tests. Deliverables: Python file with function; Unit test file. Constraints: No built-in rotate functions; 60 minute time limit.

Requested adjustments:
Make it shorter and remove the requirement for unit tests, students only have 40 minutes now.
```

### Output — regen run 1 (no fence)

```json
{
  "title": "Array Rotation",
  "context": "Practice iterative array manipulation in an introductory Python course.",
  "instructions": "Implement a function that rotates an array left by k positions without using built-in rotation functions.",
  "objectives": ["Apply iterative array manipulation"],
  "deliverables": ["A single Python file with the rotate function"],
  "constraints": ["No built-in rotation functions", "40 minute time limit"]
}
```

Variant C tally: 3 runs, 3/3 valid JSON with all 6 fields, **0/3 fenced**.

---

## Final refinement — added scoping line, re-validated

Variant C won, but was extended with one line to guard against Variant B's over-scoping failure mode:

> "The assessment must fit the level and duration exactly as stated — prefer a single focused task over multiple functions/parts."

This is the version committed as `assessment-generation.st` (see the file for the exact final text with `<attribute>` placeholders and the `<if(adjustmentNotes)>` branch). Re-validated with 4 more runs before committing.

### Output — final gen run 1 (no fence)

```json
{
  "title": "Array Element Search",
  "context": "Students are learning iteration and array manipulation in an introductory Python programming course.",
  "instructions": "Write a function `find_index(arr, target)` that returns the first index of `target` in the list `arr`, or `-1` if not found. Then write a second function `count_occurrences(arr, target)` that returns how many times `target` appears in `arr`. Both must use explicit loops (no `arr.index()`, `.count()`, or list comprehensions).",
  "objectives": ["Implement linear search using a loop", "Count occurrences by iterating through an array", "Translate iterative logic from description to code"],
  "deliverables": ["A single Python file with both functions defined"],
  "constraints": ["No built-in search or count methods", "No list comprehensions or recursion", "Must use for or while loops explicitly", "Submission deadline: 60 minutes"]
}
```

### Output — final gen run 2 (no fence)

```json
{
  "title": "Duplicate Finder",
  "context": "Students demonstrate basic array traversal and loop logic in an introductory Python course.",
  "instructions": "Write a function find_duplicates(nums) that returns a list of all values that appear more than once in the input list, preserving the order of first occurrence.",
  "objectives": ["Use loops to iterate over array elements", "Track seen values with a simple data structure", "Distinguish between first and repeated occurrences"],
  "deliverables": ["A single Python file with find_duplicates and minimal test calls"],
  "constraints": ["No use of collections.Counter or similar high-level utilities", "Must run in O(n) time", "Submission deadline: end of 60-minute lab session"]
}
```

### Output — final regen run 1 (no fence)

```json
{
  "title": "Array Rotation",
  "context": "Students demonstrate iterative algorithm implementation in an introductory Python course.",
  "instructions": "Write a function that rotates a list left by k positions without using built-in rotation functions.",
  "objectives": ["Implement an iterative algorithm correctly"],
  "deliverables": ["A single Python file with the rotate function"],
  "constraints": ["No built-in rotation functions", "Submission deadline: 40 minutes"]
}
```

### Output — final regen run 2 (**fenced**)

````text
```json
{
  "title": "Array Rotation Practice",
  "context": "Introductory Python lab on iterative array manipulation and loops.",
  "instructions": "Implement a function that rotates an array left by k positions without using built-in rotation or slicing shortcuts.",
  "objectives": ["Apply iterative array manipulation using loops"],
  "deliverables": ["A single Python file with the rotate function"],
  "constraints": ["No built-in rotation functions or slicing tricks", "Must use explicit loops", "Submission deadline: end of lab session"]
}
```
````

Final tally: 4 runs, 4/4 valid JSON with all 6 fields, 1/4 fenced.

## Overall tally across all 13 runs (A + B + C + final)

| Variant | Runs | Fenced | Over-scoped |
|---|---|---|---|
| A — instruction-heavy | 3 | 1 (33%) | 0 |
| B — schema-first | 3 | 1 (33%) | 2/2 gen runs |
| C — few-shot (initial) | 3 | 0 (0%) | 0 |
| C — final refined | 4 | 1 (25%) | 0 |
| **Total** | **13** | **3/13 (23%)** | **2/13** (both variant B) |

Residual fencing risk (never zero across any variant) is expected to be tolerated by Spring AI's structured-output entity mapping in task-03, not eliminated by prompt design alone.
