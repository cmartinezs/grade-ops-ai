# Task 02 evidence — Variant B (schema-first)

> [← task file](task-02-prompt-template.md)

Testing command and environment notes: same as [Variant A's evidence](task-02-evidence-variant-a.md#task-02-evidence--variant-a-instruction-heavy). Same sample brief.

## Rendered prompt — generation case

```text
Return exactly one JSON object with this shape and nothing else — no markdown, no prose, no code fences:

{
  "title": string,
  "context": string,
  "instructions": string,
  "objectives": [string, ...],
  "deliverables": [string, ...],
  "constraints": [string, ...]
}

Generate a practical programming assessment for the following brief:
- Learning goal: Evaluate whether students can implement iterative algorithms correctly
- Topic: Array manipulation and loops
- Level: introductory
- Duration: 60 minutes
- Language: Python

The assessment must fit the level and duration, use the specified language, and have specific, measurable objectives (not generic filler).
```

### Output — gen run 1 (**fenced**; also over-scoped: 5 functions in 60 minutes)

````text
```json
{
  "title": "Array Transformations with Loops",
  "context": "You are building a small data-processing utility that transforms arrays of numbers. The functions you write will be used to clean and prepare data before analysis.",
  "instructions": "Write Python functions in a file named `array_utils.py`. Each function must use explicit loops (for/while) — do not use list comprehensions or built-in functions like `map`, `filter`, `sum`, `min`, `max`, or `sorted`. Starter code with function signatures and docstrings is provided; implement only the function bodies.",
  "objectives": [
    "Write a function `running_total(arr)` that returns a new list where each element i is the sum of arr[0] through arr[i] using a single loop.",
    "Write a function `remove_every_other(arr)` that returns a new list containing only elements at even indices using a loop and accumulator pattern.",
    "Write a function `reverse_copy(arr)` that returns a new list with elements in reverse order using a loop (no slicing tricks).",
    "Write a function `rotate_left(arr, n)` that returns a new list rotated left by n positions using loops only.",
    "Write a function `pair_sums(arr1, arr2)` that returns a new list where element i is arr1[i] + arr2[i]; if arrays differ in length, pad the shorter with zeros."
  ],
  "deliverables": [
    "A single file `array_utils.py` containing all five function implementations.",
    "Each function passes at least 3 test cases provided by the instructor.",
    "Functions must handle edge cases (empty lists, single elements, zero rotation, mismatched lengths)."
  ],
  "constraints": [
    "No list comprehensions, generator expressions, or slicing (except `[:]` for copy).",
    "No built-in aggregation functions (`sum`, `min`, `max`, `any`, `all`).",
    "No use of `numpy`, `pandas`, or any external libraries — standard library only.",
    "Functions must not mutate the input lists (return new lists).",
    "Time limit: 60 minutes."
  ]
}
```
````

### Output — gen run 2 (no fence; also over-scoped: 4 functions in 60 minutes)

```json
{
  "title": "Array Transformations with Iterative Algorithms",
  "context": "You have learned Python lists, for/while loops, and basic list operations. This assessment tests your ability to implement iterative algorithms that manipulate arrays without relying on Python's built-in convenience functions.",
  "instructions": "Implement the following four functions in a single Python file. Each function must use a loop (for or while) to perform its task. Do not use built-ins like max(), min(), sum(), list.reverse(), sorted(), or slicing shortcuts like lst[::-1] for the core logic. Each function must include a docstring describing its behavior, parameters, and return value. Include a short test block (if __name__ == '__main__':) that demonstrates each function with at least two test cases.",
  "objectives": [
    "Implement find_max that returns the largest element in a list of integers using a single pass with a loop",
    "Implement reverse_list that reverses a list in-place by swapping elements from both ends toward the center",
    "Implement remove_duplicates that returns a new list preserving only the first occurrence of each element, preserving original order",
    "Implement rotate_left that shifts all elements of a list left by k positions, wrapping around, using a loop"
  ],
  "deliverables": [
    "One Python file (.py) containing all four function definitions",
    "A docstring for each function",
    "A test block (if __name__ == '__main__') with at least two test cases per function, including edge cases (empty list, single element, k larger than list length)"
  ],
  "constraints": [
    "Core logic must use explicit loops — do not use max(), min(), sum(), list.reverse(), sorted(), or slice reversal",
    "No external libraries or imports beyond typing (e.g., from typing import List, Optional) if desired",
    "Functions must handle edge cases: empty lists, single-element lists, and k values exceeding list length",
    "60-minute time limit — if you cannot finish all four, submit partial work for partial credit",
    "Code must be your own work; no collaboration or use of AI code generators"
  ]
}
```

## Rendered prompt — regeneration case

Same generation prompt, plus:

```text
Regenerate the previous draft below, applying the requested adjustments. Keep everything else from the original brief unless the adjustment requires otherwise.

Previous draft:
Title: Array Rotation Practice. Context: Practice iterative array manipulation. Instructions: Implement a function that rotates an array left by k positions, and write unit tests covering edge cases. Objectives: Apply iterative array manipulation; Write correct unit tests. Deliverables: Python file with function; Unit test file. Constraints: No built-in rotate functions; 60 minute time limit.

Requested adjustments:
Make it shorter and remove the requirement for unit tests, students only have 40 minutes now.
```

### Output — regen run 1 (no fence)

```json
{
  "title": "Array Rotation",
  "context": "Implement array rotation using loops.",
  "instructions": "Write a function rotate_left(arr, k) that rotates an array left by k positions.",
  "objectives": [
    "Implement rotation using only loop constructs (no slicing or built-in rotate)",
    "Correctly handle edge cases: empty array, k=0, k > array length"
  ],
  "deliverables": [
    "Python file containing the rotate_left function"
  ],
  "constraints": [
    "No built-in rotate functions or slicing shortcuts",
    "40 minute time limit"
  ]
}
```

## Tally

- 3 runs, 3/3 valid JSON with all 6 required fields, 1/3 fenced.
- Both generation runs over-scoped the assessment (4-5 separate functions) despite the brief stating a single 60-minute window — a real quality defect independent of JSON-shape compliance.
