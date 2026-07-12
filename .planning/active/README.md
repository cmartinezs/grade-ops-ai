# 🚧 Active Plannings

> [← planning/README.md](../README.md)

Plannings currently in EXPANSION or DEEPENING phase.

---

## In Progress

### [008-assessment-creation](008-assessment-creation/01-expansion.md)

Enable the first step of the open-assessment pipeline: turning a teacher's learning intent into a structured, AI-generated assessment draft that is editable and fully logged.

Stories: US-010 Assessment Brief Intake (P0), US-011 Assessment Draft Generation (P0), US-012 Assessment Draft Regeneration (P1) — `docs/02-product/user-stories/epic-02-assessment-creation/`.

| # | Story | Área | Status |
|---|-------|------|--------|
| 01 | [agents-assessment-agent-coordination](008-assessment-creation/02-deepening/story-01-agents-assessment-agent.md) → tracks `agents/.planning/001-assessment-creation` | AG | IN PROGRESS |
| 02 | [api-assessment-creation-coordination](008-assessment-creation/02-deepening/story-02-api-assessment-creation.md) → tracks `api/.planning/003-assessment-creation` | AP | TODO |
| 03 | [web-assessment-creation](008-assessment-creation/02-deepening/story-03-web-assessment-creation.md) | WB | TODO |

---

### [009-groq-infra-provisioning](009-groq-infra-provisioning/01-expansion.md)

Provision the Groq API key and its Cloud Run wiring as real Terraform infra for the `agents/` service. `infra/` has no `.planning/` workspace of its own, so this parent planning owns the Terraform implementation directly — relocated from `agents/.planning/active/002-groq-genai-provider`'s story-02 (agents/'s story-01, the Groq adapter itself, is `DONE`).

| # | Story | Área | Status |
|---|-------|------|--------|
| 01 | [groq-infra-provisioning](009-groq-infra-provisioning/02-deepening/story-01-groq-infra-provisioning.md) | IN | TODO |

---

> [← planning/README.md](../README.md)
