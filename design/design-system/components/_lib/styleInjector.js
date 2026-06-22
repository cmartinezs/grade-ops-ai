// Shared helper: inject a component's CSS rules into the document once.
// Keeps design-system components self-contained (inline-style objects can't do
// :hover / :focus / keyframes) without shipping a separate stylesheet.
const _injected = new Set();

export function injectStyle(id, css) {
  if (typeof document === "undefined") return;
  if (_injected.has(id)) return;
  _injected.add(id);
  const tag = document.createElement("style");
  tag.setAttribute("data-gops", id);
  tag.textContent = css;
  document.head.appendChild(tag);
}
