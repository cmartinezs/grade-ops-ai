// Correlation ids are for log tracing only, not security — a timestamp + random
// suffix avoids depending on crypto.randomUUID(), which real browsers/Node support
// but jsdom's test environment does not implement.
export function createCorrelationId(): string {
  return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 10)}`;
}
