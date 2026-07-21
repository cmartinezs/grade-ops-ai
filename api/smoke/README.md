# Smoke Test Fixtures

## `fake-service-account.json`

A **throwaway** RSA key pair with no relation to any real Google Cloud or Firebase project.
It exists only to satisfy `FirebaseConfig.firebaseApp()`'s `GoogleCredentials.fromStream(...)`
call at boot — that call only *parses* a well-formed key locally, it never makes a network
call at startup. Safe to commit: it is never used for any real cryptographic operation
against Google's servers.

`project_id` is `demo-gradeops-smoke` — a `demo-*`-prefixed id, which the Firebase Auth
Emulator (`docker/firebase-emulator/`) accepts without requiring real project provisioning.

## How the mocking works

When `api/` runs with the `smoke` Spring profile (`application-smoke.yml`) **and** the
`FIREBASE_AUTH_EMULATOR_HOST` environment variable set (done by `scripts/smoke-test.sh`),
the Firebase Admin SDK redirects every `FirebaseAuth` call (`verifyIdToken`, `verifyTokenUnchecked`)
to the local emulator instead of real Firebase — this is a built-in Admin SDK behavior across
all languages, not something `api/`'s own code implements or is aware of. `FirebaseAuthAdapter`
and `FirebaseConfig` need zero changes to work against the emulator.

**Never use these files or the `smoke` profile in `demo`/`beta`/production.** They exist solely
for `compose.smoke.yml`-driven local verification.
