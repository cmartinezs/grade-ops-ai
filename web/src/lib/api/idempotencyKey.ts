// UUID v4 format without depending on crypto.randomUUID() — real browsers/Node support it,
// but jsdom's test environment does not (same constraint createCorrelationId avoids).
// Not cryptographically secure; sufficient for a client-generated idempotency correlation
// token, matching this codebase's existing precedent for client-side id generation.
export function createIdempotencyKey(): string {
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === "x" ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}
