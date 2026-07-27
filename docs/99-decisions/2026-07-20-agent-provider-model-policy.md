# Agent Provider And Model Policy

- Status: Superseded
- Date: 2026-07-20
- Decision owner: Architecture / Founder
- Superseded by: [`2026-07-27-policy-based-model-routing.md`](2026-07-27-policy-based-model-routing.md)

## Context

> Historical decision. Provider/model-aware execution remains valid, but normal requests now delegate exact selection to a policy-based Model Router.

The original technology stack decision selected Spring AI with Gemini / Vertex AI as the target agent runtime. Gemini remains the Google Cloud-oriented target, but the implemented `agents/` service now supports more than one model provider.

Current verified implementation:

- `agents/src/main/resources/application.yml` sets `app.agents.llm.default-provider: groq`.
- `application-beta.yml` and `application-demo.yml` configure both Google GenAI / Gemini and OpenAI-compatible Groq settings.
- `AssessmentConfig` builds named provider adapters for `gemini` and `groq`.
- `AssessmentGenerationPortSelector` resolves provider names through a strategy map and falls back to the configured default when a request omits provider.
- The current functional vertical slice is the Assessment Agent. The other agents remain documented contracts until their releases create real consumers.

The documentation must stop describing the runtime as Gemini-only, while still preserving a clear Google Cloud/Gemini path when that environment is selected.

## Decision

Use a provider/model policy instead of a single hardcoded model assumption.

For the current Assessment Agent slice:

- `groq` is the current default provider because it is implemented and cost-safe for beta/demo iteration.
- `gemini` remains a supported provider and the target provider for Google Cloud-oriented deployments.
- A request may explicitly select a provider when the command contract supports it.
- The runtime must record both `provider` and `model`, not only a Gemini model name.
- Cost estimation must be provider-aware and must distinguish runtime estimated cost from cash cost or credits.
- Documentation must say "provider/model policy" unless the statement is specifically about a Gemini-only environment or requirement.

This decision does not add new providers. It formalizes the two providers already present in the code and leaves future providers behind a new decision.

## Rationale

| Concern | Decision impact |
|---|---|
| Google Cloud path | Gemini remains supported and documentable for Google Cloud-oriented deployments. |
| Iteration speed and cost | Groq can remain the default for beta and development while the product generates real evidence. |
| Runtime correctness | Logs and cost ledgers can represent actual provider/model usage instead of pretending all calls are Gemini. |
| Architecture hygiene | The API remains provider-agnostic; provider selection belongs to the agent runtime command/gateway boundary. |
| Avoiding speculation | The policy only covers providers already wired in the code. |

## Consequences

- `AgentExecutionLog` and related evidence docs must include `provider` and `model`.
- `CostCategory` / cost ledgers need a category for OpenAI-compatible provider usage or a provider field separate from category.
- `docs/03-ai-agents/`, `docs/04-architecture/`, `docs/05-evidence/`, and `docs/09-developer-guide/` must be updated away from Gemini-only language.
- Any future default-provider change must be recorded as a decision or release note.
- Gemini-specific documentation remains valid only when it explicitly discusses a Google Cloud/Gemini environment.

<!-- nav -->

---

← [Agent Runtime Separation](2026-06-10-agent-runtime-separation.md) | [↑ inicio](#agent-provider-and-model-policy) | [README](README.md) | [Environment Roles →](2026-07-20-environment-roles.md)
