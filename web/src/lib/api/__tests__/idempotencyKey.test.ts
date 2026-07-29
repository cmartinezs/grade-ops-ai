import { createIdempotencyKey } from "../idempotencyKey";

// Tests simulate the browser's Web Crypto surface rather than weakening the production
// implementation for jsdom (jsdom provides crypto.getRandomValues but not crypto.randomUUID —
// see idempotencyKey.ts's own comment). globalThis.crypto is a non-configurable-assignment
// getter in this environment (plain `globalThis.crypto = x` silently no-ops), so every mock
// goes through Object.defineProperty, and every test restores the original afterward so no
// other suite observes a mocked crypto.
describe("createIdempotencyKey", () => {
  const originalCrypto = globalThis.crypto;

  function setGlobalCrypto(value: unknown) {
    Object.defineProperty(globalThis, "crypto", { value, configurable: true, writable: true });
  }

  afterEach(() => {
    setGlobalCrypto(originalCrypto);
    jest.restoreAllMocks();
  });

  it("uses crypto.randomUUID() when available, returning exactly its result", () => {
    const randomUUID = jest.fn(() => "11111111-1111-4111-8111-111111111111");
    setGlobalCrypto({ randomUUID, getRandomValues: jest.fn() });

    const key = createIdempotencyKey();

    expect(randomUUID).toHaveBeenCalledTimes(1);
    expect(key).toBe("11111111-1111-4111-8111-111111111111");
  });

  it("falls back to crypto.getRandomValues() when randomUUID is not available", () => {
    const getRandomValues = jest.fn((bytes: Uint8Array) => {
      bytes.fill(0xab);
      return bytes;
    });
    setGlobalCrypto({ getRandomValues });

    createIdempotencyKey();

    expect(getRandomValues).toHaveBeenCalledTimes(1);
    expect(getRandomValues.mock.calls[0][0]).toBeInstanceOf(Uint8Array);
    expect((getRandomValues.mock.calls[0][0] as Uint8Array).length).toBe(16);
  });

  it("the getRandomValues fallback produces a syntactically valid UUID v4 string", () => {
    setGlobalCrypto({
      getRandomValues: (bytes: Uint8Array) => {
        for (let i = 0; i < bytes.length; i++) bytes[i] = i * 17;
        return bytes;
      },
    });

    const key = createIdempotencyKey();

    expect(key).toMatch(/^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/);
  });

  it("the getRandomValues fallback sets the RFC 4122 version (4) and variant (10xx) bits regardless of the underlying random bytes", () => {
    // All-0x00 and all-0xff bytes are the two extremes — if version/variant masking is
    // correct, both must still produce the fixed version nibble and variant bits.
    for (const fill of [0x00, 0xff]) {
      setGlobalCrypto({
        getRandomValues: (bytes: Uint8Array) => {
          bytes.fill(fill);
          return bytes;
        },
      });

      const key = createIdempotencyKey();
      const versionNibble = key[14];
      const variantNibble = key[19];

      expect(versionNibble).toBe("4");
      expect(["8", "9", "a", "b"]).toContain(variantNibble);
    }
  });

  it("throws explicitly when Web Crypto is unavailable, instead of generating a weak key", () => {
    setGlobalCrypto(undefined);

    expect(() => createIdempotencyKey()).toThrow(/Web Crypto/i);
  });

  it("never calls Math.random(), on either the randomUUID or the getRandomValues path", () => {
    const mathRandomSpy = jest.spyOn(Math, "random");

    setGlobalCrypto({ randomUUID: () => "11111111-1111-4111-8111-111111111111" });
    createIdempotencyKey();

    setGlobalCrypto({ getRandomValues: (bytes: Uint8Array) => bytes.fill(0x42) });
    createIdempotencyKey();

    expect(mathRandomSpy).not.toHaveBeenCalled();
  });

  // Regression guard: the whole point of this module is to never use Math.random() for a
  // key that gates duplicate-Assessment/duplicate-LLM-dispatch protection. Read the source
  // directly so a future edit can't quietly reintroduce it under some new code path.
  it("regression guard: the source of idempotencyKey.ts never references Math.random", async () => {
    const fs = await import("fs");
    const path = await import("path");
    const source = fs.readFileSync(path.join(__dirname, "..", "idempotencyKey.ts"), "utf-8");

    expect(source).not.toMatch(/Math\.random/);
  });
});
