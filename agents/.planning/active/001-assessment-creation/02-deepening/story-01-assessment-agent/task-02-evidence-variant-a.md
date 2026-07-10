# Task 02 evidence — Variant A (instruction-heavy)

> [← task file](task-02-prompt-template.md)

Testing command (repeated per run, same rendered prompt each time):

```bash
opencode run "$(cat variant-a-<gen|regen>.txt)" -m opencode/deepseek-v4-flash-free --format json \
  | jq -r 'select(.type=="text") | .part.text'
```

`opencode providers list` in this environment has no Gemini/Vertex AI provider configured — only `opencode/*` hosted models and `openai/*`. Runs use `opencode/deepseek-v4-flash-free` as an available, fast, free-tier proxy for JSON-shape/instruction-following compliance, not as a stand-in for Gemini's actual content quality. Production model stays Gemini per `CLAUDE.md`.

Sample brief used for all runs: `learningGoal="Evaluate whether students can implement iterative algorithms correctly"`, `topic="Array manipulation and loops"`, `level="introductory"`, `duration="60 minutes"`, `language="Python"`.

## Rendered prompt — generation case

```text
You are an expert programming assessment designer. Follow these rules strictly:

1. Produce a single practical programming assessment consistent with the teacher's brief below.
2. The assessment must be appropriate for the stated level and completable within the stated duration.
3. All content must be written for the requested programming language/context.
4. Objectives must be specific and measurable, not generic.
5. Constraints must state exactly what is and is not allowed (e.g. external libraries, AI assistance, time limits).
6. Output ONLY a single valid JSON object. No markdown, no prose, no code fences, no explanation before or after the JSON.
7. The JSON object must have exactly these fields, in this order: "title" (string), "context" (string), "instructions" (string), "objectives" (array of strings), "deliverables" (array of strings), "constraints" (array of strings).
8. Do not include any field not listed above. Do not omit any field.

Teacher's brief:
- Learning goal: Evaluate whether students can implement iterative algorithms correctly
- Topic: Array manipulation and loops
- Level: introductory
- Duration: 60 minutes
- Language: Python

Return the JSON object now.
```

### Output — gen run 1 (no fence)

```json
{"title": "Array Rotation", "context": "You are implementing a utility function that rotates the elements of an array to the left by a given number of positions.", "instructions": "Write a Python function `rotate_left(arr, k)` that takes a list `arr` and an integer `k` and returns a new list with the elements rotated left by `k` positions. Elements that fall off the left side wrap around to the right. If `k` is negative, rotate right instead. If the array is empty, return an empty list. Do not modify the original array.", "objectives": ["Implement a function that rotates left by a positive k", "Handle k larger than the array length using modulo", "Handle negative k as a right rotation", "Handle an empty array gracefully", "Return a new list without mutating the input"], "deliverables": ["A single Python file named `rotate.py` containing the `rotate_left` function", "The file must define `rotate_left` as the only top-level function"], "constraints": ["No external libraries or imports allowed", "You must use only basic Python loops (for/while) — no slicing, no list comprehensions, no built-in reversed()", "You may not use the `collections.deque` module", "No AI assistance or code generation tools allowed", "Total time limit: 60 minutes"]}
```

### Output — gen run 2 (no fence)

```json
{"title": "Array Rotation Challenge", "context": "You are given a list of integers and a rotation count k. Your task is to rotate the array to the right by k steps using an iterative in-place algorithm.", "instructions": "Write a Python function `rotate(arr, k)` that rotates the list `arr` to the right by `k` positions. Implement the rotation using a loop (iterative approach). Do not use Python's built-in slicing or collections.deque. Ensure k may be larger than the array length.", "objectives": ["Correctly rotate arrays of length 1 through 10 with various k values", "Handle k > len(arr) by properly normalizing the rotation count", "Implement rotation using an explicit loop (no slicing or deque)", "Preserve the original array type (list) in the output"], "deliverables": ["A single .py file containing the `rotate` function and at least 5 test cases demonstrating correctness"], "constraints": ["No external libraries allowed (only Python standard library)", "No use of list slicing (e.g., arr[-k:] + arr[:-k])", "No use of collections.deque.rotate or any higher-level rotation utility", "No AI assistance or code-generation tools", "Must be completed within 60 minutes"]}
```

## Rendered prompt — regeneration case

Same as above, plus (after "Return the JSON object now." moved to the end):

```text
This is a regeneration request. The teacher reviewed a previous draft and asked for changes.

Previous draft:
Title: Array Rotation Practice. Context: Practice iterative array manipulation. Instructions: Implement a function that rotates an array left by k positions, and write unit tests covering edge cases. Objectives: Apply iterative array manipulation; Write correct unit tests. Deliverables: Python file with function; Unit test file. Constraints: No built-in rotate functions; 60 minute time limit.

Requested adjustments:
Make it shorter and remove the requirement for unit tests, students only have 40 minutes now.

Apply the requested adjustments to the previous draft. Keep everything else consistent with the original brief unless the adjustment explicitly requires a change.
```

### Output — regen run 1 (**fenced** — violates rule 6)

````text
```json
{"title": "Array Rotation Practice", "context": "Practice iterative array manipulation", "instructions": "Implement a function that rotates an array left by k positions.", "objectives": ["Apply iterative array manipulation"], "deliverables": ["Python file with function"], "constraints": ["No built-in rotate functions", "40 minute time limit"]}
```
````

## Tally

- 3 runs, 3/3 valid JSON with all 6 required fields, 1/3 fenced despite an explicit "no code fences" rule.
- Regeneration correctly reflected the previous draft + adjustment.
