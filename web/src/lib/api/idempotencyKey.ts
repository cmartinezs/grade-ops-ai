// Idempotency keys gate duplicate-Assessment and duplicate-LLM-dispatch protection
// (see docs/99-decisions/2026-07-28-idempotency-and-concurrency-strategy.md), so they must
// come from a real source of randomness — the browser's Web Crypto API only, no weaker
// pseudo-random fallback. Tests simulate Web Crypto (mocking globalThis.crypto) rather than
// this module adapting to jsdom's gaps; production behavior is never weakened for tests.
function toHex(byte: number): string {
  return byte.toString(16).padStart(2, "0");
}

function bytesToUuidV4(bytes: Uint8Array): string {
  const hex = Array.from(bytes, toHex);
  return [
    hex.slice(0, 4).join(""),
    hex.slice(4, 6).join(""),
    hex.slice(6, 8).join(""),
    hex.slice(8, 10).join(""),
    hex.slice(10, 16).join(""),
  ].join("-");
}

export function createIdempotencyKey(): string {
  const cryptoApi = globalThis.crypto;

  if (!cryptoApi) {
    throw new Error("Web Crypto API is required to generate an idempotency key");
  }

  if (typeof cryptoApi.randomUUID === "function") {
    return cryptoApi.randomUUID();
  }

  // RFC 4122 v4 fallback for environments with getRandomValues but no randomUUID.
  const bytes = new Uint8Array(16);
  cryptoApi.getRandomValues(bytes);

  bytes[6] = (bytes[6] & 0x0f) | 0x40; // version 4
  bytes[8] = (bytes[8] & 0x3f) | 0x80; // variant RFC 4122 (10xx)

  return bytesToUuidV4(bytes);
}
