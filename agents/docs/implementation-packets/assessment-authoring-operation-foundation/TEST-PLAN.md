<a id="top"></a>

# TEST-PLAN — Agents: Assessment Authoring Operation Foundation

**Status:** Ready. **Parent:** [README](README.md) · **Prev:** [LOCAL-CONTRACTS](LOCAL-CONTRACTS.md) · **Next:** [HANDOFF →](HANDOFF.md)

## Scope

One extended test: the existing successful-generation test for `AssessmentAgentOrchestrator` gains one new assertion proving the response payload's provider field equals the resolved provider's name. No new test framework, no new test file required unless the existing structure makes that impractical.

## Existing suite

`agents/`'s own 32-test suite (per [Research 03](../../../../design-system/research/research-03-baseline-technical-verification.md)) is not restructured by this packet. Run it in full alongside the new assertion:

```bash
./mvnw -f agents/pom.xml -Pdemo clean test
```

**The `-Pdemo` profile is mandatory** — Spring AI starters are only declared under the `demo`/`beta` Maven profiles. A plain `./mvnw -f agents/pom.xml clean test` fails to compile by design; that failure is not a defect in this codebase.

## Offline only

`agents/`'s tests never call a real provider — this discipline is unchanged. Mock/stub the resolved provider exactly as the existing test suite already does.

## Contract fixture for Session A

Once the new field is implemented and tested, produce a real JSON example of the response shape (not a description of it) and include it in [HANDOFF.md](HANDOFF.md) — this becomes the fixture for the API session's Task 07C contract test, per [root packet § Task 07 split](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/04-task-distribution.md#task-07-split).

---

← [LOCAL-CONTRACTS](LOCAL-CONTRACTS.md) | [↑ inicio](#top) | [Siguiente: HANDOFF →](HANDOFF.md)
