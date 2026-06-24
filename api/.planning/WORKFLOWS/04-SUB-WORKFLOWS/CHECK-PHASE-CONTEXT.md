# [CHECK-PHASE-CONTEXT]

> [← README](README.md)

Verifies that the required `docs/` contract documents exist and have been read before generating implementation output.

---

## Steps

1. Identify the target file's repository area (DO / WB / AP / AG / IN).
2. Identify which `docs/` contract documents are required for this area (see `05-SDLC-PHASE-GUIDANCE/`).
3. Verify those documents exist and have been read by the agent:
   - API work → `docs/04-architecture/api-design.md` and `docs/04-architecture/data-model.md`
   - Agent work → `docs/03-ai-agents/[agent-name].md` and `docs/04-architecture/repository-structure.md`
   - Web work → `docs/06-ux/[ux-file].md` and `docs/04-architecture/repository-structure.md`
   - Infra work → `docs/04-architecture/deployment.md`
   - Docs work → `docs/CLAUDE.md`
4. If required docs have not been read: return `MISSING` + which files to read first.
5. If all required docs are accessible: return `OK`.

---

**Called by:** [`GENERATE-DOCUMENT`](../02-EXECUTION-WORKFLOWS/GENERATE-DOCUMENT.md)

---

> [← README](README.md)
