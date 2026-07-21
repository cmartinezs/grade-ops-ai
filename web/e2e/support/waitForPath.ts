import { expect, type Page } from "@playwright/test";

// Next.js App Router client-side navigations (router.push) go through the History API plus
// an RSC fetch — they never fire a full-document 'load' event. page.waitForURL() defaults to
// waiting for 'load', so it hangs forever on these transitions (confirmed the hard way during
// task-13's manual walkthrough, where the same underlying Playwright wait primitive timed out
// even though the navigation had already happened). Poll the URL instead of trusting
// waitForURL's default lifecycle wait.
export async function waitForPath(page: Page, pathSuffix: string, timeoutMs = 15_000) {
  await expect
    .poll(() => new URL(page.url()).pathname, { timeout: timeoutMs })
    .toContain(pathSuffix);
}
