# ADR: Create the agentclient module — the only part of api/ allowed to call agents/

**Date:** 2026-07-13
**Status:** Accepted
**Planning:** 003-assessment-creation / story-01-assessment-creation-persistence / task-05-agentclient

## Context

`api/` needs to call `agents/`'s internal `POST /internal/agents/assessment` endpoint to generate and regenerate assessment drafts. This is the first cross-service HTTP call in the project, and the two services deploy independently on Cloud Run, with `agents/` gated `INGRESS_TRAFFIC_INTERNAL_ONLY` and IAM-restricted to `api/`'s own service account.

## Decision

Plain synchronous HTTP client using Spring's `RestClient` (already available via `spring-boot-starter-webmvc`; the project has no reactive/WebFlux dependency, so `WebClient` is not used). This module does not need a Spring AI dependency — Gemini is only called inside `agents/`.

`AssessmentCommand`/`AssessmentAgentResponse` are kept as a separate copy rather than a shared library, since the two services deploy independently.

For auth: Cloud Run's IAM invoker binding between the two services' service accounts is the primary protection in `demo`/production (real OIDC identity-token auth, platform-enforced before a request reaches the app). Fetching and attaching a real Google-signed identity token from `api/` is the fully-correct production implementation, but is meaningful extra scope (new dependency, credential plumbing, no local-dev equivalent). For this task, the shared-secret header (`X-Internal-Key` / `app.internal.secret`, already scaffolded identically in both `api/` and `agents/`) is implemented as defense-in-depth that works identically in local dev and prod, rather than silently skipping the gap or over-building the real OIDC flow without a decision.

An explicit connect/read timeout is set on the `RestClient` (Gemini calls can be slow) — the default, potentially unbounded, timeout is not used.

## Consequences

Set an explicit connect/read timeout on the `RestClient` — do not use the default (potentially unbounded) timeout. This is the first cross-service HTTP call in the project; network/timeout/auth failures must not leak as raw 500s to the teacher, so `AgentClientException` (with a `UNREACHABLE`/`AGENT_REJECTED`/`AGENT_ERROR` reason code) wraps every failure mode. `agents/`'s `MALFORMED_OUTPUT` reason code currently covers both genuinely malformed model output and an underlying LLM provider failure (e.g. a live rate limit) — no retry/backoff logic should be built in `api/` assuming `AGENT_REJECTED` means one specific thing.

The real-OIDC-token gap is recorded as an open residual (`R-01`/`D-03` in this planning's `TRACEABILITY.md`), not silently dropped — revisit post-MVP if a stronger service-to-service auth guarantee is needed beyond Cloud Run's network-level IAM enforcement.

## Alternatives Considered

Fetching and attaching a real Google-signed OIDC identity token (e.g. via `google-auth-library`'s `IdTokenProvider` against the metadata server) was considered as the fully-correct production implementation, but discarded for this task as meaningful extra scope (new dependency, credential plumbing, no local-dev equivalent since there's no real Cloud Run IAM in `docker compose`) — deferred as a recorded residual rather than built now.

A shared library for `AssessmentCommand`/`AssessmentAgentResponse` between `api/` and `agents/` was implicitly considered and rejected in favor of a separate mirrored copy in each service, since the two services deploy independently.

Reactive `WebClient` was not used since the project has no other reactive/WebFlux dependency; plain synchronous `RestClient` matches the rest of the stack.
