// assessment-generation.v1
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
- Learning goal: <learningGoal>
- Topic: <topic>
- Level: <level>
- Duration: <duration>
- Language: <language>

The assessment must fit the level and duration exactly as stated — prefer a single focused task over multiple functions/parts.

<if(adjustmentNotes)>
This is a regeneration of a previous draft below. Apply the requested adjustments; keep the rest consistent with the brief.

Previous draft:
<previousDraft>

Requested adjustments:
<adjustmentNotes>
<endif>
