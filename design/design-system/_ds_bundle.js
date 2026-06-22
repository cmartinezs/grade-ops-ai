/* @ds-bundle: {"format":3,"namespace":"GradeOpsAIDesignSystem_fcd12b","components":[{"name":"Avatar","sourcePath":"components/core/Avatar.jsx"},{"name":"Badge","sourcePath":"components/core/Badge.jsx"},{"name":"Button","sourcePath":"components/core/Button.jsx"},{"name":"Card","sourcePath":"components/core/Card.jsx"},{"name":"IconButton","sourcePath":"components/core/IconButton.jsx"},{"name":"Tag","sourcePath":"components/core/Tag.jsx"},{"name":"Tooltip","sourcePath":"components/core/Tooltip.jsx"},{"name":"RubricLevel","sourcePath":"components/data/RubricLevel.jsx"},{"name":"StatCard","sourcePath":"components/data/StatCard.jsx"},{"name":"Tabs","sourcePath":"components/data/Tabs.jsx"},{"name":"Dialog","sourcePath":"components/feedback/Dialog.jsx"},{"name":"ConfirmDialog","sourcePath":"components/feedback/Dialog.jsx"},{"name":"ProgressBar","sourcePath":"components/feedback/ProgressBar.jsx"},{"name":"Spinner","sourcePath":"components/feedback/Spinner.jsx"},{"name":"LoadingOverlay","sourcePath":"components/feedback/Spinner.jsx"},{"name":"Toast","sourcePath":"components/feedback/Toast.jsx"},{"name":"ToastViewport","sourcePath":"components/feedback/Toast.jsx"},{"name":"Checkbox","sourcePath":"components/forms/Checkbox.jsx"},{"name":"Field","sourcePath":"components/forms/Field.jsx"},{"name":"Input","sourcePath":"components/forms/Input.jsx"},{"name":"Radio","sourcePath":"components/forms/Radio.jsx"},{"name":"Select","sourcePath":"components/forms/Select.jsx"},{"name":"Switch","sourcePath":"components/forms/Switch.jsx"},{"name":"Textarea","sourcePath":"components/forms/Textarea.jsx"}],"sourceHashes":{"components/_lib/styleInjector.js":"978e69efcc24","components/core/Avatar.jsx":"76fcdae8ff4a","components/core/Badge.jsx":"86eb5bf594fa","components/core/Button.jsx":"d9d9c900b25c","components/core/Card.jsx":"6a21d2aa4698","components/core/IconButton.jsx":"4ac98e7c2a82","components/core/Tag.jsx":"6b3145819812","components/core/Tooltip.jsx":"539c41c0082c","components/data/RubricLevel.jsx":"28b4eb6e72bb","components/data/StatCard.jsx":"e111d676af46","components/data/Tabs.jsx":"6723647a3761","components/feedback/Dialog.jsx":"f1f7e62259ff","components/feedback/ProgressBar.jsx":"ee441fb6b7ad","components/feedback/Spinner.jsx":"77ba50afc05a","components/feedback/Toast.jsx":"417b4add9e63","components/forms/Checkbox.jsx":"ba5c616ebb45","components/forms/Field.jsx":"12f329e2744f","components/forms/Input.jsx":"eee726fc1c59","components/forms/Radio.jsx":"5db8d93a8da7","components/forms/Select.jsx":"a63c7286cbae","components/forms/Switch.jsx":"93a2a7f2dd75","components/forms/Textarea.jsx":"4218d18af7b3","ui_kits/student/AnswerScreen.jsx":"1b062b8e3dcc","ui_kits/student/ResultsScreen.jsx":"e2c4e11ff3d3","ui_kits/student/StudentApp.jsx":"f29bbcee97bd","ui_kits/student/StudentBar.jsx":"3ae7ad1b64b2","ui_kits/student/icons.jsx":"7b94297baab3","ui_kits/teacher/AssessmentsScreen.jsx":"93f4f8634f8d","ui_kits/teacher/BankScreen.jsx":"3bf9017c8860","ui_kits/teacher/BuilderScreen.jsx":"6c0f1b6bc411","ui_kits/teacher/DashboardScreen.jsx":"2dcaecfec60c","ui_kits/teacher/GradingScreen.jsx":"68868364dadd","ui_kits/teacher/LoginScreen.jsx":"beb9eec7f6c7","ui_kits/teacher/ReportsScreen.jsx":"ba523206fe15","ui_kits/teacher/Shell.jsx":"27795cf476c4","ui_kits/teacher/StudentDetail.jsx":"af41ee778f17","ui_kits/teacher/StudentsScreen.jsx":"6a7fb9c782e3","ui_kits/teacher/TeacherApp.jsx":"a10a57256499","ui_kits/teacher/icons.jsx":"0cf89dbd6713","ui_kits/teacher/teacherData.js":"d9e832a6cb7b"},"inlinedExternals":[],"unexposedExports":[{"name":"injectStyle","sourcePath":"components/_lib/styleInjector.js"}]} */

(() => {

const __ds_ns = (window.GradeOpsAIDesignSystem_fcd12b = window.GradeOpsAIDesignSystem_fcd12b || {});

const __ds_scope = {};

(__ds_ns.__errors = __ds_ns.__errors || []);

// components/_lib/styleInjector.js
try { (() => {
// Shared helper: inject a component's CSS rules into the document once.
// Keeps design-system components self-contained (inline-style objects can't do
// :hover / :focus / keyframes) without shipping a separate stylesheet.
const _injected = new Set();
function injectStyle(id, css) {
  if (typeof document === "undefined") return;
  if (_injected.has(id)) return;
  _injected.add(id);
  const tag = document.createElement("style");
  tag.setAttribute("data-gops", id);
  tag.textContent = css;
  document.head.appendChild(tag);
}
Object.assign(__ds_scope, { injectStyle });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/_lib/styleInjector.js", error: String((e && e.message) || e) }); }

// components/core/Avatar.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const CSS = `
.gops-avatar {
  display: inline-flex; align-items: center; justify-content: center;
  border-radius: 50%;
  background: var(--sprout-100);
  color: var(--sprout-800);
  font-family: var(--font-display);
  font-weight: var(--weight-semibold);
  overflow: hidden;
  flex: none;
  user-select: none;
  border: 2px solid var(--surface-card);
}
.gops-avatar img { width: 100%; height: 100%; object-fit: cover; }
.gops-avatar--xs { width: 24px; height: 24px; font-size: 10px; }
.gops-avatar--sm { width: 32px; height: 32px; font-size: 12px; }
.gops-avatar--md { width: 40px; height: 40px; font-size: 15px; }
.gops-avatar--lg { width: 56px; height: 56px; font-size: 20px; }
.gops-avatar--gold { background: var(--gold-100); color: var(--gold-800); }
.gops-avatar--slate { background: var(--slate-200); color: var(--slate-700); }
`;
const TONES = ["", "--gold", "--slate"];
function pickTone(name = "") {
  let h = 0;
  for (let i = 0; i < name.length; i++) h = (h * 31 + name.charCodeAt(i)) % 997;
  return TONES[h % TONES.length];
}
function initials(name = "") {
  const parts = name.trim().split(/\s+/).slice(0, 2);
  return parts.map(p => p[0]?.toUpperCase() || "").join("") || "?";
}

/** Circular user avatar with image or auto-initials fallback. */
function Avatar({
  name = "",
  src = null,
  size = "md",
  className = "",
  ...rest
}) {
  __ds_scope.injectStyle("gops-avatar", CSS);
  const tone = src ? "" : pickTone(name);
  const cls = ["gops-avatar", `gops-avatar--${size}`, tone ? `gops-avatar${tone}` : "", className].filter(Boolean).join(" ");
  return /*#__PURE__*/React.createElement("span", _extends({
    className: cls,
    title: name || undefined
  }, rest), src ? /*#__PURE__*/React.createElement("img", {
    src: src,
    alt: name
  }) : initials(name));
}
Object.assign(__ds_scope, { Avatar });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/Avatar.jsx", error: String((e && e.message) || e) }); }

// components/core/Badge.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const CSS = `
.gops-badge {
  display: inline-flex; align-items: center; gap: var(--space-1);
  font-family: var(--font-sans);
  font-weight: var(--weight-semibold);
  font-size: var(--text-xs);
  line-height: 1;
  letter-spacing: 0.01em;
  padding: 4px 9px;
  border-radius: var(--radius-pill);
  white-space: nowrap;
  border: 1px solid transparent;
}
.gops-badge__dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }
.gops-badge--neutral { background: var(--slate-100); color: var(--slate-700); border-color: var(--slate-200); }
.gops-badge--brand   { background: var(--sprout-50); color: var(--sprout-700); border-color: var(--sprout-200); }
.gops-badge--gold    { background: var(--gold-50); color: var(--gold-700); border-color: var(--gold-200); }
.gops-badge--success { background: var(--success-50); color: var(--success-700); border-color: color-mix(in srgb, var(--success-500) 28%, transparent); }
.gops-badge--warning { background: var(--warning-50); color: var(--warning-700); border-color: color-mix(in srgb, var(--warning-500) 30%, transparent); }
.gops-badge--danger  { background: var(--danger-50); color: var(--danger-700); border-color: color-mix(in srgb, var(--danger-500) 28%, transparent); }
.gops-badge--info    { background: var(--info-50); color: var(--info-700); border-color: color-mix(in srgb, var(--info-500) 26%, transparent); }
.gops-badge--solid   { background: var(--brand); color: #fff; border-color: transparent; }
`;

/** Small status / category pill. Use `dot` for live-status semantics. */
function Badge({
  tone = "neutral",
  dot = false,
  children,
  className = "",
  ...rest
}) {
  __ds_scope.injectStyle("gops-badge", CSS);
  const cls = ["gops-badge", `gops-badge--${tone}`, className].filter(Boolean).join(" ");
  return /*#__PURE__*/React.createElement("span", _extends({
    className: cls
  }, rest), dot && /*#__PURE__*/React.createElement("span", {
    className: "gops-badge__dot",
    "aria-hidden": "true"
  }), children);
}
Object.assign(__ds_scope, { Badge });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/Badge.jsx", error: String((e && e.message) || e) }); }

// components/core/Button.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const CSS = `
.gops-btn {
  --_bg: var(--brand);
  --_fg: #fff;
  --_bd: transparent;
  display: inline-flex; align-items: center; justify-content: center;
  gap: var(--space-2);
  font-family: var(--font-sans);
  font-weight: var(--weight-semibold);
  font-size: var(--text-md);
  line-height: 1;
  white-space: nowrap;
  border: var(--border-width) solid var(--_bd);
  background: var(--_bg);
  color: var(--_fg);
  border-radius: var(--radius-md);
  padding: 0 var(--space-4);
  height: 40px;
  cursor: pointer;
  transition: background var(--dur-fast) var(--ease-out),
              box-shadow var(--dur-fast) var(--ease-out),
              transform var(--dur-fast) var(--ease-out),
              border-color var(--dur-fast) var(--ease-out);
  user-select: none;
}
.gops-btn:focus-visible { outline: none; box-shadow: var(--ring); }
.gops-btn:active:not(:disabled) { transform: translateY(1px); }
.gops-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.gops-btn--sm { height: 32px; font-size: var(--text-sm); border-radius: var(--radius-sm); padding: 0 var(--space-3); }
.gops-btn--lg { height: 48px; font-size: var(--text-lg); padding: 0 var(--space-6); border-radius: var(--radius-lg); }
.gops-btn--block { width: 100%; }

.gops-btn--primary { --_bg: var(--brand); --_fg: #fff; }
.gops-btn--primary:hover:not(:disabled) { --_bg: var(--brand-hover); box-shadow: var(--shadow-brand); }
.gops-btn--primary:active:not(:disabled) { --_bg: var(--brand-active); }

.gops-btn--accent { --_bg: var(--accent); --_fg: var(--text-on-gold); }
.gops-btn--accent:hover:not(:disabled) { --_bg: var(--accent-hover); }

.gops-btn--secondary { --_bg: var(--surface-card); --_fg: var(--text-body); --_bd: var(--border-default); }
.gops-btn--secondary:hover:not(:disabled) { --_bg: var(--surface-sunken); --_bd: var(--border-strong); }

.gops-btn--ghost { --_bg: transparent; --_fg: var(--text-body); --_bd: transparent; }
.gops-btn--ghost:hover:not(:disabled) { --_bg: var(--surface-sunken); }

.gops-btn--danger { --_bg: var(--danger-500); --_fg: #fff; }
.gops-btn--danger:hover:not(:disabled) { --_bg: var(--danger-600); }

.gops-btn--quiet-danger { --_bg: transparent; --_fg: var(--danger-600); --_bd: transparent; }
.gops-btn--quiet-danger:hover:not(:disabled) { --_bg: var(--danger-50); }

.gops-btn__spinner {
  width: 1em; height: 1em; border-radius: 50%;
  border: 2px solid currentColor; border-right-color: transparent;
  animation: gops-btn-spin 0.6s linear infinite;
}
@keyframes gops-btn-spin { to { transform: rotate(360deg); } }
`;

/**
 * Primary action button. Async-aware: pass `loading` to show an inline spinner
 * (the GradeOps async convention — disable the control + show a loader in-place).
 */
function Button({
  variant = "primary",
  size = "md",
  block = false,
  loading = false,
  disabled = false,
  iconLeft = null,
  iconRight = null,
  children,
  className = "",
  ...rest
}) {
  __ds_scope.injectStyle("gops-btn", CSS);
  const cls = ["gops-btn", `gops-btn--${variant}`, size !== "md" ? `gops-btn--${size}` : "", block ? "gops-btn--block" : "", className].filter(Boolean).join(" ");
  return /*#__PURE__*/React.createElement("button", _extends({
    className: cls,
    disabled: disabled || loading,
    "aria-busy": loading || undefined
  }, rest), loading ? /*#__PURE__*/React.createElement("span", {
    className: "gops-btn__spinner",
    "aria-hidden": "true"
  }) : iconLeft, children && /*#__PURE__*/React.createElement("span", null, children), !loading && iconRight);
}
Object.assign(__ds_scope, { Button });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/Button.jsx", error: String((e && e.message) || e) }); }

// components/core/Card.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const CSS = `
.gops-card {
  background: var(--surface-card);
  border: var(--border-width) solid var(--border-subtle);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: clip;
}
.gops-card--flat { box-shadow: none; }
.gops-card--raised { box-shadow: var(--shadow-md); border-color: transparent; }
.gops-card--interactive { cursor: pointer; transition: box-shadow var(--dur-base) var(--ease-out), transform var(--dur-base) var(--ease-out), border-color var(--dur-base) var(--ease-out); }
.gops-card--interactive:hover { box-shadow: var(--shadow-lg); transform: translateY(-2px); border-color: var(--border-default); }
.gops-card--accent { border-top: 3px solid var(--brand); }
.gops-card__pad { padding: var(--space-5); }
.gops-card__header { padding: var(--space-4) var(--space-5); border-bottom: 1px solid var(--border-subtle); display: flex; align-items: center; justify-content: space-between; gap: var(--space-3); }
.gops-card__title { font-family: var(--font-display); font-weight: var(--weight-semibold); font-size: var(--text-lg); color: var(--text-strong); }
.gops-card__footer { padding: var(--space-4) var(--space-5); border-top: 1px solid var(--border-subtle); background: var(--surface-sunken); }
`;

/** Surface container. Compose with Card.Header / Card.Body / Card.Footer. */
function Card({
  elevation = "default",
  interactive = false,
  accent = false,
  className = "",
  children,
  ...rest
}) {
  __ds_scope.injectStyle("gops-card", CSS);
  const cls = ["gops-card", elevation === "flat" ? "gops-card--flat" : "", elevation === "raised" ? "gops-card--raised" : "", interactive ? "gops-card--interactive" : "", accent ? "gops-card--accent" : "", className].filter(Boolean).join(" ");
  return /*#__PURE__*/React.createElement("div", _extends({
    className: cls
  }, rest), children);
}
Card.Header = function CardHeader({
  children,
  ...rest
}) {
  return /*#__PURE__*/React.createElement("div", _extends({
    className: "gops-card__header"
  }, rest), children);
};
Card.Title = function CardTitle({
  children,
  ...rest
}) {
  return /*#__PURE__*/React.createElement("div", _extends({
    className: "gops-card__title"
  }, rest), children);
};
Card.Body = function CardBody({
  children,
  ...rest
}) {
  return /*#__PURE__*/React.createElement("div", _extends({
    className: "gops-card__pad"
  }, rest), children);
};
Card.Footer = function CardFooter({
  children,
  ...rest
}) {
  return /*#__PURE__*/React.createElement("div", _extends({
    className: "gops-card__footer"
  }, rest), children);
};
Object.assign(__ds_scope, { Card });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/Card.jsx", error: String((e && e.message) || e) }); }

// components/core/IconButton.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const CSS = `
.gops-iconbtn {
  display: inline-flex; align-items: center; justify-content: center;
  border: var(--border-width) solid transparent;
  background: transparent;
  color: var(--text-muted);
  border-radius: var(--radius-md);
  cursor: pointer;
  width: 40px; height: 40px;
  transition: background var(--dur-fast) var(--ease-out), color var(--dur-fast) var(--ease-out);
}
.gops-iconbtn svg { width: 20px; height: 20px; display: block; }
.gops-iconbtn:hover:not(:disabled) { background: var(--surface-sunken); color: var(--text-strong); }
.gops-iconbtn:focus-visible { outline: none; box-shadow: var(--ring); }
.gops-iconbtn:active:not(:disabled) { transform: translateY(1px); }
.gops-iconbtn:disabled { opacity: 0.45; cursor: not-allowed; }
.gops-iconbtn--sm { width: 32px; height: 32px; border-radius: var(--radius-sm); }
.gops-iconbtn--sm svg { width: 18px; height: 18px; }
.gops-iconbtn--solid { background: var(--brand); color: #fff; }
.gops-iconbtn--solid:hover:not(:disabled) { background: var(--brand-hover); color: #fff; }
.gops-iconbtn--outline { border-color: var(--border-default); color: var(--text-body); }
.gops-iconbtn--outline:hover:not(:disabled) { background: var(--surface-sunken); }
`;

/** Square icon-only button for toolbars, table rows and dense actions. */
function IconButton({
  variant = "ghost",
  size = "md",
  label,
  children,
  className = "",
  ...rest
}) {
  __ds_scope.injectStyle("gops-iconbtn", CSS);
  const cls = ["gops-iconbtn", variant !== "ghost" ? `gops-iconbtn--${variant}` : "", size === "sm" ? "gops-iconbtn--sm" : "", className].filter(Boolean).join(" ");
  return /*#__PURE__*/React.createElement("button", _extends({
    className: cls,
    "aria-label": label,
    title: label
  }, rest), children);
}
Object.assign(__ds_scope, { IconButton });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/IconButton.jsx", error: String((e && e.message) || e) }); }

// components/core/Tag.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const CSS = `
.gops-tag {
  display: inline-flex; align-items: center; gap: 5px;
  font-family: var(--font-sans); font-weight: var(--weight-medium); font-size: var(--text-xs);
  padding: 4px 9px; border-radius: var(--radius-sm);
  background: var(--surface-sunken); color: var(--text-body);
  border: 1px solid var(--border-default); white-space: nowrap; user-select: none;
  transition: background var(--dur-fast) var(--ease-out), border-color var(--dur-fast) var(--ease-out);
}
.gops-tag--brand { background: var(--sprout-50); color: var(--sprout-700); border-color: var(--sprout-200); }
.gops-tag--gold  { background: var(--gold-50);   color: var(--gold-700);   border-color: var(--gold-200); }
.gops-tag--info  { background: var(--info-50);   color: var(--info-700);   border-color: color-mix(in srgb, var(--info-500) 28%, transparent); }
.gops-tag--clickable { cursor: pointer; }
.gops-tag--clickable:hover { background: var(--slate-200); border-color: var(--border-strong); }
.gops-tag--brand.gops-tag--clickable:hover { background: var(--sprout-100); }
.gops-tag--gold.gops-tag--clickable:hover  { background: var(--gold-100); }
.gops-tag__dot { width: 7px; height: 7px; border-radius: 50%; background: currentColor; flex: none; }
.gops-tag__remove {
  width: 16px; height: 16px; border-radius: 50%;
  display: inline-flex; align-items: center; justify-content: center;
  border: none; background: none; cursor: pointer; padding: 0;
  color: inherit; opacity: 0.65; margin-right: -3px; flex: none;
  transition: opacity var(--dur-fast), background var(--dur-fast);
}
.gops-tag__remove:hover { opacity: 1; background: color-mix(in srgb, currentColor 16%, transparent); }
.gops-tag__remove svg { width: 11px; height: 11px; display: block; }
`;

/**
 * Filterable chip — subject/unit taxonomy filters, question-bank active filters,
 * selected-student groups. Unlike Badge, Tag supports an optional × remove button.
 */
function Tag({
  tone = "neutral",
  dot = false,
  children,
  onRemove,
  onClick,
  className = "",
  ...rest
}) {
  __ds_scope.injectStyle("gops-tag", CSS);
  const cls = ["gops-tag", tone !== "neutral" ? `gops-tag--${tone}` : "", onClick || onRemove ? "gops-tag--clickable" : "", className].filter(Boolean).join(" ");
  return /*#__PURE__*/React.createElement("span", _extends({
    className: cls,
    onClick: onClick,
    role: onClick ? "button" : undefined,
    tabIndex: onClick ? 0 : undefined
  }, rest), dot && /*#__PURE__*/React.createElement("span", {
    className: "gops-tag__dot",
    "aria-hidden": "true"
  }), children, onRemove && /*#__PURE__*/React.createElement("button", {
    className: "gops-tag__remove",
    onClick: e => {
      e.stopPropagation();
      onRemove();
    },
    "aria-label": "Quitar"
  }, /*#__PURE__*/React.createElement("svg", {
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    strokeWidth: "2.5",
    strokeLinecap: "round"
  }, /*#__PURE__*/React.createElement("path", {
    d: "M18 6L6 18M6 6l12 12"
  }))));
}
Object.assign(__ds_scope, { Tag });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/Tag.jsx", error: String((e && e.message) || e) }); }

// components/core/Tooltip.jsx
try { (() => {
const CSS = `
.gops-tip { position: relative; display: inline-flex; }
.gops-tip__pop {
  position: absolute; bottom: calc(100% + 7px); left: 50%;
  transform: translateX(-50%) translateY(4px);
  background: var(--slate-900); color: #fff;
  font-family: var(--font-sans); font-size: var(--text-xs); font-weight: var(--weight-medium);
  line-height: 1.4; max-width: 220px; text-wrap: pretty;
  padding: 5px 10px; border-radius: var(--radius-sm);
  box-shadow: var(--shadow-md); pointer-events: none;
  opacity: 0; transform: translateX(-50%) translateY(4px);
  transition: opacity var(--dur-fast) var(--ease-out), transform var(--dur-fast) var(--ease-out);
  z-index: var(--z-tooltip);
}
.gops-tip__pop::after {
  content: ""; position: absolute; top: 100%; left: 50%; transform: translateX(-50%);
  border: 4px solid transparent; border-top-color: var(--slate-900);
}
.gops-tip:hover .gops-tip__pop,
.gops-tip:focus-within .gops-tip__pop { opacity: 1; transform: translateX(-50%) translateY(0); }
.gops-tip--bottom .gops-tip__pop {
  bottom: auto; top: calc(100% + 7px);
  transform: translateX(-50%) translateY(-4px);
}
.gops-tip--bottom:hover .gops-tip__pop,
.gops-tip--bottom:focus-within .gops-tip__pop { transform: translateX(-50%) translateY(0); }
.gops-tip--bottom .gops-tip__pop::after {
  top: auto; bottom: 100%;
  border-top-color: transparent; border-bottom-color: var(--slate-900);
}
`;

/** Text tooltip on hover / focus — wrap any trigger element. */
function Tooltip({
  label,
  placement = "top",
  children,
  className = ""
}) {
  __ds_scope.injectStyle("gops-tip", CSS);
  const cls = ["gops-tip", placement === "bottom" ? "gops-tip--bottom" : "", className].filter(Boolean).join(" ");
  return /*#__PURE__*/React.createElement("span", {
    className: cls
  }, children, /*#__PURE__*/React.createElement("span", {
    className: "gops-tip__pop",
    role: "tooltip"
  }, label));
}
Object.assign(__ds_scope, { Tooltip });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/Tooltip.jsx", error: String((e && e.message) || e) }); }

// components/data/RubricLevel.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const CSS = `
.gops-rubric {
  display: inline-flex; align-items: center; gap: 7px;
  font-family: var(--font-sans); font-weight: var(--weight-semibold); font-size: var(--text-xs);
  padding: 4px 10px 4px 8px; border-radius: var(--radius-pill);
  border: 1px solid transparent; white-space: nowrap;
}
.gops-rubric__dot { width: 8px; height: 8px; border-radius: 2px; flex: none; }
.gops-rubric--exceeds    { background: color-mix(in srgb, var(--perf-exceeds) 12%, #fff); color: var(--perf-exceeds); border-color: color-mix(in srgb, var(--perf-exceeds) 30%, transparent); }
.gops-rubric--exceeds .gops-rubric__dot { background: var(--perf-exceeds); }
.gops-rubric--meets      { background: color-mix(in srgb, var(--perf-meets) 14%, #fff); color: var(--sprout-700); border-color: color-mix(in srgb, var(--perf-meets) 36%, transparent); }
.gops-rubric--meets .gops-rubric__dot { background: var(--perf-meets); }
.gops-rubric--developing { background: var(--gold-50); color: var(--gold-700); border-color: color-mix(in srgb, var(--perf-developing) 36%, transparent); }
.gops-rubric--developing .gops-rubric__dot { background: var(--perf-developing); }
.gops-rubric--beginning  { background: var(--warning-50); color: var(--warning-700); border-color: color-mix(in srgb, var(--perf-beginning) 34%, transparent); }
.gops-rubric--beginning .gops-rubric__dot { background: var(--perf-beginning); }
.gops-rubric--notmet     { background: var(--danger-50); color: var(--danger-700); border-color: color-mix(in srgb, var(--perf-notmet) 30%, transparent); }
.gops-rubric--notmet .gops-rubric__dot { background: var(--perf-notmet); }
.gops-rubric--solid { color: #fff !important; border-color: transparent !important; }
.gops-rubric--solid.gops-rubric--exceeds { background: var(--perf-exceeds); }
.gops-rubric--solid.gops-rubric--meets { background: var(--perf-meets); }
.gops-rubric--solid.gops-rubric--developing { background: var(--perf-developing); color: var(--slate-900) !important; }
.gops-rubric--solid.gops-rubric--beginning { background: var(--perf-beginning); color: var(--slate-900) !important; }
.gops-rubric--solid.gops-rubric--notmet { background: var(--perf-notmet); }
.gops-rubric--solid .gops-rubric__dot { display: none; }
`;
const LABELS = {
  exceeds: "Logrado destacado",
  meets: "Logrado",
  developing: "En desarrollo",
  beginning: "Inicial",
  notmet: "No logrado"
};

/**
 * Rubric performance-level badge for open assessments. Maps a level key to the
 * GradeOps achievement ladder (Logrado destacado → No logrado).
 */
function RubricLevel({
  level = "meets",
  label,
  solid = false,
  className = "",
  ...rest
}) {
  __ds_scope.injectStyle("gops-rubric", CSS);
  const cls = ["gops-rubric", `gops-rubric--${level}`, solid ? "gops-rubric--solid" : "", className].filter(Boolean).join(" ");
  return /*#__PURE__*/React.createElement("span", _extends({
    className: cls
  }, rest), /*#__PURE__*/React.createElement("span", {
    className: "gops-rubric__dot",
    "aria-hidden": "true"
  }), label || LABELS[level]);
}
Object.assign(__ds_scope, { RubricLevel });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/data/RubricLevel.jsx", error: String((e && e.message) || e) }); }

// components/data/StatCard.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const CSS = `
.gops-stat { background: var(--surface-card); border: 1px solid var(--border-subtle); border-radius: var(--radius-lg); padding: var(--space-5); box-shadow: var(--shadow-xs); }
.gops-stat__top { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 10px; }
.gops-stat__label { font-family: var(--font-sans); font-size: var(--text-sm); font-weight: var(--weight-medium); color: var(--text-muted); }
.gops-stat__icon { width: 34px; height: 34px; border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; background: var(--sprout-50); color: var(--brand); flex: none; }
.gops-stat__icon svg { width: 19px; height: 19px; }
.gops-stat__icon--gold { background: var(--gold-50); color: var(--gold-600); }
.gops-stat__icon--info { background: var(--info-50); color: var(--info-600); }
.gops-stat__icon--danger { background: var(--danger-50); color: var(--danger-600); }
.gops-stat__value { font-family: var(--font-display); font-weight: var(--weight-bold); font-size: var(--text-3xl); color: var(--text-strong); line-height: 1; letter-spacing: var(--tracking-tight); }
.gops-stat__value small { font-size: var(--text-lg); color: var(--text-muted); font-weight: var(--weight-medium); margin-left: 4px; }
.gops-stat__delta { display: inline-flex; align-items: center; gap: 3px; font-size: var(--text-xs); font-weight: var(--weight-semibold); margin-top: 8px; }
.gops-stat__delta--up { color: var(--success-600); }
.gops-stat__delta--down { color: var(--danger-600); }
.gops-stat__delta svg { width: 13px; height: 13px; }
`;

/** KPI tile for the teacher dashboard (promedio, entregas, por corregir…). */
function StatCard({
  label,
  value,
  unit,
  icon,
  iconTone = "brand",
  delta,
  deltaDir = "up",
  className = "",
  ...rest
}) {
  __ds_scope.injectStyle("gops-stat", CSS);
  return /*#__PURE__*/React.createElement("div", _extends({
    className: ["gops-stat", className].filter(Boolean).join(" ")
  }, rest), /*#__PURE__*/React.createElement("div", {
    className: "gops-stat__top"
  }, /*#__PURE__*/React.createElement("span", {
    className: "gops-stat__label"
  }, label), icon && /*#__PURE__*/React.createElement("span", {
    className: ["gops-stat__icon", iconTone !== "brand" ? `gops-stat__icon--${iconTone}` : ""].filter(Boolean).join(" ")
  }, icon)), /*#__PURE__*/React.createElement("div", {
    className: "gops-stat__value"
  }, value, unit && /*#__PURE__*/React.createElement("small", null, unit)), delta != null && /*#__PURE__*/React.createElement("div", {
    className: `gops-stat__delta gops-stat__delta--${deltaDir}`
  }, /*#__PURE__*/React.createElement("svg", {
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    strokeWidth: "2.5",
    strokeLinecap: "round",
    strokeLinejoin: "round"
  }, deltaDir === "up" ? /*#__PURE__*/React.createElement("path", {
    d: "M7 17L17 7M9 7h8v8"
  }) : /*#__PURE__*/React.createElement("path", {
    d: "M7 7l10 10M9 17h8V9"
  })), delta));
}
Object.assign(__ds_scope, { StatCard });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/data/StatCard.jsx", error: String((e && e.message) || e) }); }

// components/data/Tabs.jsx
try { (() => {
const CSS = `
.gops-tabs { display: flex; gap: 4px; border-bottom: 1px solid var(--border-subtle); }
.gops-tabs--pills { border-bottom: none; gap: 6px; background: var(--surface-sunken); padding: 4px; border-radius: var(--radius-md); width: fit-content; }
.gops-tab {
  appearance: none; border: none; background: none; cursor: pointer;
  font-family: var(--font-sans); font-weight: var(--weight-semibold); font-size: var(--text-md);
  color: var(--text-muted); padding: 10px 14px; position: relative;
  display: inline-flex; align-items: center; gap: 7px;
  transition: color var(--dur-fast) var(--ease-out);
}
.gops-tab:hover { color: var(--text-strong); }
.gops-tab--active { color: var(--brand); }
.gops-tab--active::after { content: ""; position: absolute; left: 10px; right: 10px; bottom: -1px; height: 2.5px; background: var(--brand); border-radius: 2px 2px 0 0; }
.gops-tabs--pills .gops-tab { border-radius: var(--radius-sm); padding: 7px 14px; }
.gops-tabs--pills .gops-tab--active { background: var(--surface-card); color: var(--text-strong); box-shadow: var(--shadow-xs); }
.gops-tabs--pills .gops-tab--active::after { display: none; }
.gops-tab__count { font-size: var(--text-xs); font-weight: var(--weight-semibold); background: var(--slate-200); color: var(--text-muted); padding: 1px 7px; border-radius: var(--radius-pill); }
.gops-tab--active .gops-tab__count { background: var(--sprout-100); color: var(--sprout-700); }
`;

/** Tab bar. `variant="pills"` for segmented control style. */
function Tabs({
  tabs = [],
  value,
  onChange,
  variant = "underline",
  className = ""
}) {
  __ds_scope.injectStyle("gops-tabs", CSS);
  const cls = ["gops-tabs", variant === "pills" ? "gops-tabs--pills" : "", className].filter(Boolean).join(" ");
  return /*#__PURE__*/React.createElement("div", {
    className: cls,
    role: "tablist"
  }, tabs.map(t => {
    const key = t.id ?? t.label;
    const active = key === value;
    return /*#__PURE__*/React.createElement("button", {
      key: key,
      role: "tab",
      "aria-selected": active,
      className: ["gops-tab", active ? "gops-tab--active" : ""].filter(Boolean).join(" "),
      onClick: () => onChange && onChange(key)
    }, t.icon, t.label, t.count != null && /*#__PURE__*/React.createElement("span", {
      className: "gops-tab__count"
    }, t.count));
  }));
}
Object.assign(__ds_scope, { Tabs });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/data/Tabs.jsx", error: String((e && e.message) || e) }); }

// components/feedback/Dialog.jsx
try { (() => {
const CSS = `
@keyframes gops-dialog-overlay { from { opacity: 0; } to { opacity: 1; } }
@keyframes gops-dialog-pop { from { opacity: 0; transform: translateY(12px) scale(0.97); } to { opacity: 1; transform: none; } }
.gops-dialog__overlay {
  position: fixed; inset: 0; z-index: var(--z-modal);
  background: color-mix(in srgb, var(--slate-950) 45%, transparent);
  backdrop-filter: blur(2px);
  display: flex; align-items: center; justify-content: center;
  padding: 20px;
  animation: gops-dialog-overlay var(--dur-base) var(--ease-out);
}
.gops-dialog {
  background: var(--surface-card);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  width: 100%; max-width: 440px;
  max-height: calc(100vh - 40px); overflow: auto;
  animation: gops-dialog-pop var(--dur-base) var(--ease-spring);
}
.gops-dialog--lg { max-width: 640px; }
.gops-dialog__head { display: flex; align-items: flex-start; gap: 14px; padding: 22px 22px 0; }
.gops-dialog__badge { flex: none; width: 44px; height: 44px; border-radius: var(--radius-md); display: flex; align-items: center; justify-content: center; }
.gops-dialog__badge svg { width: 24px; height: 24px; }
.gops-dialog__badge--danger { background: var(--danger-50); color: var(--danger-600); }
.gops-dialog__badge--warning { background: var(--warning-50); color: var(--warning-600); }
.gops-dialog__badge--brand { background: var(--sprout-50); color: var(--brand); }
.gops-dialog__title { font-family: var(--font-display); font-weight: var(--weight-semibold); font-size: var(--text-xl); color: var(--text-strong); line-height: 1.2; }
.gops-dialog__body { padding: 8px 22px 0; color: var(--text-muted); font-size: var(--text-md); line-height: var(--leading-normal); }
.gops-dialog__body--inset { padding-left: 80px; }
.gops-dialog__foot { display: flex; justify-content: flex-end; gap: 10px; padding: 22px; }
`;
const BADGE_ICON = {
  danger: /*#__PURE__*/React.createElement("path", {
    d: "M12 9v4m0 4h.01M10.3 4.3L2.6 18a2 2 0 001.7 3h15.4a2 2 0 001.7-3L13.7 4.3a2 2 0 00-3.4 0z"
  }),
  warning: /*#__PURE__*/React.createElement("path", {
    d: "M12 9v4m0 4h.01M10.3 4.3L2.6 18a2 2 0 001.7 3h15.4a2 2 0 001.7-3L13.7 4.3a2 2 0 00-3.4 0z"
  }),
  brand: /*#__PURE__*/React.createElement("path", {
    d: "M9 11l3 3L22 4M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11"
  })
};

/** Modal surface. Use Dialog for custom content; ConfirmDialog for confirms. */
function Dialog({
  open,
  onClose,
  title,
  icon,
  size = "md",
  children,
  footer
}) {
  __ds_scope.injectStyle("gops-dialog", CSS);
  if (!open) return null;
  return /*#__PURE__*/React.createElement("div", {
    className: "gops-dialog__overlay",
    onMouseDown: e => {
      if (e.target === e.currentTarget && onClose) onClose();
    }
  }, /*#__PURE__*/React.createElement("div", {
    className: ["gops-dialog", size === "lg" ? "gops-dialog--lg" : ""].filter(Boolean).join(" "),
    role: "dialog",
    "aria-modal": "true"
  }, (title || icon) && /*#__PURE__*/React.createElement("div", {
    className: "gops-dialog__head"
  }, icon && /*#__PURE__*/React.createElement("div", {
    className: `gops-dialog__badge gops-dialog__badge--${icon}`
  }, /*#__PURE__*/React.createElement("svg", {
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    strokeWidth: "2",
    strokeLinecap: "round",
    strokeLinejoin: "round"
  }, BADGE_ICON[icon])), title && /*#__PURE__*/React.createElement("div", {
    className: "gops-dialog__title"
  }, title)), /*#__PURE__*/React.createElement("div", {
    className: ["gops-dialog__body", icon ? "gops-dialog__body--inset" : ""].filter(Boolean).join(" ")
  }, children), footer && /*#__PURE__*/React.createElement("div", {
    className: "gops-dialog__foot"
  }, footer)));
}

/**
 * Confirmation dialog — the GradeOps "confirm before any data-mutating or
 * leave-with-unsaved action" pattern. Pass `loading` to keep it open with a
 * spinner while the async action resolves.
 */
function ConfirmDialog({
  open,
  onCancel,
  onConfirm,
  title = "¿Confirmar acción?",
  message,
  confirmLabel = "Confirmar",
  cancelLabel = "Cancelar",
  tone = "brand",
  loading = false
}) {
  const confirmVariant = tone === "danger" ? "danger" : "primary";
  return /*#__PURE__*/React.createElement(Dialog, {
    open: open,
    onClose: loading ? undefined : onCancel,
    title: title,
    icon: tone,
    footer: /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement(__ds_scope.Button, {
      variant: "ghost",
      onClick: onCancel,
      disabled: loading
    }, cancelLabel), /*#__PURE__*/React.createElement(__ds_scope.Button, {
      variant: confirmVariant,
      onClick: onConfirm,
      loading: loading
    }, confirmLabel))
  }, message);
}
Object.assign(__ds_scope, { Dialog, ConfirmDialog });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/Dialog.jsx", error: String((e && e.message) || e) }); }

// components/feedback/ProgressBar.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const CSS = `
.gops-progress { width: 100%; }
.gops-progress__head { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 6px; }
.gops-progress__label { font-family: var(--font-sans); font-size: var(--text-sm); font-weight: var(--weight-medium); color: var(--text-body); }
.gops-progress__value { font-family: var(--font-mono); font-size: var(--text-xs); color: var(--text-muted); }
.gops-progress__track { height: 8px; border-radius: var(--radius-pill); background: var(--slate-200); overflow: hidden; }
.gops-progress__track--lg { height: 12px; }
.gops-progress__fill { height: 100%; border-radius: inherit; background: var(--brand); transition: width var(--dur-slow) var(--ease-out); }
.gops-progress__fill--gold { background: var(--accent); }
.gops-progress__fill--danger { background: var(--danger-500); }
.gops-progress__fill--success { background: var(--success-500); }
.gops-progress__fill--striped { background-image: linear-gradient(45deg, rgba(255,255,255,.25) 25%, transparent 25%, transparent 50%, rgba(255,255,255,.25) 50%, rgba(255,255,255,.25) 75%, transparent 75%); background-size: 16px 16px; animation: gops-stripe 0.8s linear infinite; }
@keyframes gops-stripe { to { background-position: 16px 0; } }
`;

/** Determinate progress bar — bulk grading, upload, course completion. */
function ProgressBar({
  value = 0,
  max = 100,
  label,
  showValue = false,
  tone = "brand",
  size = "md",
  striped = false,
  className = "",
  ...rest
}) {
  __ds_scope.injectStyle("gops-progress", CSS);
  const pct = Math.max(0, Math.min(100, value / max * 100));
  const fill = ["gops-progress__fill", tone !== "brand" ? `gops-progress__fill--${tone}` : "", striped ? "gops-progress__fill--striped" : ""].filter(Boolean).join(" ");
  return /*#__PURE__*/React.createElement("div", _extends({
    className: ["gops-progress", className].filter(Boolean).join(" ")
  }, rest), (label || showValue) && /*#__PURE__*/React.createElement("div", {
    className: "gops-progress__head"
  }, label && /*#__PURE__*/React.createElement("span", {
    className: "gops-progress__label"
  }, label), showValue && /*#__PURE__*/React.createElement("span", {
    className: "gops-progress__value"
  }, Math.round(pct), "%")), /*#__PURE__*/React.createElement("div", {
    className: ["gops-progress__track", size === "lg" ? "gops-progress__track--lg" : ""].filter(Boolean).join(" "),
    role: "progressbar",
    "aria-valuenow": Math.round(pct),
    "aria-valuemin": 0,
    "aria-valuemax": 100
  }, /*#__PURE__*/React.createElement("div", {
    className: fill,
    style: {
      width: pct + "%"
    }
  })));
}
Object.assign(__ds_scope, { ProgressBar });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/ProgressBar.jsx", error: String((e && e.message) || e) }); }

// components/feedback/Spinner.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const CSS = `
@keyframes gops-spin { to { transform: rotate(360deg); } }
.gops-spinner {
  display: inline-block; border-radius: 50%;
  border-style: solid; border-color: currentColor;
  border-right-color: transparent !important;
  animation: gops-spin 0.6s linear infinite;
  vertical-align: -0.15em;
}
.gops-spinner--xs { width: 14px; height: 14px; border-width: 2px; }
.gops-spinner--sm { width: 18px; height: 18px; border-width: 2px; }
.gops-spinner--md { width: 24px; height: 24px; border-width: 2.5px; }
.gops-spinner--lg { width: 36px; height: 36px; border-width: 3px; }
.gops-loadingoverlay { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; gap: 10px; background: color-mix(in srgb, var(--surface-card) 70%, transparent); backdrop-filter: blur(1.5px); border-radius: inherit; color: var(--brand); font-size: var(--text-sm); font-weight: var(--weight-medium); z-index: 2; }
`;

/** Inline loading spinner — the GradeOps async indicator. Inherits text color. */
function Spinner({
  size = "sm",
  className = "",
  style,
  ...rest
}) {
  __ds_scope.injectStyle("gops-spinner", CSS);
  const cls = ["gops-spinner", `gops-spinner--${size}`, className].filter(Boolean).join(" ");
  return /*#__PURE__*/React.createElement("span", _extends({
    className: cls,
    role: "status",
    "aria-label": "Cargando",
    style: style
  }, rest));
}

/** Overlay a component with a centered spinner while an async action runs.
 *  Wrap content in a position:relative parent. */
function LoadingOverlay({
  label = "Procesando…",
  size = "md"
}) {
  __ds_scope.injectStyle("gops-spinner", CSS);
  return /*#__PURE__*/React.createElement("div", {
    className: "gops-loadingoverlay"
  }, /*#__PURE__*/React.createElement(Spinner, {
    size: size
  }), label && /*#__PURE__*/React.createElement("span", null, label));
}
Object.assign(__ds_scope, { Spinner, LoadingOverlay });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/Spinner.jsx", error: String((e && e.message) || e) }); }

// components/feedback/Toast.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const CSS = `
@keyframes gops-toast-in { from { opacity: 0; transform: translateY(10px) scale(0.98); } to { opacity: 1; transform: none; } }
.gops-toastviewport {
  position: fixed; bottom: 20px; right: 20px;
  display: flex; flex-direction: column; gap: 10px;
  z-index: var(--z-toast); width: min(380px, calc(100vw - 40px));
  pointer-events: none;
}
.gops-toast {
  pointer-events: auto;
  display: flex; align-items: flex-start; gap: 12px;
  background: var(--surface-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  padding: 12px 14px;
  animation: gops-toast-in var(--dur-base) var(--ease-spring);
}
.gops-toast__icon { flex: none; width: 22px; height: 22px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin-top: 1px; }
.gops-toast__icon svg { width: 14px; height: 14px; }
.gops-toast__body { flex: 1; min-width: 0; }
.gops-toast__title { font-family: var(--font-sans); font-weight: var(--weight-semibold); font-size: var(--text-md); color: var(--text-strong); line-height: 1.3; }
.gops-toast__msg { font-size: var(--text-sm); color: var(--text-muted); margin-top: 2px; }
.gops-toast__close { flex: none; background: none; border: none; cursor: pointer; color: var(--text-subtle); padding: 2px; border-radius: var(--radius-xs); display: flex; }
.gops-toast__close:hover { color: var(--text-strong); background: var(--surface-sunken); }
.gops-toast__close svg { width: 16px; height: 16px; }
.gops-toast--success .gops-toast__icon { background: var(--success-50); color: var(--success-600); }
.gops-toast--error   .gops-toast__icon { background: var(--danger-50); color: var(--danger-600); }
.gops-toast--info    .gops-toast__icon { background: var(--info-50); color: var(--info-600); }
.gops-toast--loading .gops-toast__icon { background: var(--sprout-50); color: var(--brand); }
`;
const ICONS = {
  success: /*#__PURE__*/React.createElement("path", {
    d: "M5 12l5 5L20 7"
  }),
  error: /*#__PURE__*/React.createElement("path", {
    d: "M18 6L6 18M6 6l12 12"
  }),
  info: /*#__PURE__*/React.createElement("path", {
    d: "M12 8h.01M11 12h1v4h1"
  })
};

/** Single toast. Used by ToastViewport; `loading` shows a spinner icon. */
function Toast({
  tone = "info",
  title,
  message,
  loading = false,
  onClose
}) {
  __ds_scope.injectStyle("gops-toast", CSS);
  return /*#__PURE__*/React.createElement("div", {
    className: `gops-toast gops-toast--${loading ? "loading" : tone}`,
    role: "status"
  }, /*#__PURE__*/React.createElement("span", {
    className: "gops-toast__icon",
    "aria-hidden": "true"
  }, loading ? /*#__PURE__*/React.createElement("span", {
    style: {
      width: 14,
      height: 14,
      border: "2px solid currentColor",
      borderRightColor: "transparent",
      borderRadius: "50%",
      animation: "gops-spin 0.6s linear infinite"
    }
  }) : /*#__PURE__*/React.createElement("svg", {
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    strokeWidth: "2.4",
    strokeLinecap: "round",
    strokeLinejoin: "round"
  }, ICONS[tone] || ICONS.info)), /*#__PURE__*/React.createElement("div", {
    className: "gops-toast__body"
  }, title && /*#__PURE__*/React.createElement("div", {
    className: "gops-toast__title"
  }, title), message && /*#__PURE__*/React.createElement("div", {
    className: "gops-toast__msg"
  }, message)), onClose && /*#__PURE__*/React.createElement("button", {
    className: "gops-toast__close",
    onClick: onClose,
    "aria-label": "Cerrar"
  }, /*#__PURE__*/React.createElement("svg", {
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    strokeWidth: "2",
    strokeLinecap: "round"
  }, /*#__PURE__*/React.createElement("path", {
    d: "M18 6L6 18M6 6l12 12"
  }))));
}

/** Fixed bottom-right stack. Pass an array of toast objects + onDismiss(id). */
function ToastViewport({
  toasts = [],
  onDismiss
}) {
  __ds_scope.injectStyle("gops-toast", CSS);
  if (!toasts.length) return null;
  return /*#__PURE__*/React.createElement("div", {
    className: "gops-toastviewport"
  }, toasts.map(t => /*#__PURE__*/React.createElement(Toast, _extends({
    key: t.id
  }, t, {
    onClose: onDismiss ? () => onDismiss(t.id) : undefined
  }))));
}
Object.assign(__ds_scope, { Toast, ToastViewport });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/Toast.jsx", error: String((e && e.message) || e) }); }

// components/forms/Checkbox.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const CSS = `
.gops-check { display: inline-flex; align-items: flex-start; gap: 10px; cursor: pointer; font-family: var(--font-sans); font-size: var(--text-md); color: var(--text-body); user-select: none; }
.gops-check input { position: absolute; opacity: 0; width: 0; height: 0; }
.gops-check__box {
  flex: none; width: 20px; height: 20px; margin-top: 1px;
  border: 1.5px solid var(--border-strong);
  border-radius: var(--radius-xs);
  background: var(--surface-card);
  display: inline-flex; align-items: center; justify-content: center;
  color: #fff;
  transition: background var(--dur-fast) var(--ease-out), border-color var(--dur-fast) var(--ease-out);
}
.gops-check__box svg { width: 14px; height: 14px; opacity: 0; transform: scale(0.6); transition: opacity var(--dur-fast) var(--ease-out), transform var(--dur-fast) var(--ease-spring); }
.gops-check input:checked + .gops-check__box { background: var(--brand); border-color: var(--brand); }
.gops-check input:checked + .gops-check__box svg { opacity: 1; transform: scale(1); }
.gops-check input:focus-visible + .gops-check__box { box-shadow: var(--ring); }
.gops-check input:disabled + .gops-check__box { background: var(--surface-sunken); border-color: var(--border-default); }
.gops-check--disabled { cursor: not-allowed; color: var(--text-disabled); }
.gops-check__text { display: flex; flex-direction: column; gap: 1px; }
.gops-check__desc { font-size: var(--text-xs); color: var(--text-muted); }
`;

/** Checkbox for multi-select answer keys, settings, and bulk row selection. */
function Checkbox({
  label,
  description,
  disabled = false,
  className = "",
  ...rest
}) {
  __ds_scope.injectStyle("gops-check", CSS);
  const cls = ["gops-check", disabled ? "gops-check--disabled" : "", className].filter(Boolean).join(" ");
  return /*#__PURE__*/React.createElement("label", {
    className: cls
  }, /*#__PURE__*/React.createElement("input", _extends({
    type: "checkbox",
    disabled: disabled
  }, rest)), /*#__PURE__*/React.createElement("span", {
    className: "gops-check__box",
    "aria-hidden": "true"
  }, /*#__PURE__*/React.createElement("svg", {
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    strokeWidth: "3",
    strokeLinecap: "round",
    strokeLinejoin: "round"
  }, /*#__PURE__*/React.createElement("path", {
    d: "M5 12l5 5L20 7"
  }))), (label || description) && /*#__PURE__*/React.createElement("span", {
    className: "gops-check__text"
  }, label && /*#__PURE__*/React.createElement("span", null, label), description && /*#__PURE__*/React.createElement("span", {
    className: "gops-check__desc"
  }, description)));
}
Object.assign(__ds_scope, { Checkbox });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/Checkbox.jsx", error: String((e && e.message) || e) }); }

// components/forms/Field.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const CSS = `
.gops-field { display: flex; flex-direction: column; gap: 6px; }
.gops-field__label { font-family: var(--font-sans); font-weight: var(--weight-semibold); font-size: var(--text-sm); color: var(--text-strong); display: flex; align-items: center; gap: 6px; }
.gops-field__req { color: var(--danger-500); }
.gops-field__opt { color: var(--text-subtle); font-weight: var(--weight-regular); font-size: var(--text-xs); }
.gops-field__hint { font-size: var(--text-xs); color: var(--text-muted); }
.gops-field__error { font-size: var(--text-xs); color: var(--danger-600); font-weight: var(--weight-medium); display: flex; align-items: center; gap: 5px; }
`;

/** Form field wrapper: label + required/optional marker + hint/error text. */
function Field({
  label,
  htmlFor,
  required = false,
  optional = false,
  hint,
  error,
  children,
  className = "",
  ...rest
}) {
  __ds_scope.injectStyle("gops-field", CSS);
  return /*#__PURE__*/React.createElement("div", _extends({
    className: ["gops-field", className].filter(Boolean).join(" ")
  }, rest), label && /*#__PURE__*/React.createElement("label", {
    className: "gops-field__label",
    htmlFor: htmlFor
  }, label, required && /*#__PURE__*/React.createElement("span", {
    className: "gops-field__req",
    "aria-hidden": "true"
  }, "*"), optional && /*#__PURE__*/React.createElement("span", {
    className: "gops-field__opt"
  }, "(opcional)")), children, error ? /*#__PURE__*/React.createElement("span", {
    className: "gops-field__error"
  }, error) : hint && /*#__PURE__*/React.createElement("span", {
    className: "gops-field__hint"
  }, hint));
}
Object.assign(__ds_scope, { Field });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/Field.jsx", error: String((e && e.message) || e) }); }

// components/forms/Input.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const CSS = `
.gops-input {
  font-family: var(--font-sans);
  font-size: var(--text-md);
  color: var(--text-body);
  background: var(--surface-card);
  border: var(--border-width) solid var(--border-default);
  border-radius: var(--radius-md);
  height: 40px;
  padding: 0 var(--space-3);
  width: 100%;
  transition: border-color var(--dur-fast) var(--ease-out), box-shadow var(--dur-fast) var(--ease-out);
}
.gops-input::placeholder { color: var(--text-disabled); }
.gops-input:hover:not(:disabled) { border-color: var(--border-strong); }
.gops-input:focus { outline: none; border-color: var(--border-focus); box-shadow: var(--ring); }
.gops-input:disabled { background: var(--surface-sunken); color: var(--text-disabled); cursor: not-allowed; }
.gops-input--invalid { border-color: var(--danger-500); }
.gops-input--invalid:focus { box-shadow: 0 0 0 3px color-mix(in srgb, var(--danger-500) 30%, transparent); }
.gops-input--sm { height: 32px; font-size: var(--text-sm); }
.gops-input--lg { height: 48px; font-size: var(--text-lg); }
.gops-input--mono { font-family: var(--font-mono); letter-spacing: 0.04em; }

.gops-inputwrap { position: relative; display: flex; align-items: center; }
.gops-inputwrap .gops-input { padding-left: 38px; }
.gops-inputwrap__icon { position: absolute; left: 11px; display: flex; color: var(--text-subtle); pointer-events: none; }
.gops-inputwrap__icon svg { width: 18px; height: 18px; }
`;

/** Text input. Pass `icon` for a leading glyph, `invalid` for the error border. */
function Input({
  size = "md",
  invalid = false,
  mono = false,
  icon = null,
  className = "",
  ...rest
}) {
  __ds_scope.injectStyle("gops-input", CSS);
  const cls = ["gops-input", size !== "md" ? `gops-input--${size}` : "", invalid ? "gops-input--invalid" : "", mono ? "gops-input--mono" : "", className].filter(Boolean).join(" ");
  const input = /*#__PURE__*/React.createElement("input", _extends({
    className: cls,
    "aria-invalid": invalid || undefined
  }, rest));
  if (!icon) return input;
  return /*#__PURE__*/React.createElement("span", {
    className: "gops-inputwrap"
  }, /*#__PURE__*/React.createElement("span", {
    className: "gops-inputwrap__icon",
    "aria-hidden": "true"
  }, icon), input);
}
Object.assign(__ds_scope, { Input });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/Input.jsx", error: String((e && e.message) || e) }); }

// components/forms/Radio.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const CSS = `
.gops-radio { display: inline-flex; align-items: flex-start; gap: 10px; cursor: pointer; font-family: var(--font-sans); font-size: var(--text-md); color: var(--text-body); user-select: none; }
.gops-radio input { position: absolute; opacity: 0; width: 0; height: 0; }
.gops-radio__dot {
  flex: none; width: 20px; height: 20px; margin-top: 1px;
  border: 1.5px solid var(--border-strong);
  border-radius: 50%;
  background: var(--surface-card);
  display: inline-flex; align-items: center; justify-content: center;
  transition: border-color var(--dur-fast) var(--ease-out);
}
.gops-radio__dot::after { content: ""; width: 10px; height: 10px; border-radius: 50%; background: var(--brand); transform: scale(0); transition: transform var(--dur-fast) var(--ease-spring); }
.gops-radio input:checked + .gops-radio__dot { border-color: var(--brand); }
.gops-radio input:checked + .gops-radio__dot::after { transform: scale(1); }
.gops-radio input:focus-visible + .gops-radio__dot { box-shadow: var(--ring); }
.gops-radio input:disabled + .gops-radio__dot { background: var(--surface-sunken); }
.gops-radio--disabled { cursor: not-allowed; color: var(--text-disabled); }
.gops-radio__text { display: flex; flex-direction: column; gap: 1px; }
.gops-radio__desc { font-size: var(--text-xs); color: var(--text-muted); }
`;

/** Single-choice radio for single-answer keys and exclusive options. */
function Radio({
  label,
  description,
  disabled = false,
  className = "",
  ...rest
}) {
  __ds_scope.injectStyle("gops-radio", CSS);
  const cls = ["gops-radio", disabled ? "gops-radio--disabled" : "", className].filter(Boolean).join(" ");
  return /*#__PURE__*/React.createElement("label", {
    className: cls
  }, /*#__PURE__*/React.createElement("input", _extends({
    type: "radio",
    disabled: disabled
  }, rest)), /*#__PURE__*/React.createElement("span", {
    className: "gops-radio__dot",
    "aria-hidden": "true"
  }), (label || description) && /*#__PURE__*/React.createElement("span", {
    className: "gops-radio__text"
  }, label && /*#__PURE__*/React.createElement("span", null, label), description && /*#__PURE__*/React.createElement("span", {
    className: "gops-radio__desc"
  }, description)));
}
Object.assign(__ds_scope, { Radio });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/Radio.jsx", error: String((e && e.message) || e) }); }

// components/forms/Select.jsx
try { (() => {
const CSS = `
.gops-select { position: relative; display: inline-flex; width: 100%; }
.gops-select select {
  appearance: none; -webkit-appearance: none;
  font-family: var(--font-sans);
  font-size: var(--text-md);
  color: var(--text-body);
  background: var(--surface-card);
  border: var(--border-width) solid var(--border-default);
  border-radius: var(--radius-md);
  height: 40px;
  padding: 0 38px 0 var(--space-3);
  width: 100%;
  cursor: pointer;
  transition: border-color var(--dur-fast) var(--ease-out), box-shadow var(--dur-fast) var(--ease-out);
}
.gops-select select:hover:not(:disabled) { border-color: var(--border-strong); }
.gops-select select:focus { outline: none; border-color: var(--border-focus); box-shadow: var(--ring); }
.gops-select select:disabled { background: var(--surface-sunken); color: var(--text-disabled); cursor: not-allowed; }
.gops-select__chevron { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); pointer-events: none; color: var(--text-subtle); }
.gops-select__chevron svg { width: 18px; height: 18px; display: block; }
.gops-select--sm select { height: 32px; font-size: var(--text-sm); }
`;

/** Native select styled to match the system, with chevron affordance. */
function Select({
  size = "md",
  children,
  className = "",
  ...rest
}) {
  __ds_scope.injectStyle("gops-select", CSS);
  const cls = ["gops-select", size === "sm" ? "gops-select--sm" : "", className].filter(Boolean).join(" ");
  return /*#__PURE__*/React.createElement("span", {
    className: cls
  }, /*#__PURE__*/React.createElement("select", rest, children), /*#__PURE__*/React.createElement("span", {
    className: "gops-select__chevron",
    "aria-hidden": "true"
  }, /*#__PURE__*/React.createElement("svg", {
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    strokeWidth: "2",
    strokeLinecap: "round",
    strokeLinejoin: "round"
  }, /*#__PURE__*/React.createElement("path", {
    d: "M6 9l6 6 6-6"
  }))));
}
Object.assign(__ds_scope, { Select });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/Select.jsx", error: String((e && e.message) || e) }); }

// components/forms/Switch.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const CSS = `
.gops-switch { display: inline-flex; align-items: center; gap: 10px; cursor: pointer; font-family: var(--font-sans); font-size: var(--text-md); color: var(--text-body); user-select: none; }
.gops-switch input { position: absolute; opacity: 0; width: 0; height: 0; }
.gops-switch__track {
  position: relative; flex: none;
  width: 40px; height: 24px; border-radius: var(--radius-pill);
  background: var(--slate-300);
  transition: background var(--dur-base) var(--ease-out);
}
.gops-switch__thumb {
  position: absolute; top: 3px; left: 3px;
  width: 18px; height: 18px; border-radius: 50%;
  background: #fff; box-shadow: var(--shadow-sm);
  transition: transform var(--dur-base) var(--ease-spring);
}
.gops-switch input:checked + .gops-switch__track { background: var(--brand); }
.gops-switch input:checked + .gops-switch__track .gops-switch__thumb { transform: translateX(16px); }
.gops-switch input:focus-visible + .gops-switch__track { box-shadow: var(--ring); }
.gops-switch input:disabled + .gops-switch__track { opacity: 0.5; }
.gops-switch--disabled { cursor: not-allowed; color: var(--text-disabled); }
`;

/** On/off toggle for assessment settings (late submissions, penalties, global). */
function Switch({
  label,
  disabled = false,
  className = "",
  ...rest
}) {
  __ds_scope.injectStyle("gops-switch", CSS);
  const cls = ["gops-switch", disabled ? "gops-switch--disabled" : "", className].filter(Boolean).join(" ");
  return /*#__PURE__*/React.createElement("label", {
    className: cls
  }, /*#__PURE__*/React.createElement("input", _extends({
    type: "checkbox",
    role: "switch",
    disabled: disabled
  }, rest)), /*#__PURE__*/React.createElement("span", {
    className: "gops-switch__track",
    "aria-hidden": "true"
  }, /*#__PURE__*/React.createElement("span", {
    className: "gops-switch__thumb"
  })), label && /*#__PURE__*/React.createElement("span", null, label));
}
Object.assign(__ds_scope, { Switch });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/Switch.jsx", error: String((e && e.message) || e) }); }

// components/forms/Textarea.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const CSS = `
.gops-textarea {
  font-family: var(--font-sans);
  font-size: var(--text-md);
  color: var(--text-body);
  background: var(--surface-card);
  border: var(--border-width) solid var(--border-default);
  border-radius: var(--radius-md);
  padding: var(--space-3);
  width: 100%;
  min-height: 96px;
  line-height: var(--leading-normal);
  resize: vertical;
  transition: border-color var(--dur-fast) var(--ease-out), box-shadow var(--dur-fast) var(--ease-out);
}
.gops-textarea::placeholder { color: var(--text-disabled); }
.gops-textarea:hover:not(:disabled) { border-color: var(--border-strong); }
.gops-textarea:focus { outline: none; border-color: var(--border-focus); box-shadow: var(--ring); }
.gops-textarea:disabled { background: var(--surface-sunken); color: var(--text-disabled); cursor: not-allowed; }
.gops-textarea--invalid { border-color: var(--danger-500); }
`;

/** Multi-line text input for statements, rubric criteria, feedback notes. */
function Textarea({
  invalid = false,
  className = "",
  ...rest
}) {
  __ds_scope.injectStyle("gops-textarea", CSS);
  const cls = ["gops-textarea", invalid ? "gops-textarea--invalid" : "", className].filter(Boolean).join(" ");
  return /*#__PURE__*/React.createElement("textarea", _extends({
    className: cls,
    "aria-invalid": invalid || undefined
  }, rest));
}
Object.assign(__ds_scope, { Textarea });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/Textarea.jsx", error: String((e && e.message) || e) }); }

// ui_kits/student/AnswerScreen.jsx
try { (() => {
// Student answering a closed/mixed assessment via magic link.
const {
  Card,
  Badge,
  Button,
  Radio,
  Checkbox,
  Textarea,
  ProgressBar
} = window.GradeOpsAIDesignSystem_fcd12b;
const QUESTIONS = [{
  kind: "single",
  text: "¿Cuál es el principal producto energético de la fase lumínica?",
  opts: ["Glucosa", "ATP y NADPH", "Dióxido de carbono", "Oxígeno molecular"],
  pts: 10
}, {
  kind: "multi",
  text: "Selecciona las moléculas producidas en el ciclo de Calvin.",
  opts: ["G3P", "ATP (consumido)", "Glucosa", "Clorofila"],
  pts: 15
}, {
  kind: "tf",
  text: "La clorofila absorbe principalmente la luz de color verde.",
  opts: ["Verdadero", "Falso"],
  pts: 5
}, {
  kind: "open",
  text: "Explica con tus palabras por qué la fotosíntesis es clave para los ecosistemas.",
  pts: 20
}];
function AnswerScreen({
  onSubmit
}) {
  const [answers, setAnswers] = React.useState({});
  const answered = Object.keys(answers).length;
  const pct = Math.round(answered / QUESTIONS.length * 100);
  return /*#__PURE__*/React.createElement("div", {
    style: ansStyles.page
  }, /*#__PURE__*/React.createElement(StudentBar, {
    right: /*#__PURE__*/React.createElement(Badge, {
      tone: "info",
      dot: true
    }, "En curso \xB7 cierra 09:30")
  }), /*#__PURE__*/React.createElement("div", {
    style: ansStyles.scroll
  }, /*#__PURE__*/React.createElement("div", {
    style: ansStyles.wrap
  }, /*#__PURE__*/React.createElement("div", {
    style: ansStyles.hero
  }, /*#__PURE__*/React.createElement(Badge, {
    tone: "brand"
  }, "Mixta"), /*#__PURE__*/React.createElement("h1", {
    style: ansStyles.title
  }, "Prueba Unidad 3 \u2014 Fotos\xEDntesis"), /*#__PURE__*/React.createElement("p", {
    style: ansStyles.meta
  }, "2\xB0A Biolog\xEDa \xB7 Prof. Paula M\xE9ndez \xB7 4 preguntas \xB7 50 puntos"), /*#__PURE__*/React.createElement("div", {
    style: {
      marginTop: 16
    }
  }, /*#__PURE__*/React.createElement(ProgressBar, {
    value: answered,
    max: QUESTIONS.length,
    label: "Progreso",
    showValue: true,
    striped: answered < QUESTIONS.length,
    tone: pct === 100 ? "success" : "brand"
  }))), QUESTIONS.map((q, i) => /*#__PURE__*/React.createElement(Card, {
    key: i,
    style: {
      marginBottom: 16
    }
  }, /*#__PURE__*/React.createElement(Card.Body, null, /*#__PURE__*/React.createElement("div", {
    style: ansStyles.qhead
  }, /*#__PURE__*/React.createElement("span", {
    style: ansStyles.qnum
  }, i + 1), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: ansStyles.qtext
  }, q.text), /*#__PURE__*/React.createElement("div", {
    style: ansStyles.qpts
  }, q.pts, " pts \xB7 ", {
    single: "Alternativa única",
    multi: "Selección múltiple",
    tf: "Verdadero / Falso",
    open: "Respuesta abierta"
  }[q.kind]))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 10,
      marginTop: 14,
      paddingLeft: 42
    }
  }, q.kind === "open" ? /*#__PURE__*/React.createElement(Textarea, {
    placeholder: "Escribe tu respuesta\u2026",
    onChange: e => setAnswers(a => ({
      ...a,
      [i]: e.target.value || undefined
    }))
  }) : q.kind === "multi" ? q.opts.map((o, j) => /*#__PURE__*/React.createElement(Checkbox, {
    key: j,
    label: o,
    onChange: () => setAnswers(a => ({
      ...a,
      [i]: true
    }))
  })) : q.opts.map((o, j) => /*#__PURE__*/React.createElement(Radio, {
    key: j,
    name: "q" + i,
    label: o,
    onChange: () => setAnswers(a => ({
      ...a,
      [i]: true
    }))
  })))))), /*#__PURE__*/React.createElement("div", {
    style: ansStyles.footer
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: "var(--text-sm)",
      color: "var(--text-muted)",
      display: "flex",
      alignItems: "center",
      gap: 7
    }
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "save",
    size: 15,
    color: "var(--text-subtle)"
  }), " Guardado autom\xE1tico"), /*#__PURE__*/React.createElement(Button, {
    size: "lg",
    iconRight: /*#__PURE__*/React.createElement(Icon, {
      name: "send",
      size: 18
    }),
    onClick: onSubmit
  }, "Enviar respuestas")))));
}
const ansStyles = {
  page: {
    display: "flex",
    flexDirection: "column",
    height: "100%",
    background: "var(--surface-page)",
    fontFamily: "var(--font-sans)"
  },
  scroll: {
    flex: 1,
    overflow: "auto",
    padding: "28px 20px 48px"
  },
  wrap: {
    maxWidth: 720,
    margin: "0 auto"
  },
  hero: {
    marginBottom: 22
  },
  title: {
    fontFamily: "var(--font-display)",
    fontWeight: 700,
    fontSize: "var(--text-3xl)",
    color: "var(--text-strong)",
    margin: "12px 0 6px",
    letterSpacing: "-0.02em"
  },
  meta: {
    margin: 0,
    color: "var(--text-muted)",
    fontSize: "var(--text-md)"
  },
  qhead: {
    display: "flex",
    gap: 12,
    alignItems: "flex-start"
  },
  qnum: {
    flex: "none",
    width: 30,
    height: 30,
    borderRadius: "var(--radius-sm)",
    background: "var(--surface-brand-soft)",
    color: "var(--brand-hover)",
    fontFamily: "var(--font-display)",
    fontWeight: 700,
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontSize: "var(--text-md)"
  },
  qtext: {
    fontWeight: 600,
    fontSize: "var(--text-lg)",
    color: "var(--text-strong)",
    lineHeight: 1.35
  },
  qpts: {
    fontSize: "var(--text-xs)",
    color: "var(--text-subtle)",
    marginTop: 4,
    fontFamily: "var(--font-mono)"
  },
  footer: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    marginTop: 24
  }
};
window.AnswerScreen = AnswerScreen;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/student/AnswerScreen.jsx", error: String((e && e.message) || e) }); }

// ui_kits/student/ResultsScreen.jsx
try { (() => {
// Student results via magic link — shows every assessment across teachers,
// with grade, rubric levels, AI feedback, and lowest points to improve.
const {
  Card,
  Badge,
  Button,
  RubricLevel,
  ProgressBar,
  Avatar
} = window.GradeOpsAIDesignSystem_fcd12b;
const OTHER = [{
  name: "Ensayo: Revolución Industrial",
  course: "Historia",
  score: "5,8",
  st: "meets"
}, {
  name: "Quiz: Funciones cuadráticas",
  course: "Matemática",
  score: "4,1",
  st: "developing"
}, {
  name: "Control de lectura — Quijote",
  course: "Lenguaje",
  score: "6,4",
  st: "exceeds"
}];
const RUBRIC = [{
  name: "Comprensión del concepto",
  level: "meets"
}, {
  name: "Uso de evidencia y ejemplos",
  level: "exceeds"
}, {
  name: "Estructura y argumentación",
  level: "developing"
}, {
  name: "Uso de lenguaje técnico",
  level: "meets"
}];
function ResultsScreen({
  onBack
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: resStyles.page
  }, /*#__PURE__*/React.createElement(StudentBar, {
    right: /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement(Avatar, {
      name: "Diego Soto",
      size: "sm"
    }), /*#__PURE__*/React.createElement("span", {
      style: {
        fontSize: "var(--text-sm)",
        fontWeight: 600,
        color: "var(--text-strong)"
      }
    }, "Diego Soto"))
  }), /*#__PURE__*/React.createElement("div", {
    style: resStyles.scroll
  }, /*#__PURE__*/React.createElement("div", {
    style: resStyles.wrap
  }, /*#__PURE__*/React.createElement("div", {
    style: resStyles.scoreCard
  }, /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement(Badge, {
    tone: "brand",
    style: {
      marginBottom: 10
    }
  }, "Fotos\xEDntesis \xB7 Biolog\xEDa"), /*#__PURE__*/React.createElement("h1", {
    style: resStyles.title
  }, "Tus resultados"), /*#__PURE__*/React.createElement("p", {
    style: resStyles.sub
  }, "Prof. Paula M\xE9ndez \xB7 publicado hoy")), /*#__PURE__*/React.createElement("div", {
    style: resStyles.scoreBig
  }, /*#__PURE__*/React.createElement("div", {
    style: resStyles.scoreNum
  }, "5,1"), /*#__PURE__*/React.createElement(RubricLevel, {
    level: "meets",
    solid: true
  }))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: "1.5fr 1fr",
      gap: 16,
      marginTop: 18,
      alignItems: "start"
    }
  }, /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, null, "Retroalimentaci\xF3n"), /*#__PURE__*/React.createElement(Icon, {
    name: "sparkles",
    size: 18,
    color: "var(--brand)"
  })), /*#__PURE__*/React.createElement(Card.Body, null, /*#__PURE__*/React.createElement("p", {
    style: resStyles.feedback
  }, "Buen dominio conceptual, Diego. Tu mayor fortaleza fue el uso de ejemplos (plantas C3 y C4), que aplicaste con precisi\xF3n. Para subir tu nota, enf\xF3cate en cerrar tus ensayos retomando la tesis y en cuidar las transiciones entre p\xE1rrafos."), /*#__PURE__*/React.createElement("div", {
    style: resStyles.lowbox
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 8,
      marginBottom: 8
    }
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "target",
    size: 17,
    color: "var(--gold-600)"
  }), /*#__PURE__*/React.createElement("span", {
    style: {
      fontWeight: 700,
      color: "var(--gold-800)"
    }
  }, "Puntos a mejorar")), /*#__PURE__*/React.createElement("ul", {
    style: resStyles.lowlist
  }, /*#__PURE__*/React.createElement("li", null, "Estructura y argumentaci\xF3n \u2014 cierra retomando la tesis."), /*#__PURE__*/React.createElement("li", null, "Rol del NADPH en la fase oscura \u2014 repasa el material de la clase 4."))))), /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, null, "R\xFAbrica")), /*#__PURE__*/React.createElement("div", {
    style: {
      padding: "6px 16px 14px"
    }
  }, RUBRIC.map((r, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    style: resStyles.rubRow
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: "var(--text-sm)",
      color: "var(--text-body)",
      flex: 1
    }
  }, r.name), /*#__PURE__*/React.createElement(RubricLevel, {
    level: r.level
  })))))), /*#__PURE__*/React.createElement(Card, {
    style: {
      marginTop: 18
    }
  }, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, null, "Todas tus evaluaciones"), /*#__PURE__*/React.createElement(Badge, {
    tone: "neutral"
  }, OTHER.length + 1, " en total")), /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("div", {
    style: resStyles.evalRow
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: resStyles.evalName
  }, "Prueba Unidad 3 \u2014 Fotos\xEDntesis"), /*#__PURE__*/React.createElement("div", {
    style: resStyles.evalCourse
  }, "Biolog\xEDa \xB7 Prof. M\xE9ndez")), /*#__PURE__*/React.createElement("span", {
    style: resStyles.evalScore
  }, "5,1"), /*#__PURE__*/React.createElement(RubricLevel, {
    level: "meets"
  })), OTHER.map((o, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    style: resStyles.evalRow
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: resStyles.evalName
  }, o.name), /*#__PURE__*/React.createElement("div", {
    style: resStyles.evalCourse
  }, o.course)), /*#__PURE__*/React.createElement("span", {
    style: resStyles.evalScore
  }, o.score), /*#__PURE__*/React.createElement(RubricLevel, {
    level: o.st
  }))))), /*#__PURE__*/React.createElement("div", {
    style: {
      textAlign: "center",
      marginTop: 22,
      color: "var(--text-subtle)",
      fontSize: "var(--text-xs)"
    }
  }, "Este enlace es personal y re\xFAne todas tus evaluaciones, sin importar el profesor."))));
}
const resStyles = {
  page: {
    display: "flex",
    flexDirection: "column",
    height: "100%",
    background: "var(--surface-page)",
    fontFamily: "var(--font-sans)"
  },
  scroll: {
    flex: 1,
    overflow: "auto",
    padding: "28px 20px 48px"
  },
  wrap: {
    maxWidth: 760,
    margin: "0 auto"
  },
  scoreCard: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: 20,
    background: "linear-gradient(140deg, var(--sprout-700), var(--sprout-800))",
    borderRadius: "var(--radius-xl)",
    padding: "28px 30px",
    color: "#fff"
  },
  title: {
    fontFamily: "var(--font-display)",
    fontWeight: 700,
    fontSize: "var(--text-3xl)",
    margin: 0,
    letterSpacing: "-0.02em"
  },
  sub: {
    margin: "6px 0 0",
    opacity: 0.85,
    fontSize: "var(--text-md)"
  },
  scoreBig: {
    textAlign: "center",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    gap: 8
  },
  scoreNum: {
    fontFamily: "var(--font-display)",
    fontWeight: 800,
    fontSize: 56,
    lineHeight: 1
  },
  feedback: {
    margin: 0,
    fontSize: "var(--text-md)",
    lineHeight: 1.6,
    color: "var(--text-body)"
  },
  lowbox: {
    marginTop: 16,
    padding: "14px 16px",
    background: "var(--gold-50)",
    border: "1px solid var(--gold-200)",
    borderRadius: "var(--radius-md)"
  },
  lowlist: {
    margin: 0,
    paddingLeft: 18,
    color: "var(--gold-800)",
    fontSize: "var(--text-sm)",
    lineHeight: 1.7
  },
  rubRow: {
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    gap: 10,
    padding: "9px 0",
    borderBottom: "1px solid var(--border-subtle)"
  },
  evalRow: {
    display: "flex",
    alignItems: "center",
    gap: 14,
    padding: "13px 20px",
    borderBottom: "1px solid var(--border-subtle)"
  },
  evalName: {
    fontWeight: 600,
    fontSize: "var(--text-md)",
    color: "var(--text-strong)"
  },
  evalCourse: {
    fontSize: "var(--text-xs)",
    color: "var(--text-subtle)",
    marginTop: 2
  },
  evalScore: {
    fontFamily: "var(--font-display)",
    fontWeight: 700,
    fontSize: "var(--text-lg)",
    color: "var(--text-strong)"
  }
};
window.ResultsScreen = ResultsScreen;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/student/ResultsScreen.jsx", error: String((e && e.message) || e) }); }

// ui_kits/student/StudentApp.jsx
try { (() => {
// Student portal orchestrator: answer an assessment → submit → view results.
const {
  Button,
  ToastViewport,
  ConfirmDialog
} = window.GradeOpsAIDesignSystem_fcd12b;
function StudentApp() {
  const [view, setView] = React.useState("answer"); // answer | results
  const [confirm, setConfirm] = React.useState(false);
  const [submitting, setSubmitting] = React.useState(false);
  const [toasts, setToasts] = React.useState([]);
  const idRef = React.useRef(0);
  const push = t => {
    const id = ++idRef.current;
    setToasts(x => [...x, {
      id,
      ...t
    }]);
    if (!t.loading) setTimeout(() => setToasts(x => x.filter(o => o.id !== id)), 3200);
    return id;
  };
  const submit = () => {
    setSubmitting(true);
    setTimeout(() => {
      setSubmitting(false);
      setConfirm(false);
      setView("results");
      push({
        tone: "success",
        title: "Respuestas enviadas",
        message: "Tu profesora recibió tu entrega."
      });
    }, 1400);
  };
  return /*#__PURE__*/React.createElement(React.Fragment, null, view === "answer" ? /*#__PURE__*/React.createElement(AnswerScreen, {
    onSubmit: () => setConfirm(true)
  }) : /*#__PURE__*/React.createElement(ResultsScreen, {
    onBack: () => setView("answer")
  }), /*#__PURE__*/React.createElement(ConfirmDialog, {
    open: confirm,
    tone: "brand",
    title: "\xBFEnviar tus respuestas?",
    message: "Una vez enviadas no podr\xE1s modificarlas. Revisa que hayas respondido todas las preguntas.",
    confirmLabel: "Enviar",
    cancelLabel: "Seguir respondiendo",
    loading: submitting,
    onCancel: () => setConfirm(false),
    onConfirm: submit
  }), /*#__PURE__*/React.createElement(ToastViewport, {
    toasts: toasts,
    onDismiss: id => setToasts(x => x.filter(t => t.id !== id))
  }));
}
ReactDOM.createRoot(document.getElementById("root")).render(/*#__PURE__*/React.createElement(StudentApp, null));
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/student/StudentApp.jsx", error: String((e && e.message) || e) }); }

// ui_kits/student/StudentBar.jsx
try { (() => {
// Student top bar — minimal, branded, shows who's signed in via magic link.
function StudentBar({
  right
}) {
  return /*#__PURE__*/React.createElement("header", {
    style: barStyles.bar
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 10
    }
  }, /*#__PURE__*/React.createElement("img", {
    src: "../../assets/logo-mark.svg",
    alt: "",
    style: {
      width: 30,
      height: 30
    }
  }), /*#__PURE__*/React.createElement("span", {
    style: barStyles.brand
  }, "GradeOps", /*#__PURE__*/React.createElement("span", {
    style: {
      color: "var(--brand)"
    }
  }, "AI"))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 12
    }
  }, right));
}
const barStyles = {
  bar: {
    height: 60,
    flex: "none",
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    padding: "0 24px",
    borderBottom: "1px solid var(--border-subtle)",
    background: "var(--surface-card)"
  },
  brand: {
    fontFamily: "var(--font-display)",
    fontWeight: 700,
    fontSize: 18,
    color: "var(--text-strong)",
    letterSpacing: "-0.02em"
  }
};
window.StudentBar = StudentBar;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/student/StudentBar.jsx", error: String((e && e.message) || e) }); }

// ui_kits/student/icons.jsx
try { (() => {
// Lucide → React icon helper (student portal copy).
function Icon({
  name,
  size = 20,
  strokeWidth = 2,
  color = "currentColor",
  style,
  className
}) {
  const pascal = String(name).split("-").map(s => s[0].toUpperCase() + s.slice(1)).join("");
  const node = window.lucide && window.lucide.icons && window.lucide.icons[pascal] || [];
  const children = node.map(([tag, attrs], i) => React.createElement(tag, {
    key: i,
    ...attrs
  }));
  return React.createElement("svg", {
    className,
    width: size,
    height: size,
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: color,
    strokeWidth,
    strokeLinecap: "round",
    strokeLinejoin: "round",
    style: {
      flex: "none",
      display: "block",
      ...style
    }
  }, children);
}
window.Icon = Icon;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/student/icons.jsx", error: String((e && e.message) || e) }); }

// ui_kits/teacher/AssessmentsScreen.jsx
try { (() => {
// AssessmentsScreen.jsx — full evaluation list with filters, progress and actions.
const {
  Badge,
  Button,
  Input,
  Select,
  Tabs,
  Card,
  ProgressBar,
  IconButton
} = window.GradeOpsAIDesignSystem_fcd12b;
const EVALS = [{
  id: 1,
  title: "Prueba Unidad 3 — Fotosíntesis",
  subject: "Biología",
  course: "1°B",
  type: "Cerrada",
  status: "corregida",
  done: 32,
  total: 32,
  avg: "5,4",
  due: "5 jun",
  shared: true
}, {
  id: 2,
  title: "Ensayo: Revolución Industrial",
  subject: "Historia",
  course: "2°A",
  type: "Abierta",
  status: "en_revision",
  done: 14,
  total: 28,
  avg: null,
  due: "Hoy 23:59",
  shared: false
}, {
  id: 3,
  title: "Control — Álgebra lineal",
  subject: "Matemática",
  course: "3°A",
  type: "Mixta",
  status: "en_curso",
  done: 8,
  total: 25,
  avg: null,
  due: "20 jun",
  shared: false
}, {
  id: 4,
  title: "Prueba diagnóstica — Lenguaje",
  subject: "Lenguaje",
  course: "1°B",
  type: "Cerrada",
  status: "corregida",
  done: 30,
  total: 30,
  avg: "4,9",
  due: "1 jun",
  shared: false
}, {
  id: 5,
  title: "Informe de laboratorio: Células",
  subject: "Biología",
  course: "2°A",
  type: "Abierta",
  status: "borrador",
  done: 0,
  total: 0,
  avg: null,
  due: "—",
  shared: false
}, {
  id: 6,
  title: "Quiz: Funciones cuadráticas",
  subject: "Matemática",
  course: "2°B",
  type: "Cerrada",
  status: "corrigiendo",
  done: 18,
  total: 31,
  avg: null,
  due: "Hoy 18:00",
  shared: true
}];
const ST = {
  corregida: {
    tone: "success",
    label: "Corregida"
  },
  en_revision: {
    tone: "warning",
    label: "En revisión"
  },
  en_curso: {
    tone: "info",
    label: "En curso"
  },
  corrigiendo: {
    tone: "info",
    label: "Corrigiendo"
  },
  borrador: {
    tone: "neutral",
    label: "Borrador"
  }
};
const TT = {
  Cerrada: "brand",
  Abierta: "gold",
  Mixta: "info"
};
const TYPE_MAP = {
  open: "Abierta",
  closed: "Cerrada",
  mixed: "Mixta"
};
function AssessmentsScreen({
  onGrade
}) {
  const [tab, setTab] = React.useState("all");
  const rows = tab === "all" ? EVALS : EVALS.filter(e => e.type === TYPE_MAP[tab]);
  return /*#__PURE__*/React.createElement("div", {
    style: {
      maxWidth: "var(--content-max)",
      display: "flex",
      flexDirection: "column",
      gap: 18
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 12,
      flexWrap: "wrap"
    }
  }, /*#__PURE__*/React.createElement(Tabs, {
    value: tab,
    onChange: setTab,
    tabs: [{
      id: "all",
      label: "Todas",
      count: EVALS.length
    }, {
      id: "open",
      label: "Abiertas"
    }, {
      id: "closed",
      label: "Cerradas"
    }, {
      id: "mixed",
      label: "Mixtas"
    }]
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1
    }
  }), /*#__PURE__*/React.createElement(Input, {
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "search",
      size: 17
    }),
    placeholder: "Buscar evaluaci\xF3n\u2026",
    size: "sm",
    style: {
      width: 200
    }
  }), /*#__PURE__*/React.createElement(Select, {
    size: "sm",
    style: {
      width: 160
    }
  }, /*#__PURE__*/React.createElement("option", null, "Todos los cursos"), /*#__PURE__*/React.createElement("option", null, "1\xB0B"), /*#__PURE__*/React.createElement("option", null, "2\xB0A"), /*#__PURE__*/React.createElement("option", null, "3\xB0A"), /*#__PURE__*/React.createElement("option", null, "2\xB0B"))), /*#__PURE__*/React.createElement(Card, null, rows.map((e, i) => {
    const st = ST[e.status];
    const pct = e.total > 0 ? Math.round(e.done / e.total * 100) : 0;
    return /*#__PURE__*/React.createElement("div", {
      key: e.id,
      style: {
        display: "grid",
        gridTemplateColumns: "1fr 140px 88px 180px",
        gap: 16,
        alignItems: "center",
        padding: "14px 20px",
        borderBottom: i < rows.length - 1 ? "1px solid var(--border-subtle)" : "none"
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        minWidth: 0
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        alignItems: "center",
        gap: 8,
        marginBottom: 4
      }
    }, /*#__PURE__*/React.createElement("span", {
      style: {
        fontWeight: 600,
        fontSize: "var(--text-md)",
        color: "var(--text-strong)",
        whiteSpace: "nowrap",
        overflow: "hidden",
        textOverflow: "ellipsis"
      }
    }, e.title), e.shared && /*#__PURE__*/React.createElement(Icon, {
      name: "globe",
      size: 13,
      color: "var(--gold-600)"
    })), /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        alignItems: "center",
        gap: 8,
        fontSize: "var(--text-sm)",
        color: "var(--text-muted)"
      }
    }, /*#__PURE__*/React.createElement(Badge, {
      tone: TT[e.type]
    }, e.type), /*#__PURE__*/React.createElement("span", null, e.subject, " \xB7 ", e.course), /*#__PURE__*/React.createElement("span", {
      style: {
        color: e.due.includes("Hoy") ? "var(--warning-600)" : "inherit"
      }
    }, "\xB7 ", e.due))), /*#__PURE__*/React.createElement("div", null, e.total > 0 ? /*#__PURE__*/React.createElement(ProgressBar, {
      value: pct,
      label: `${e.done}/${e.total}`,
      showValue: true
    }) : /*#__PURE__*/React.createElement("span", {
      style: {
        fontSize: "var(--text-xs)",
        color: "var(--text-subtle)"
      }
    }, "Sin entregas")), /*#__PURE__*/React.createElement("div", {
      style: {
        textAlign: "center"
      }
    }, e.avg ? /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement("div", {
      style: {
        fontFamily: "var(--font-display)",
        fontWeight: 700,
        fontSize: "var(--text-xl)",
        color: "var(--text-strong)"
      }
    }, e.avg), /*#__PURE__*/React.createElement("div", {
      style: {
        fontSize: "var(--text-xs)",
        color: "var(--text-subtle)"
      }
    }, "promedio")) : /*#__PURE__*/React.createElement("span", {
      style: {
        fontSize: "var(--text-xs)",
        color: "var(--text-subtle)"
      }
    }, "\u2014")), /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        alignItems: "center",
        gap: 8,
        justifyContent: "flex-end"
      }
    }, /*#__PURE__*/React.createElement(Badge, {
      tone: st.tone,
      dot: true
    }, st.label), e.status !== "borrador" ? /*#__PURE__*/React.createElement(IconButton, {
      label: "Ver / Corregir",
      size: "sm",
      onClick: onGrade
    }, /*#__PURE__*/React.createElement(Icon, {
      name: "arrow-right",
      size: 15
    })) : /*#__PURE__*/React.createElement(IconButton, {
      label: "Editar borrador",
      size: "sm"
    }, /*#__PURE__*/React.createElement(Icon, {
      name: "pencil",
      size: 15
    }))));
  })));
}
window.AssessmentsScreen = AssessmentsScreen;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/teacher/AssessmentsScreen.jsx", error: String((e && e.message) || e) }); }

// ui_kits/teacher/BankScreen.jsx
try { (() => {
// BankScreen.jsx — standalone question bank browser with detail panel.
const {
  Badge,
  Button,
  Input,
  Select,
  Tabs,
  Card,
  IconButton
} = window.GradeOpsAIDesignSystem_fcd12b;
const QUESTIONS = [{
  id: "q1",
  text: "¿Cuál es el principal producto energético de la fase lumínica?",
  type: "Única",
  subject: "Biología",
  unit: "Unidad 3",
  topic: "Fotosíntesis",
  scope: "global",
  uses: 124
}, {
  id: "q2",
  text: "La clorofila absorbe principalmente luz verde.",
  type: "V/F",
  subject: "Biología",
  unit: "Unidad 3",
  topic: "Pigmentos",
  scope: "global",
  uses: 89
}, {
  id: "q3",
  text: "Selecciona las moléculas producidas en el ciclo de Calvin.",
  type: "Múltiple",
  subject: "Biología",
  unit: "Unidad 3",
  topic: "Fotosíntesis",
  scope: "personal",
  uses: 18
}, {
  id: "q4",
  text: "¿En qué organelo ocurre la fotosíntesis?",
  type: "Única",
  subject: "Biología",
  unit: "Unidad 2",
  topic: "Célula",
  scope: "global",
  uses: 203
}, {
  id: "q5",
  text: "Indica los factores que afectan la tasa de fotosíntesis.",
  type: "Múltiple",
  subject: "Biología",
  unit: "Unidad 3",
  topic: "Fotosíntesis",
  scope: "personal",
  uses: 7
}, {
  id: "q6",
  text: "Analiza la evolución del protagonista a lo largo del relato.",
  type: "Abierta",
  subject: "Lenguaje",
  unit: "Unidad 1",
  topic: "Narrativa",
  scope: "personal",
  uses: 3
}, {
  id: "q7",
  text: "¿Cuál es la derivada de f(x) = x² + 3x?",
  type: "Única",
  subject: "Matemática",
  unit: "Unidad 2",
  topic: "Derivadas",
  scope: "global",
  uses: 56
}, {
  id: "q8",
  text: "Determina si los siguientes triángulos son semejantes.",
  type: "Múltiple",
  subject: "Matemática",
  unit: "Unidad 1",
  topic: "Geometría",
  scope: "global",
  uses: 41
}];
const TT = {
  "Única": "brand",
  "Múltiple": "info",
  "V/F": "neutral",
  "Abierta": "gold"
};
function BankScreen() {
  const [bankTab, setBankTab] = React.useState("global");
  const [subject, setSubject] = React.useState("all");
  const [sel, setSel] = React.useState(null);
  const questions = QUESTIONS.filter(q => q.scope === bankTab && (subject === "all" || q.subject === subject));
  const detail = sel ? QUESTIONS.find(q => q.id === sel) : null;
  return /*#__PURE__*/React.createElement("div", {
    style: {
      maxWidth: "var(--content-max)",
      display: "flex",
      flexDirection: "column",
      gap: 18
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 12,
      flexWrap: "wrap"
    }
  }, /*#__PURE__*/React.createElement(Tabs, {
    value: bankTab,
    onChange: v => {
      setBankTab(v);
      setSel(null);
    },
    variant: "pills",
    tabs: [{
      id: "global",
      label: "Banco global"
    }, {
      id: "personal",
      label: "Mis preguntas"
    }]
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1
    }
  }), /*#__PURE__*/React.createElement(Input, {
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "search",
      size: 17
    }),
    placeholder: "Buscar pregunta\u2026",
    size: "sm",
    style: {
      width: 220
    }
  }), /*#__PURE__*/React.createElement(Select, {
    size: "sm",
    style: {
      width: 180
    },
    value: subject,
    onChange: e => setSubject(e.target.value)
  }, /*#__PURE__*/React.createElement("option", {
    value: "all"
  }, "Todas las materias"), /*#__PURE__*/React.createElement("option", {
    value: "Biolog\xEDa"
  }, "Biolog\xEDa"), /*#__PURE__*/React.createElement("option", {
    value: "Lenguaje"
  }, "Lenguaje"), /*#__PURE__*/React.createElement("option", {
    value: "Matem\xE1tica"
  }, "Matem\xE1tica")), /*#__PURE__*/React.createElement(Button, {
    variant: "secondary",
    size: "sm",
    iconLeft: /*#__PURE__*/React.createElement(Icon, {
      name: "plus",
      size: 15
    })
  }, "Nueva pregunta")), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: detail ? "1fr 300px" : "1fr",
      gap: 16,
      alignItems: "start"
    }
  }, /*#__PURE__*/React.createElement(Card, null, questions.length === 0 && /*#__PURE__*/React.createElement("div", {
    style: {
      padding: 40,
      textAlign: "center",
      color: "var(--text-subtle)",
      fontSize: "var(--text-sm)"
    }
  }, "No hay preguntas que coincidan con los filtros."), questions.map((q, i) => /*#__PURE__*/React.createElement("button", {
    key: q.id,
    onClick: () => setSel(sel === q.id ? null : q.id),
    style: {
      width: "100%",
      display: "flex",
      alignItems: "flex-start",
      gap: 14,
      padding: "14px 18px",
      border: "none",
      borderBottom: i < questions.length - 1 ? "1px solid var(--border-subtle)" : "none",
      background: sel === q.id ? "var(--surface-brand-soft)" : "transparent",
      cursor: "pointer",
      fontFamily: "inherit",
      textAlign: "left",
      transition: "background 120ms"
    },
    onMouseEnter: e => {
      if (sel !== q.id) e.currentTarget.style.background = "var(--surface-sunken)";
    },
    onMouseLeave: e => {
      if (sel !== q.id) e.currentTarget.style.background = "transparent";
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement("p", {
    style: {
      margin: "0 0 7px",
      fontWeight: 500,
      fontSize: "var(--text-sm)",
      color: "var(--text-strong)",
      lineHeight: 1.45
    }
  }, q.text), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 7,
      flexWrap: "wrap"
    }
  }, /*#__PURE__*/React.createElement(Badge, {
    tone: TT[q.type]
  }, q.type), /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: "var(--text-xs)",
      color: "var(--text-subtle)"
    }
  }, q.subject, " \xB7 ", q.unit, " \xB7 ", q.topic), /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: "var(--text-xs)",
      color: "var(--text-subtle)"
    }
  }, "\xB7 ", q.uses, " usos"))), /*#__PURE__*/React.createElement(Icon, {
    name: "chevron-right",
    size: 16,
    color: sel === q.id ? "var(--brand)" : "var(--text-subtle)"
  }))), bankTab === "personal" && /*#__PURE__*/React.createElement("div", {
    style: {
      padding: "12px 18px",
      borderTop: "1px solid var(--border-subtle)"
    }
  }, /*#__PURE__*/React.createElement(Button, {
    variant: "secondary",
    size: "sm",
    block: true,
    iconLeft: /*#__PURE__*/React.createElement(Icon, {
      name: "sparkles",
      size: 15
    })
  }, "Generar preguntas con IA"))), detail && /*#__PURE__*/React.createElement(Card, {
    accent: true
  }, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, null, "Detalle"), /*#__PURE__*/React.createElement(IconButton, {
    label: "Cerrar",
    size: "sm",
    onClick: () => setSel(null)
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "x",
    size: 15
  }))), /*#__PURE__*/React.createElement(Card.Body, null, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      gap: 8,
      marginBottom: 12
    }
  }, /*#__PURE__*/React.createElement(Badge, {
    tone: TT[detail.type]
  }, detail.type), /*#__PURE__*/React.createElement(Badge, {
    tone: detail.scope === "global" ? "gold" : "neutral"
  }, detail.scope === "global" ? "Global" : "Personal")), /*#__PURE__*/React.createElement("p", {
    style: {
      fontWeight: 600,
      fontSize: "var(--text-md)",
      color: "var(--text-strong)",
      lineHeight: 1.5,
      margin: "0 0 14px"
    }
  }, detail.text), /*#__PURE__*/React.createElement("div", {
    style: {
      fontSize: "var(--text-xs)",
      color: "var(--text-muted)",
      lineHeight: 2,
      borderTop: "1px solid var(--border-subtle)",
      paddingTop: 12
    }
  }, /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("b", null, "Materia:"), " ", detail.subject), /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("b", null, "Unidad:"), " ", detail.unit), /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("b", null, "Tem\xE1tica:"), " ", detail.topic), /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("b", null, "Usos en evaluaciones:"), " ", detail.uses))), /*#__PURE__*/React.createElement(Card.Footer, null, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 8
    }
  }, /*#__PURE__*/React.createElement(Button, {
    size: "sm",
    block: true,
    iconLeft: /*#__PURE__*/React.createElement(Icon, {
      name: "file-pen-line",
      size: 15
    })
  }, "A\xF1adir a evaluaci\xF3n"), /*#__PURE__*/React.createElement(Button, {
    variant: "secondary",
    size: "sm",
    block: true,
    iconLeft: /*#__PURE__*/React.createElement(Icon, {
      name: "pencil",
      size: 15
    })
  }, "Editar pregunta"))))));
}
window.BankScreen = BankScreen;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/teacher/BankScreen.jsx", error: String((e && e.message) || e) }); }

// ui_kits/teacher/BuilderScreen.jsx
try { (() => {
// Assessment builder: settings + question bank picker (closed-question example).
const {
  Card,
  Badge,
  Button,
  Input,
  Select,
  Field,
  Switch,
  Checkbox,
  Tabs,
  IconButton,
  Radio
} = window.GradeOpsAIDesignSystem_fcd12b;
const BANK = [{
  q: "¿Cuál es el principal producto de la fase lumínica?",
  type: "Única",
  scope: "global",
  topic: "Fotosíntesis",
  used: 124,
  added: true
}, {
  q: "Selecciona las moléculas producidas en el ciclo de Calvin.",
  type: "Múltiple",
  scope: "personal",
  topic: "Fotosíntesis",
  used: 18,
  added: true
}, {
  q: "La clorofila absorbe principalmente luz verde. (V/F)",
  type: "V/F",
  scope: "global",
  topic: "Pigmentos",
  used: 89,
  added: false
}, {
  q: "¿En qué organelo ocurre la fotosíntesis?",
  type: "Única",
  scope: "global",
  topic: "Célula",
  used: 203,
  added: false
}, {
  q: "Indica las etapas correctas de la respiración celular.",
  type: "Múltiple",
  scope: "personal",
  topic: "Metabolismo",
  used: 7,
  added: false
}];
function BuilderScreen({
  onCancel,
  onSave
}) {
  const [bankTab, setBankTab] = React.useState("global");
  const [added, setAdded] = React.useState(() => BANK.filter(b => b.added).length);
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: "1fr 360px",
      gap: 18,
      maxWidth: "var(--content-max)",
      alignItems: "start"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 16
    }
  }, /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, null, "Detalles de la evaluaci\xF3n"), /*#__PURE__*/React.createElement(Badge, {
    tone: "brand"
  }, "Cerrada")), /*#__PURE__*/React.createElement(Card.Body, null, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: "1fr 1fr",
      gap: 16
    }
  }, /*#__PURE__*/React.createElement(Field, {
    label: "T\xEDtulo",
    htmlFor: "t",
    required: true
  }, /*#__PURE__*/React.createElement(Input, {
    id: "t",
    defaultValue: "Prueba Unidad 3 \u2014 Fotos\xEDntesis"
  })), /*#__PURE__*/React.createElement(Field, {
    label: "Curso / Asignatura",
    htmlFor: "c"
  }, /*#__PURE__*/React.createElement(Select, {
    id: "c",
    defaultValue: "bio"
  }, /*#__PURE__*/React.createElement("option", {
    value: "bio"
  }, "1\xB0B \xB7 Biolog\xEDa"), /*#__PURE__*/React.createElement("option", {
    value: "his"
  }, "2\xB0A \xB7 Historia"))), /*#__PURE__*/React.createElement(Field, {
    label: "Inicio",
    htmlFor: "d1"
  }, /*#__PURE__*/React.createElement(Input, {
    id: "d1",
    type: "datetime-local",
    defaultValue: "2026-06-20T08:30"
  })), /*#__PURE__*/React.createElement(Field, {
    label: "Cierre",
    htmlFor: "d2"
  }, /*#__PURE__*/React.createElement(Input, {
    id: "d2",
    type: "datetime-local",
    defaultValue: "2026-06-20T09:30"
  }))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 12,
      marginTop: 16,
      paddingTop: 16,
      borderTop: "1px solid var(--border-subtle)"
    }
  }, /*#__PURE__*/React.createElement(Switch, {
    label: "Permitir respuestas fuera de tiempo",
    defaultChecked: true
  }), /*#__PURE__*/React.createElement(Switch, {
    label: "Penalizar entregas atrasadas (\u221210%)"
  }), /*#__PURE__*/React.createElement(Switch, {
    label: "Compartir en el banco global al publicar"
  })))), /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, null, "Preguntas seleccionadas"), /*#__PURE__*/React.createElement(Badge, {
    tone: "success",
    dot: true
  }, added, " preguntas \xB7 60 pts")), /*#__PURE__*/React.createElement(Card.Body, null, /*#__PURE__*/React.createElement("div", {
    style: builderStyles.empty
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "list-checks",
    size: 22,
    color: "var(--brand)"
  }), /*#__PURE__*/React.createElement("span", null, "Arrastra desde el banco o usa ", /*#__PURE__*/React.createElement("b", null, "+"), " para a\xF1adir. La IA corrige con la clave de respuestas."))))), /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, null, "Banco de preguntas")), /*#__PURE__*/React.createElement("div", {
    style: {
      padding: "12px 16px 0"
    }
  }, /*#__PURE__*/React.createElement(Input, {
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "search",
      size: 17
    }),
    placeholder: "Buscar por tema o enunciado\u2026",
    size: "sm"
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      margin: "12px 0 6px"
    }
  }, /*#__PURE__*/React.createElement(Tabs, {
    value: bankTab,
    onChange: setBankTab,
    variant: "pills",
    tabs: [{
      id: "global",
      label: "Global"
    }, {
      id: "personal",
      label: "Personal"
    }]
  }))), /*#__PURE__*/React.createElement("div", {
    style: {
      maxHeight: 340,
      overflow: "auto",
      padding: "4px 10px 12px"
    }
  }, BANK.filter(b => b.scope === bankTab).map((b, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    style: builderStyles.bankItem
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: builderStyles.bankQ
  }, b.q), /*#__PURE__*/React.createElement("div", {
    style: builderStyles.bankMeta
  }, /*#__PURE__*/React.createElement(Badge, {
    tone: "neutral"
  }, b.type), /*#__PURE__*/React.createElement("span", null, b.topic), /*#__PURE__*/React.createElement("span", null, "\xB7"), /*#__PURE__*/React.createElement("span", null, b.used, " usos"))), /*#__PURE__*/React.createElement(IconButton, {
    label: b.added ? "Quitar" : "Añadir",
    variant: b.added ? "ghost" : "outline",
    size: "sm"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: b.added ? "check" : "plus",
    size: 16,
    color: b.added ? "var(--success-600)" : undefined
  }))))), /*#__PURE__*/React.createElement(Card.Footer, null, /*#__PURE__*/React.createElement(Button, {
    variant: "secondary",
    size: "sm",
    block: true,
    iconLeft: /*#__PURE__*/React.createElement(Icon, {
      name: "sparkles",
      size: 15
    })
  }, "Generar preguntas con IA"))), /*#__PURE__*/React.createElement("div", {
    style: {
      gridColumn: "1 / -1",
      display: "flex",
      justifyContent: "flex-end",
      gap: 10
    }
  }, /*#__PURE__*/React.createElement(Button, {
    variant: "ghost",
    onClick: onCancel
  }, "Cancelar"), /*#__PURE__*/React.createElement(Button, {
    variant: "secondary"
  }, "Vista previa"), /*#__PURE__*/React.createElement(Button, {
    variant: "primary",
    iconLeft: /*#__PURE__*/React.createElement(Icon, {
      name: "check",
      size: 16
    }),
    onClick: onSave
  }, "Guardar evaluaci\xF3n")));
}
const builderStyles = {
  empty: {
    display: "flex",
    alignItems: "center",
    gap: 12,
    padding: "16px",
    background: "var(--surface-brand-soft)",
    border: "1px dashed var(--sprout-300)",
    borderRadius: "var(--radius-md)",
    fontSize: "var(--text-sm)",
    color: "var(--text-muted)",
    lineHeight: 1.5
  },
  bankItem: {
    display: "flex",
    alignItems: "center",
    gap: 10,
    padding: "11px 8px",
    borderBottom: "1px solid var(--border-subtle)"
  },
  bankQ: {
    fontWeight: 500,
    fontSize: "var(--text-sm)",
    color: "var(--text-strong)",
    lineHeight: 1.4,
    marginBottom: 5
  },
  bankMeta: {
    display: "flex",
    alignItems: "center",
    gap: 7,
    fontSize: "var(--text-xs)",
    color: "var(--text-subtle)"
  }
};
window.BuilderScreen = BuilderScreen;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/teacher/BuilderScreen.jsx", error: String((e && e.message) || e) }); }

// ui_kits/teacher/DashboardScreen.jsx
try { (() => {
// Teacher control panel: KPIs, recent assessments, group performance, at-risk.
const {
  Card,
  StatCard,
  Badge,
  Button,
  Tabs,
  ProgressBar,
  RubricLevel,
  Avatar
} = window.GradeOpsAIDesignSystem_fcd12b;
const RECENT = [{
  name: "Prueba Unidad 3 — Fotosíntesis",
  type: "Cerrada",
  tone: "brand",
  course: "1°B Biología",
  status: "Corregida",
  st: "success",
  count: 32,
  avg: "5,4"
}, {
  name: "Ensayo: Revolución Industrial",
  type: "Abierta",
  tone: "gold",
  course: "2°A Historia",
  status: "En revisión",
  st: "warning",
  count: 28,
  avg: "—"
}, {
  name: "Control de lectura — Quijote",
  type: "Mixta",
  tone: "info",
  course: "3°A Lenguaje",
  status: "En curso",
  st: "info",
  count: 30,
  avg: "—"
}, {
  name: "Quiz: Funciones cuadráticas",
  type: "Cerrada",
  tone: "brand",
  course: "2°B Matemática",
  status: "Corregida",
  st: "success",
  count: 31,
  avg: "4,9"
}];
const RISK = [{
  name: "Tomás Vidal",
  course: "1°B",
  avg: "3,2",
  trend: "down"
}, {
  name: "Javiera Lillo",
  course: "2°A",
  avg: "3,8",
  trend: "down"
}, {
  name: "Benjamín Ortiz",
  course: "3°A",
  avg: "3,9",
  trend: "flat"
}];
function DashboardScreen({
  onOpenAssessment,
  onGoBank
}) {
  const [tab, setTab] = React.useState("all");
  const I = (n, c) => /*#__PURE__*/React.createElement(Icon, {
    name: n,
    size: 19,
    color: c
  });
  const rows = tab === "all" ? RECENT : RECENT.filter(r => r.type.toLowerCase() === {
    open: "abierta",
    closed: "cerrada",
    mixed: "mixta"
  }[tab]);
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 22,
      maxWidth: "var(--content-max)"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: "repeat(4, 1fr)",
      gap: 16
    }
  }, /*#__PURE__*/React.createElement(StatCard, {
    label: "Promedio general",
    value: "5,2",
    icon: I("trending-up"),
    delta: "+0,3"
  }), /*#__PURE__*/React.createElement(StatCard, {
    label: "Por corregir",
    value: "14",
    iconTone: "gold",
    icon: I("clipboard-check")
  }), /*#__PURE__*/React.createElement(StatCard, {
    label: "Entregas a tiempo",
    value: "92",
    unit: "%",
    iconTone: "info",
    icon: I("calendar-clock")
  }), /*#__PURE__*/React.createElement(StatCard, {
    label: "En riesgo",
    value: "3",
    unit: "alumnos",
    iconTone: "danger",
    icon: I("alert-triangle"),
    delta: "-1",
    deltaDir: "down"
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: "1.7fr 1fr",
      gap: 18,
      alignItems: "start"
    }
  }, /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, null, "Evaluaciones recientes"), /*#__PURE__*/React.createElement(Tabs, {
    value: tab,
    onChange: setTab,
    variant: "pills",
    tabs: [{
      id: "all",
      label: "Todas"
    }, {
      id: "open",
      label: "Abiertas"
    }, {
      id: "closed",
      label: "Cerradas"
    }, {
      id: "mixed",
      label: "Mixtas"
    }]
  })), /*#__PURE__*/React.createElement("div", null, rows.map((r, i) => /*#__PURE__*/React.createElement("button", {
    key: i,
    onClick: onOpenAssessment,
    style: rowStyles.row,
    onMouseEnter: e => e.currentTarget.style.background = "var(--surface-sunken)",
    onMouseLeave: e => e.currentTarget.style.background = "transparent"
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      minWidth: 0,
      flex: 1,
      textAlign: "left"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: rowStyles.name
  }, r.name), /*#__PURE__*/React.createElement("div", {
    style: rowStyles.meta
  }, /*#__PURE__*/React.createElement(Badge, {
    tone: r.tone
  }, r.type), /*#__PURE__*/React.createElement("span", null, r.course), /*#__PURE__*/React.createElement("span", null, "\xB7"), /*#__PURE__*/React.createElement("span", null, r.count, " estudiantes"))), /*#__PURE__*/React.createElement("div", {
    style: {
      textAlign: "right",
      display: "flex",
      alignItems: "center",
      gap: 16
    }
  }, /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("div", {
    style: rowStyles.avg
  }, r.avg), /*#__PURE__*/React.createElement("div", {
    style: rowStyles.avgLbl
  }, "promedio")), /*#__PURE__*/React.createElement(Badge, {
    tone: r.st,
    dot: true
  }, r.status), /*#__PURE__*/React.createElement(Icon, {
    name: "chevron-right",
    size: 18,
    color: "var(--text-subtle)"
  }))))), /*#__PURE__*/React.createElement(Card.Footer, null, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      justifyContent: "space-between",
      alignItems: "center"
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: "var(--text-sm)",
      color: "var(--text-muted)"
    }
  }, "Mostrando ", rows.length, " de 24"), /*#__PURE__*/React.createElement(Button, {
    variant: "ghost",
    size: "sm",
    iconRight: /*#__PURE__*/React.createElement(Icon, {
      name: "arrow-right",
      size: 16
    })
  }, "Ver todas")))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 18
    }
  }, /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, null, "Cobertura por curso")), /*#__PURE__*/React.createElement(Card.Body, null, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 14
    }
  }, /*#__PURE__*/React.createElement(ProgressBar, {
    value: 84,
    tone: "success",
    label: "1\xB0B Biolog\xEDa",
    showValue: true
  }), /*#__PURE__*/React.createElement(ProgressBar, {
    value: 61,
    label: "2\xB0A Historia",
    showValue: true
  }), /*#__PURE__*/React.createElement(ProgressBar, {
    value: 47,
    tone: "gold",
    label: "3\xB0A Lenguaje",
    showValue: true
  }), /*#__PURE__*/React.createElement(ProgressBar, {
    value: 28,
    tone: "danger",
    label: "2\xB0B Matem\xE1tica",
    showValue: true
  })))), /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, null, "Estudiantes en riesgo"), /*#__PURE__*/React.createElement(Badge, {
    tone: "danger"
  }, RISK.length)), /*#__PURE__*/React.createElement("div", {
    style: {
      padding: "6px 6px 10px"
    }
  }, RISK.map((s, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    style: rowStyles.riskRow
  }, /*#__PURE__*/React.createElement(Avatar, {
    name: s.name,
    size: "sm"
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: rowStyles.name
  }, s.name), /*#__PURE__*/React.createElement("div", {
    style: rowStyles.avgLbl
  }, s.course)), /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-display)",
      fontWeight: 700,
      color: "var(--danger-600)"
    }
  }, s.avg), /*#__PURE__*/React.createElement(Icon, {
    name: s.trend === "down" ? "trending-down" : "minus",
    size: 16,
    color: "var(--danger-500)"
  }))))))));
}
const rowStyles = {
  row: {
    width: "100%",
    display: "flex",
    alignItems: "center",
    gap: 16,
    padding: "14px 20px",
    border: "none",
    borderBottom: "1px solid var(--border-subtle)",
    background: "transparent",
    cursor: "pointer",
    fontFamily: "inherit",
    transition: "background 120ms"
  },
  name: {
    fontWeight: 600,
    fontSize: "var(--text-md)",
    color: "var(--text-strong)",
    whiteSpace: "nowrap",
    overflow: "hidden",
    textOverflow: "ellipsis",
    marginBottom: 4
  },
  meta: {
    display: "flex",
    alignItems: "center",
    gap: 8,
    fontSize: "var(--text-sm)",
    color: "var(--text-muted)"
  },
  avg: {
    fontFamily: "var(--font-display)",
    fontWeight: 700,
    fontSize: "var(--text-lg)",
    color: "var(--text-strong)",
    textAlign: "right"
  },
  avgLbl: {
    fontSize: "var(--text-xs)",
    color: "var(--text-subtle)",
    textAlign: "right"
  },
  riskRow: {
    display: "flex",
    alignItems: "center",
    gap: 11,
    padding: "9px 14px"
  }
};
window.DashboardScreen = DashboardScreen;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/teacher/DashboardScreen.jsx", error: String((e && e.message) || e) }); }

// ui_kits/teacher/GradingScreen.jsx
try { (() => {
// AI grading review for an open assessment: rubric indices, performance levels,
// AI-generated feedback with reasoning, teacher approves/edits before publishing.
const {
  Card,
  Badge,
  Button,
  Avatar,
  RubricLevel,
  ProgressBar,
  Textarea,
  IconButton,
  LoadingOverlay
} = window.GradeOpsAIDesignSystem_fcd12b;
const STUDENTS = [{
  name: "Camila Rojas",
  score: "6,2",
  level: "exceeds",
  done: true
}, {
  name: "Diego Soto",
  score: "5,1",
  level: "meets",
  done: true,
  active: true
}, {
  name: "Fernanda Paz",
  score: "4,3",
  level: "developing",
  done: true
}, {
  name: "Tomás Vidal",
  score: "3,2",
  level: "beginning",
  done: true
}, {
  name: "Ignacia Lobos",
  score: "—",
  level: null,
  done: false
}];
const CRITERIA = [{
  name: "Comprensión del concepto",
  weight: "30%",
  level: "meets",
  reason: "Identifica correctamente las fases lumínica y oscura, pero confunde el rol del NADPH en la cadena. La definición es precisa aunque incompleta."
}, {
  name: "Uso de evidencia y ejemplos",
  weight: "25%",
  level: "exceeds",
  reason: "Aporta dos ejemplos pertinentes (plantas C3 y C4) y los relaciona con el contexto climático. Supera lo solicitado."
}, {
  name: "Estructura y argumentación",
  weight: "25%",
  level: "developing",
  reason: "La introducción es clara, pero la conclusión no retoma la tesis y el desarrollo presenta saltos lógicos entre párrafos."
}, {
  name: "Uso de lenguaje técnico",
  weight: "20%",
  level: "meets",
  reason: "Emplea vocabulario disciplinar adecuado (cloroplasto, estroma, tilacoide) con un uso impreciso de «energía» como sinónimo de ATP."
}];
function GradingScreen({
  onPublish
}) {
  const [regrading, setRegrading] = React.useState(false);
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: "240px 1fr",
      gap: 18,
      maxWidth: "var(--content-max)",
      alignItems: "start"
    }
  }, /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, {
    style: {
      fontSize: "var(--text-md)"
    }
  }, "Entregas \xB7 28")), /*#__PURE__*/React.createElement("div", {
    style: {
      padding: "6px 0 8px"
    }
  }, STUDENTS.map((s, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    style: {
      ...gradeStyles.stuRow,
      ...(s.active ? gradeStyles.stuActive : {})
    }
  }, /*#__PURE__*/React.createElement(Avatar, {
    name: s.name,
    size: "sm"
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: gradeStyles.stuName
  }, s.name), /*#__PURE__*/React.createElement("div", {
    style: gradeStyles.stuMeta
  }, s.done ? "Corregida por IA" : "Sin entrega")), s.done ? /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-display)",
      fontWeight: 700,
      fontSize: "var(--text-md)",
      color: s.active ? "var(--brand)" : "var(--text-strong)"
    }
  }, s.score) : /*#__PURE__*/React.createElement(Icon, {
    name: "clock",
    size: 16,
    color: "var(--text-subtle)"
  }))))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 16,
      position: "relative"
    }
  }, regrading && /*#__PURE__*/React.createElement(LoadingOverlay, {
    label: "Recalificando con IA\u2026"
  }), /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement("div", {
    style: gradeStyles.head
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 12
    }
  }, /*#__PURE__*/React.createElement(Avatar, {
    name: "Diego Soto",
    size: "md"
  }), /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-display)",
      fontWeight: 700,
      fontSize: "var(--text-lg)",
      color: "var(--text-strong)"
    }
  }, "Diego Soto"), /*#__PURE__*/React.createElement("div", {
    style: {
      fontSize: "var(--text-sm)",
      color: "var(--text-muted)"
    }
  }, "Ensayo: Fotos\xEDntesis \xB7 2\xB0A Biolog\xEDa \xB7 entregado a tiempo"))), /*#__PURE__*/React.createElement("div", {
    style: {
      textAlign: "right"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-display)",
      fontWeight: 800,
      fontSize: "var(--text-3xl)",
      color: "var(--text-strong)",
      lineHeight: 1
    }
  }, "5,1"), /*#__PURE__*/React.createElement(RubricLevel, {
    level: "meets"
  }))), /*#__PURE__*/React.createElement("div", {
    style: {
      padding: "0 20px 18px",
      display: "flex",
      alignItems: "center",
      gap: 10
    }
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "sparkles",
    size: 16,
    color: "var(--brand)"
  }), /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: "var(--text-sm)",
      color: "var(--text-muted)"
    }
  }, "Corregido autom\xE1ticamente seg\xFAn la r\xFAbrica de 4 \xEDndices. Revisa y publica."))), /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, null, "R\xFAbrica e indicadores"), /*#__PURE__*/React.createElement(Badge, {
    tone: "brand"
  }, "4 \xEDndices")), /*#__PURE__*/React.createElement("div", null, CRITERIA.map((c, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    style: gradeStyles.crit
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      justifyContent: "space-between",
      alignItems: "center",
      gap: 12,
      marginBottom: 8
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 10
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      fontWeight: 600,
      color: "var(--text-strong)"
    }
  }, c.name), /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: "var(--text-xs)",
      color: "var(--text-subtle)",
      fontFamily: "var(--font-mono)"
    }
  }, c.weight)), /*#__PURE__*/React.createElement(RubricLevel, {
    level: c.level
  })), /*#__PURE__*/React.createElement("div", {
    style: gradeStyles.reason
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "quote",
    size: 14,
    color: "var(--text-subtle)",
    style: {
      marginTop: 2
    }
  }), /*#__PURE__*/React.createElement("span", null, c.reason)))))), /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, null, "Retroalimentaci\xF3n para el estudiante")), /*#__PURE__*/React.createElement(Card.Body, null, /*#__PURE__*/React.createElement(Textarea, {
    defaultValue: "Buen dominio conceptual de la fotosíntesis, Diego. Tu mayor fortaleza fue el uso de ejemplos (plantas C3 y C4). Para mejorar, enfócate en cerrar tus ensayos retomando la tesis y en cuidar las transiciones entre párrafos. Repasa el rol del NADPH en la fase oscura.",
    style: {
      minHeight: 110
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: gradeStyles.lowpoints
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "target",
    size: 16,
    color: "var(--gold-600)"
  }), /*#__PURE__*/React.createElement("span", null, /*#__PURE__*/React.createElement("b", null, "Puntos a mejorar:"), " estructura y argumentaci\xF3n \xB7 rol del NADPH"))), /*#__PURE__*/React.createElement(Card.Footer, null, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      justifyContent: "space-between",
      alignItems: "center",
      gap: 12
    }
  }, /*#__PURE__*/React.createElement(Button, {
    variant: "secondary",
    size: "sm",
    iconLeft: /*#__PURE__*/React.createElement(Icon, {
      name: "refresh-cw",
      size: 15
    }),
    onClick: () => {
      setRegrading(true);
      setTimeout(() => setRegrading(false), 1500);
    }
  }, "Recalificar con IA"), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      gap: 10
    }
  }, /*#__PURE__*/React.createElement(Button, {
    variant: "ghost"
  }, "Guardar borrador"), /*#__PURE__*/React.createElement(Button, {
    variant: "primary",
    iconLeft: /*#__PURE__*/React.createElement(Icon, {
      name: "send",
      size: 16
    }),
    onClick: onPublish
  }, "Publicar y enviar enlace")))))));
}
const gradeStyles = {
  stuRow: {
    display: "flex",
    alignItems: "center",
    gap: 10,
    padding: "9px 14px",
    borderLeft: "3px solid transparent",
    cursor: "pointer"
  },
  stuActive: {
    background: "var(--surface-brand-soft)",
    borderLeftColor: "var(--brand)"
  },
  stuName: {
    fontWeight: 600,
    fontSize: "var(--text-sm)",
    color: "var(--text-strong)",
    whiteSpace: "nowrap",
    overflow: "hidden",
    textOverflow: "ellipsis"
  },
  stuMeta: {
    fontSize: "var(--text-xs)",
    color: "var(--text-subtle)"
  },
  head: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "flex-start",
    padding: "20px 20px 14px",
    gap: 16
  },
  crit: {
    padding: "14px 20px",
    borderBottom: "1px solid var(--border-subtle)"
  },
  reason: {
    display: "flex",
    gap: 8,
    fontSize: "var(--text-sm)",
    color: "var(--text-muted)",
    lineHeight: 1.55,
    background: "var(--surface-sunken)",
    padding: "10px 12px",
    borderRadius: "var(--radius-sm)"
  },
  lowpoints: {
    display: "flex",
    alignItems: "center",
    gap: 8,
    marginTop: 12,
    padding: "10px 12px",
    background: "var(--gold-50)",
    border: "1px solid var(--gold-200)",
    borderRadius: "var(--radius-sm)",
    fontSize: "var(--text-sm)",
    color: "var(--gold-800)"
  }
};
window.GradingScreen = GradingScreen;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/teacher/GradingScreen.jsx", error: String((e && e.message) || e) }); }

// ui_kits/teacher/LoginScreen.jsx
try { (() => {
// Teacher login — Google SSO + email/password.
const {
  Button,
  Input,
  Field,
  Spinner
} = window.GradeOpsAIDesignSystem_fcd12b;
function LoginScreen({
  onLogin
}) {
  const [loading, setLoading] = React.useState(false);
  const go = () => {
    setLoading(true);
    setTimeout(onLogin, 1100);
  };
  return /*#__PURE__*/React.createElement("div", {
    style: loginStyles.root
  }, /*#__PURE__*/React.createElement("div", {
    style: loginStyles.left
  }, /*#__PURE__*/React.createElement("div", {
    style: loginStyles.brand
  }, /*#__PURE__*/React.createElement("img", {
    src: "../../assets/logo-mark.svg",
    alt: "",
    style: {
      width: 40,
      height: 40
    }
  }), /*#__PURE__*/React.createElement("span", {
    style: loginStyles.brandText
  }, "GradeOps", /*#__PURE__*/React.createElement("span", {
    style: {
      color: "var(--brand)"
    }
  }, "AI"))), /*#__PURE__*/React.createElement("div", {
    style: loginStyles.card
  }, /*#__PURE__*/React.createElement("h1", {
    style: loginStyles.title
  }, "Bienvenida de vuelta"), /*#__PURE__*/React.createElement("p", {
    style: loginStyles.sub
  }, "Entra para crear y corregir evaluaciones con IA."), /*#__PURE__*/React.createElement("button", {
    style: loginStyles.google,
    onClick: go,
    onMouseEnter: e => e.currentTarget.style.background = "var(--surface-sunken)",
    onMouseLeave: e => e.currentTarget.style.background = "var(--surface-card)"
  }, /*#__PURE__*/React.createElement("svg", {
    width: "18",
    height: "18",
    viewBox: "0 0 48 48"
  }, /*#__PURE__*/React.createElement("path", {
    fill: "#EA4335",
    d: "M24 9.5c3.5 0 6.6 1.2 9 3.6l6.7-6.7C35.6 2.4 30.1 0 24 0 14.6 0 6.4 5.4 2.5 13.2l7.8 6.1C12.2 13.3 17.6 9.5 24 9.5z"
  }), /*#__PURE__*/React.createElement("path", {
    fill: "#4285F4",
    d: "M46.5 24.5c0-1.6-.1-3.1-.4-4.5H24v9h12.7c-.5 3-2.2 5.5-4.7 7.2l7.3 5.7c4.3-4 6.8-9.9 6.8-17.4z"
  }), /*#__PURE__*/React.createElement("path", {
    fill: "#FBBC05",
    d: "M10.3 28.7c-.5-1.5-.8-3-.8-4.7s.3-3.2.8-4.7l-7.8-6.1C.9 16.5 0 20.1 0 24s.9 7.5 2.5 10.8l7.8-6.1z"
  }), /*#__PURE__*/React.createElement("path", {
    fill: "#34A853",
    d: "M24 48c6.1 0 11.3-2 15-5.5l-7.3-5.7c-2 1.4-4.7 2.3-7.7 2.3-6.4 0-11.8-3.8-13.7-9.3l-7.8 6.1C6.4 42.6 14.6 48 24 48z"
  })), loading ? "Conectando…" : "Continuar con Google", loading && /*#__PURE__*/React.createElement(Spinner, {
    size: "xs"
  })), /*#__PURE__*/React.createElement("div", {
    style: loginStyles.divider
  }, /*#__PURE__*/React.createElement("span", {
    style: loginStyles.divLine
  }), /*#__PURE__*/React.createElement("span", {
    style: {
      padding: "0 12px"
    }
  }, "o con tu correo"), /*#__PURE__*/React.createElement("span", {
    style: loginStyles.divLine
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 14
    }
  }, /*#__PURE__*/React.createElement(Field, {
    label: "Correo institucional",
    htmlFor: "em"
  }, /*#__PURE__*/React.createElement(Input, {
    id: "em",
    type: "email",
    placeholder: "paula.mendez@liceoandes.cl",
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "mail",
      size: 18
    }),
    defaultValue: "paula.mendez@liceoandes.cl"
  })), /*#__PURE__*/React.createElement(Field, {
    label: "Contrase\xF1a",
    htmlFor: "pw"
  }, /*#__PURE__*/React.createElement(Input, {
    id: "pw",
    type: "password",
    placeholder: "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022",
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "lock",
      size: 18
    }),
    defaultValue: "demodemo"
  })), /*#__PURE__*/React.createElement(Button, {
    block: true,
    loading: loading,
    onClick: go
  }, "Iniciar sesi\xF3n"), /*#__PURE__*/React.createElement("a", {
    href: "#",
    style: loginStyles.forgot
  }, "\xBFOlvidaste tu contrase\xF1a?"))), /*#__PURE__*/React.createElement("p", {
    style: loginStyles.foot
  }, "Portal docente \xB7 \xBFEres estudiante? Usa el enlace de tu correo.")), /*#__PURE__*/React.createElement("div", {
    style: loginStyles.right
  }, /*#__PURE__*/React.createElement("div", {
    style: loginStyles.quoteWrap
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "graduation-cap",
    size: 40,
    color: "rgba(255,255,255,0.9)"
  }), /*#__PURE__*/React.createElement("p", {
    style: loginStyles.quote
  }, "\u201CCorrijo una prueba de 32 alumnos en lo que antes me tomaba una tarde entera.\u201D"), /*#__PURE__*/React.createElement("div", {
    style: loginStyles.quoteAuthor
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontWeight: 700
    }
  }, "Rodrigo Salinas"), /*#__PURE__*/React.createElement("div", {
    style: {
      opacity: 0.8
    }
  }, "Profesor de Historia \xB7 Colegio del Valle")), /*#__PURE__*/React.createElement("div", {
    style: loginStyles.statRow
  }, /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("div", {
    style: loginStyles.statNum
  }, "+8.400"), /*#__PURE__*/React.createElement("div", {
    style: loginStyles.statLbl
  }, "docentes")), /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("div", {
    style: loginStyles.statNum
  }, "1,2M"), /*#__PURE__*/React.createElement("div", {
    style: loginStyles.statLbl
  }, "correcciones")), /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("div", {
    style: loginStyles.statNum
  }, "9 min"), /*#__PURE__*/React.createElement("div", {
    style: loginStyles.statLbl
  }, "ahorro/prueba"))))));
}
const loginStyles = {
  root: {
    display: "flex",
    height: "100%",
    fontFamily: "var(--font-sans)",
    background: "var(--surface-page)"
  },
  left: {
    flex: "1 1 52%",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    justifyContent: "center",
    padding: "40px 24px",
    position: "relative"
  },
  brand: {
    display: "flex",
    alignItems: "center",
    gap: 11,
    marginBottom: 22
  },
  brandText: {
    fontFamily: "var(--font-display)",
    fontWeight: 700,
    fontSize: 23,
    color: "var(--text-strong)",
    letterSpacing: "-0.02em"
  },
  card: {
    width: "100%",
    maxWidth: 380
  },
  title: {
    fontFamily: "var(--font-display)",
    fontWeight: 700,
    fontSize: "var(--text-3xl)",
    color: "var(--text-strong)",
    margin: "0 0 6px",
    letterSpacing: "-0.02em"
  },
  sub: {
    margin: "0 0 24px",
    color: "var(--text-muted)",
    fontSize: "var(--text-md)"
  },
  google: {
    width: "100%",
    height: 46,
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    gap: 10,
    border: "1px solid var(--border-default)",
    background: "var(--surface-card)",
    borderRadius: "var(--radius-md)",
    cursor: "pointer",
    fontFamily: "inherit",
    fontWeight: 600,
    fontSize: "var(--text-md)",
    color: "var(--text-body)",
    transition: "background 120ms"
  },
  divider: {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    color: "var(--text-subtle)",
    fontSize: "var(--text-xs)",
    margin: "20px 0"
  },
  divLine: {
    flex: 1,
    height: 1,
    background: "var(--border-subtle)"
  },
  forgot: {
    textAlign: "center",
    fontSize: "var(--text-sm)",
    color: "var(--text-link)",
    marginTop: 2
  },
  foot: {
    marginTop: 28,
    fontSize: "var(--text-xs)",
    color: "var(--text-subtle)",
    textAlign: "center"
  },
  right: {
    flex: "1 1 48%",
    background: "linear-gradient(150deg, var(--sprout-600), var(--sprout-800))",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    padding: 40
  },
  quoteWrap: {
    maxWidth: 420,
    color: "#fff"
  },
  quote: {
    fontFamily: "var(--font-display)",
    fontWeight: 600,
    fontSize: "var(--text-2xl)",
    lineHeight: 1.3,
    margin: "22px 0 20px",
    letterSpacing: "-0.01em"
  },
  quoteAuthor: {
    fontSize: "var(--text-md)",
    lineHeight: 1.5
  },
  statRow: {
    display: "flex",
    gap: 28,
    marginTop: 40,
    paddingTop: 24,
    borderTop: "1px solid rgba(255,255,255,0.2)"
  },
  statNum: {
    fontFamily: "var(--font-display)",
    fontWeight: 700,
    fontSize: "var(--text-2xl)"
  },
  statLbl: {
    fontSize: "var(--text-xs)",
    opacity: 0.8,
    marginTop: 2
  }
};
window.LoginScreen = LoginScreen;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/teacher/LoginScreen.jsx", error: String((e && e.message) || e) }); }

// ui_kits/teacher/ReportsScreen.jsx
try { (() => {
// ReportsScreen.jsx — drill-down: Global → Curso → Asignatura → Detalle alumno
const {
  Card,
  StatCard,
  Badge,
  Button,
  ProgressBar,
  RubricLevel
} = window.GradeOpsAIDesignSystem_fcd12b;
const {
  students,
  SUBJECTS_BY_COURSE,
  COURSES,
  subjectStats,
  _avg,
  _r1
} = window.TEACHER_DATA;

// ── Global View ───────────────────────────────────────────────────────────────
const WEAK_TOPICS = [{
  topic: "Derivadas implícitas",
  course: "2°B",
  subject: "Matemática",
  pct: 38,
  level: "beginning"
}, {
  topic: "Revolución Industrial · causas",
  course: "2°A",
  subject: "Historia",
  pct: 44,
  level: "beginning"
}, {
  topic: "Análisis literario",
  course: "3°A",
  subject: "Lenguaje",
  pct: 51,
  level: "developing"
}, {
  topic: "Fotosíntesis fase lumínica",
  course: "1°B",
  subject: "Biología",
  pct: 57,
  level: "developing"
}];
function RptGlobalView({
  onCourse
}) {
  const allAvg = _r1(_avg(students.map(s => s.avg)));
  const riskCount = students.filter(s => s.risk).length;
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 22
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: "repeat(4,1fr)",
      gap: 16
    }
  }, /*#__PURE__*/React.createElement(StatCard, {
    label: "Promedio global",
    value: allAvg.toFixed(1),
    delta: "+0,2",
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "trending-up",
      size: 19
    })
  }), /*#__PURE__*/React.createElement(StatCard, {
    label: "Total estudiantes",
    value: students.length,
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "users",
      size: 19
    }),
    iconTone: "info"
  }), /*#__PURE__*/React.createElement(StatCard, {
    label: "Evaluaciones activas",
    value: "6",
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "file-pen-line",
      size: 19
    }),
    iconTone: "gold"
  }), /*#__PURE__*/React.createElement(StatCard, {
    label: "En riesgo",
    value: riskCount,
    unit: "alumnos",
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "alert-triangle",
      size: 19
    }),
    iconTone: "danger",
    delta: "-1",
    deltaDir: "down"
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: "1.4fr 1fr",
      gap: 18,
      alignItems: "start"
    }
  }, /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, null, "Rendimiento por curso"), /*#__PURE__*/React.createElement(Button, {
    variant: "ghost",
    size: "sm",
    iconLeft: /*#__PURE__*/React.createElement(Icon, {
      name: "download",
      size: 15
    })
  }, "Exportar PDF")), /*#__PURE__*/React.createElement(Card.Body, null, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 18
    }
  }, COURSES.map(c => {
    const cs = students.filter(s => s.course === c);
    const ca = cs.length ? _r1(_avg(cs.map(s => s.avg))) : 0;
    const cr = cs.filter(s => s.risk).length;
    return /*#__PURE__*/React.createElement("div", {
      key: c
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        alignItems: "baseline",
        justifyContent: "space-between",
        marginBottom: 8
      }
    }, /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("button", {
      onClick: () => onCourse(c),
      style: {
        fontWeight: 700,
        fontSize: "var(--text-md)",
        color: "var(--brand)",
        background: "none",
        border: "none",
        cursor: "pointer",
        padding: 0,
        fontFamily: "var(--font-sans)"
      },
      onMouseEnter: e => e.currentTarget.style.textDecoration = "underline",
      onMouseLeave: e => e.currentTarget.style.textDecoration = "none"
    }, c), /*#__PURE__*/React.createElement("span", {
      style: {
        fontSize: "var(--text-xs)",
        color: "var(--text-subtle)",
        marginLeft: 10
      }
    }, cs.length, " alumnos \xB7 ", (SUBJECTS_BY_COURSE[c] || []).length, " asignaturas", cr > 0 && /*#__PURE__*/React.createElement(React.Fragment, null, " \xB7 ", /*#__PURE__*/React.createElement("span", {
      style: {
        color: "var(--danger-600)",
        fontWeight: 600
      }
    }, cr, " en riesgo")))), /*#__PURE__*/React.createElement("span", {
      style: {
        fontFamily: "var(--font-display)",
        fontWeight: 700,
        fontSize: "var(--text-xl)",
        color: scoreColor(ca)
      }
    }, ca.toFixed(1))), /*#__PURE__*/React.createElement(ProgressBar, {
      value: ca / 7 * 100,
      tone: ca < 4.5 ? "danger" : ca >= 5.5 ? "success" : "brand"
    }));
  })))), /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, null, "Temas con menor logro")), /*#__PURE__*/React.createElement(Card.Body, null, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 18
    }
  }, WEAK_TOPICS.map((w, i) => /*#__PURE__*/React.createElement("div", {
    key: i
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      justifyContent: "space-between",
      marginBottom: 8
    }
  }, /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("div", {
    style: {
      fontWeight: 600,
      fontSize: "var(--text-sm)",
      color: "var(--text-strong)"
    }
  }, w.topic), /*#__PURE__*/React.createElement("div", {
    style: {
      fontSize: "var(--text-xs)",
      color: "var(--text-subtle)",
      marginTop: 1
    }
  }, w.course, " \xB7 ", w.subject)), /*#__PURE__*/React.createElement(RubricLevel, {
    level: w.level,
    solid: true
  })), /*#__PURE__*/React.createElement(ProgressBar, {
    value: w.pct,
    tone: w.pct < 45 ? "danger" : "gold",
    showValue: true
  }))))))));
}

// ── Course View ───────────────────────────────────────────────────────────────
function RptCourseView({
  course,
  onSubject,
  onStudent
}) {
  const cs = students.filter(s => s.course === course);
  const subjects = SUBJECTS_BY_COURSE[course] || [];
  const ca = cs.length ? _r1(_avg(cs.map(s => s.avg))) : 0;
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 22
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: "repeat(4,1fr)",
      gap: 14
    }
  }, /*#__PURE__*/React.createElement(StatCard, {
    label: "Promedio curso",
    value: ca.toFixed(1),
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "bar-chart-3",
      size: 19
    }),
    iconTone: "success"
  }), /*#__PURE__*/React.createElement(StatCard, {
    label: "Estudiantes",
    value: cs.length,
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "users",
      size: 19
    }),
    iconTone: "info"
  }), /*#__PURE__*/React.createElement(StatCard, {
    label: "Asignaturas",
    value: subjects.length,
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "book-open",
      size: 19
    })
  }), /*#__PURE__*/React.createElement(StatCard, {
    label: "En riesgo",
    value: cs.filter(s => s.risk).length,
    unit: "alumnos",
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "alert-triangle",
      size: 19
    }),
    iconTone: "danger"
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: `repeat(${subjects.length},1fr)`,
      gap: 14
    }
  }, subjects.map(sub => {
    const st = subjectStats(course, sub);
    if (!st) return null;
    return /*#__PURE__*/React.createElement("button", {
      key: sub,
      onClick: () => onSubject(sub),
      style: {
        border: "1px solid var(--border-subtle)",
        borderRadius: "var(--radius-md)",
        background: "var(--surface-card)",
        padding: "16px 18px",
        cursor: "pointer",
        textAlign: "left",
        transition: "box-shadow 140ms, border-color 140ms"
      },
      onMouseEnter: e => {
        e.currentTarget.style.borderColor = "var(--brand)";
        e.currentTarget.style.boxShadow = "var(--shadow-md)";
      },
      onMouseLeave: e => {
        e.currentTarget.style.borderColor = "var(--border-subtle)";
        e.currentTarget.style.boxShadow = "none";
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        justifyContent: "space-between"
      }
    }, /*#__PURE__*/React.createElement("span", {
      style: {
        fontWeight: 700,
        fontSize: "var(--text-md)",
        color: "var(--text-strong)"
      }
    }, sub), st.riskCount > 0 && /*#__PURE__*/React.createElement(Badge, {
      tone: "danger",
      dot: true
    }, st.riskCount)), /*#__PURE__*/React.createElement("div", {
      style: {
        fontFamily: "var(--font-display)",
        fontWeight: 700,
        fontSize: "var(--text-3xl)",
        color: scoreColor(st.avg),
        margin: "10px 0 6px"
      }
    }, st.avg.toFixed(1)), /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        gap: 12,
        fontSize: "var(--text-xs)",
        color: "var(--text-subtle)",
        marginBottom: 10
      }
    }, /*#__PURE__*/React.createElement("span", null, "M\xE1x ", /*#__PURE__*/React.createElement("b", {
      style: {
        color: "var(--success-600)"
      }
    }, st.max.toFixed(1))), /*#__PURE__*/React.createElement("span", null, "M\xEDn ", /*#__PURE__*/React.createElement("b", {
      style: {
        color: "var(--danger-600)"
      }
    }, st.min.toFixed(1)))), /*#__PURE__*/React.createElement(ProgressBar, {
      value: st.avg / 7 * 100,
      tone: st.avg < 4 ? "danger" : st.avg >= 5.5 ? "success" : "brand"
    }));
  })), /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, null, "Desglose por asignatura \xB7 ", course)), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: "1fr 90px 80px 80px 80px 80px",
      gap: 14,
      padding: "10px 20px",
      borderBottom: "1px solid var(--border-subtle)",
      background: "var(--surface-sunken)"
    }
  }, ["Asignatura", "Promedio", "Máx", "Mín", "Eval.", "Riesgo"].map((h, i) => /*#__PURE__*/React.createElement("span", {
    key: i,
    style: {
      fontSize: "var(--text-xs)",
      fontWeight: 700,
      color: "var(--text-subtle)",
      textTransform: "uppercase",
      letterSpacing: ".05em",
      textAlign: i > 0 ? "center" : "left"
    }
  }, h))), subjects.map((sub, i) => {
    const st = subjectStats(course, sub);
    if (!st) return null;
    return /*#__PURE__*/React.createElement("div", {
      key: sub,
      onClick: () => onSubject(sub),
      style: {
        display: "grid",
        gridTemplateColumns: "1fr 90px 80px 80px 80px 80px",
        gap: 14,
        alignItems: "center",
        padding: "13px 20px",
        borderBottom: i < subjects.length - 1 ? "1px solid var(--border-subtle)" : "none",
        cursor: "pointer",
        transition: "background 100ms"
      },
      onMouseEnter: e => e.currentTarget.style.background = "var(--surface-sunken)",
      onMouseLeave: e => e.currentTarget.style.background = "transparent"
    }, /*#__PURE__*/React.createElement("span", {
      style: {
        fontWeight: 600,
        fontSize: "var(--text-md)",
        color: "var(--brand)"
      }
    }, sub), /*#__PURE__*/React.createElement("div", {
      style: {
        textAlign: "center",
        fontFamily: "var(--font-display)",
        fontWeight: 700,
        fontSize: "var(--text-xl)",
        color: scoreColor(st.avg)
      }
    }, st.avg.toFixed(1)), /*#__PURE__*/React.createElement("div", {
      style: {
        textAlign: "center",
        fontWeight: 600,
        color: "var(--success-600)"
      }
    }, st.max.toFixed(1)), /*#__PURE__*/React.createElement("div", {
      style: {
        textAlign: "center",
        fontWeight: 600,
        color: "var(--danger-600)"
      }
    }, st.min.toFixed(1)), /*#__PURE__*/React.createElement("div", {
      style: {
        textAlign: "center",
        color: "var(--text-muted)"
      }
    }, st.totalEvals), /*#__PURE__*/React.createElement("div", {
      style: {
        textAlign: "center"
      }
    }, st.riskCount > 0 ? /*#__PURE__*/React.createElement(Badge, {
      tone: "danger",
      dot: true
    }, st.riskCount) : /*#__PURE__*/React.createElement("span", {
      style: {
        color: "var(--text-subtle)"
      }
    }, "\u2014")));
  })));
}

// ── Subject View ──────────────────────────────────────────────────────────────
function RptSubjectView({
  course,
  subject,
  onStudent
}) {
  const st = subjectStats(course, subject);
  if (!st) return null;
  const sorted = [...st.students].sort((a, b) => b.grades[subject].avg - a.grades[subject].avg);
  const weakTopics = WEAK_TOPICS.filter(w => w.course === course && w.subject === subject);
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 22
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: "repeat(4,1fr)",
      gap: 14
    }
  }, /*#__PURE__*/React.createElement(StatCard, {
    label: "Promedio",
    value: st.avg.toFixed(1),
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "bar-chart-3",
      size: 19
    }),
    iconTone: "success"
  }), /*#__PURE__*/React.createElement(StatCard, {
    label: "Nota m\xE1s alta",
    value: st.max.toFixed(1),
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "trending-up",
      size: 19
    }),
    iconTone: "info"
  }), /*#__PURE__*/React.createElement(StatCard, {
    label: "Nota m\xE1s baja",
    value: st.min.toFixed(1),
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "trending-down",
      size: 19
    }),
    iconTone: "danger"
  }), /*#__PURE__*/React.createElement(StatCard, {
    label: "En riesgo",
    value: st.riskCount,
    unit: "alumnos",
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "alert-triangle",
      size: 19
    }),
    iconTone: "danger"
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: weakTopics.length ? "1.6fr 1fr" : "1fr",
      gap: 18,
      alignItems: "start"
    }
  }, /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, null, "Ranking \xB7 ", course, " ", subject), /*#__PURE__*/React.createElement(Badge, {
    tone: "info"
  }, st.totalEvals, " evaluaciones")), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: "36px 1fr 130px 70px 80px 90px",
      gap: 12,
      padding: "10px 20px",
      borderBottom: "1px solid var(--border-subtle)",
      background: "var(--surface-sunken)"
    }
  }, ["#", "Estudiante", "Progreso", "Nota", "Tendencia", ""].map((h, i) => /*#__PURE__*/React.createElement("span", {
    key: i,
    style: {
      fontSize: "var(--text-xs)",
      fontWeight: 700,
      color: "var(--text-subtle)",
      textTransform: "uppercase",
      letterSpacing: ".05em",
      textAlign: i > 1 && i < 5 ? "center" : "left"
    }
  }, h))), sorted.map((s, i) => {
    const g = s.grades[subject];
    return /*#__PURE__*/React.createElement("div", {
      key: s.id,
      onClick: () => onStudent(s),
      style: {
        display: "grid",
        gridTemplateColumns: "36px 1fr 130px 70px 80px 90px",
        gap: 12,
        alignItems: "center",
        padding: "12px 20px",
        borderBottom: i < sorted.length - 1 ? "1px solid var(--border-subtle)" : "none",
        cursor: "pointer",
        transition: "background 100ms"
      },
      onMouseEnter: e => e.currentTarget.style.background = "var(--surface-sunken)",
      onMouseLeave: e => e.currentTarget.style.background = "transparent"
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        fontFamily: "var(--font-display)",
        fontWeight: 700,
        fontSize: "var(--text-lg)",
        color: i === 0 ? "var(--gold-600)" : i === 1 ? "var(--slate-400)" : i === 2 ? "var(--brand-soft-fg, var(--brand))" : "var(--text-subtle)",
        textAlign: "center"
      }
    }, i + 1), /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        alignItems: "center",
        gap: 10
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        width: 28,
        height: 28,
        borderRadius: "50%",
        background: "var(--surface-sunken)",
        border: "1px solid var(--border-subtle)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontSize: 11,
        fontWeight: 700,
        color: "var(--text-muted)",
        flexShrink: 0
      }
    }, s.name.split(" ").map(n => n[0]).join("").slice(0, 2)), /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("div", {
      style: {
        fontWeight: 600,
        fontSize: "var(--text-sm)",
        color: "var(--text-strong)"
      }
    }, s.name), /*#__PURE__*/React.createElement("div", {
      style: {
        fontSize: "var(--text-xs)",
        color: "var(--text-subtle)"
      }
    }, g.evals, " eval."))), /*#__PURE__*/React.createElement(ProgressBar, {
      value: g.avg / 7 * 100,
      tone: g.avg < 4 ? "danger" : g.avg >= 5.5 ? "success" : "brand"
    }), /*#__PURE__*/React.createElement("div", {
      style: {
        textAlign: "center",
        fontFamily: "var(--font-display)",
        fontWeight: 700,
        fontSize: "var(--text-xl)",
        color: scoreColor(g.avg)
      }
    }, g.avg.toFixed(1)), /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        justifyContent: "center"
      }
    }, /*#__PURE__*/React.createElement(TrendIcon, {
      t: g.trend
    })), /*#__PURE__*/React.createElement("div", null, g.risk ? /*#__PURE__*/React.createElement(Badge, {
      tone: "danger",
      dot: true
    }, "En riesgo") : /*#__PURE__*/React.createElement("span", {
      style: {
        fontSize: "var(--text-xs)",
        color: "var(--text-subtle)"
      }
    }, "\u2014")));
  })), weakTopics.length > 0 && /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, null, "Temas con menor logro")), /*#__PURE__*/React.createElement(Card.Body, null, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 18
    }
  }, weakTopics.map((w, i) => /*#__PURE__*/React.createElement("div", {
    key: i
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      justifyContent: "space-between",
      marginBottom: 8
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontWeight: 600,
      fontSize: "var(--text-sm)",
      color: "var(--text-strong)"
    }
  }, w.topic), /*#__PURE__*/React.createElement(RubricLevel, {
    level: w.level,
    solid: true
  })), /*#__PURE__*/React.createElement(ProgressBar, {
    value: w.pct,
    tone: w.pct < 45 ? "danger" : "gold",
    showValue: true
  }))))))));
}

// ── Orchestrator ──────────────────────────────────────────────────────────────
function ReportsScreen() {
  const [view, setView] = React.useState({
    level: "global"
  });
  const crumbs = [{
    label: "Reportes"
  }];
  if (view.course) crumbs.push({
    label: view.course
  });
  if (view.subject) crumbs.push({
    label: view.subject
  });
  if (view.student) crumbs.push({
    label: view.student.name
  });
  const goTo = idx => {
    if (idx === 0) setView({
      level: "global"
    });
    if (idx === 1) setView({
      level: "course",
      course: view.course
    });
    if (idx === 2) setView({
      level: "subject",
      course: view.course,
      subject: view.subject
    });
  };
  return /*#__PURE__*/React.createElement("div", {
    style: {
      maxWidth: "var(--content-max)",
      display: "flex",
      flexDirection: "column",
      gap: 0
    }
  }, crumbs.length > 1 && /*#__PURE__*/React.createElement(Breadcrumb, {
    items: crumbs,
    onNavigate: goTo
  }), view.level === "global" && /*#__PURE__*/React.createElement(RptGlobalView, {
    onCourse: c => setView({
      level: "course",
      course: c
    })
  }), view.level === "course" && /*#__PURE__*/React.createElement(RptCourseView, {
    course: view.course,
    onSubject: sub => setView({
      level: "subject",
      course: view.course,
      subject: sub
    }),
    onStudent: s => setView({
      level: "student",
      student: s,
      course: view.course
    })
  }), view.level === "subject" && /*#__PURE__*/React.createElement(RptSubjectView, {
    course: view.course,
    subject: view.subject,
    onStudent: s => setView({
      level: "student",
      student: s,
      course: view.course,
      subject: view.subject
    })
  }), view.level === "student" && /*#__PURE__*/React.createElement(StudentDetail, {
    student: view.student
  }));
}
window.ReportsScreen = ReportsScreen;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/teacher/ReportsScreen.jsx", error: String((e && e.message) || e) }); }

// ui_kits/teacher/Shell.jsx
try { (() => {
// Teacher portal app shell: sidebar + topbar. Composes DS primitives.
const {
  Avatar,
  Badge,
  IconButton,
  Button
} = window.GradeOpsAIDesignSystem_fcd12b;
const NAV = [{
  id: "dashboard",
  label: "Panel",
  icon: "layout-dashboard"
}, {
  id: "assessments",
  label: "Evaluaciones",
  icon: "file-pen-line"
}, {
  id: "bank",
  label: "Banco de preguntas",
  icon: "library"
}, {
  id: "students",
  label: "Estudiantes",
  icon: "users"
}, {
  id: "reports",
  label: "Reportes",
  icon: "bar-chart-3"
}];
function Shell({
  active,
  onNavigate,
  title,
  subtitle,
  actions,
  children
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: shellStyles.root
  }, /*#__PURE__*/React.createElement("aside", {
    style: shellStyles.sidebar
  }, /*#__PURE__*/React.createElement("div", {
    style: shellStyles.brand
  }, /*#__PURE__*/React.createElement("img", {
    src: "../../assets/logo-mark.svg",
    alt: "",
    style: {
      width: 34,
      height: 34
    }
  }), /*#__PURE__*/React.createElement("span", {
    style: shellStyles.brandText
  }, "GradeOps", /*#__PURE__*/React.createElement("span", {
    style: {
      color: "var(--brand)"
    }
  }, "AI"))), /*#__PURE__*/React.createElement("nav", {
    style: shellStyles.nav
  }, NAV.map(n => {
    const on = n.id === active;
    return /*#__PURE__*/React.createElement("button", {
      key: n.id,
      onClick: () => onNavigate(n.id),
      style: {
        ...shellStyles.navItem,
        ...(on ? shellStyles.navItemActive : {})
      },
      onMouseEnter: e => {
        if (!on) e.currentTarget.style.background = "var(--surface-sunken)";
      },
      onMouseLeave: e => {
        if (!on) e.currentTarget.style.background = "transparent";
      }
    }, /*#__PURE__*/React.createElement(Icon, {
      name: n.icon,
      size: 19
    }), n.label);
  })), /*#__PURE__*/React.createElement("div", {
    style: shellStyles.upsell
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 8,
      marginBottom: 6
    }
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "sparkles",
    size: 16,
    color: "var(--gold-600)"
  }), /*#__PURE__*/React.createElement("span", {
    style: {
      fontWeight: 700,
      fontSize: "var(--text-sm)",
      color: "var(--text-strong)"
    }
  }, "Cr\xE9ditos IA")), /*#__PURE__*/React.createElement("p", {
    style: {
      margin: 0,
      fontSize: "var(--text-xs)",
      color: "var(--text-muted)",
      lineHeight: 1.5
    }
  }, "742 correcciones restantes este mes.")), /*#__PURE__*/React.createElement("div", {
    style: shellStyles.userRow
  }, /*#__PURE__*/React.createElement(Avatar, {
    name: "Paula M\xE9ndez",
    size: "sm"
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      minWidth: 0,
      flex: 1
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: shellStyles.userName
  }, "Paula M\xE9ndez"), /*#__PURE__*/React.createElement("div", {
    style: shellStyles.userMeta
  }, "Liceo Andes")), /*#__PURE__*/React.createElement(IconButton, {
    label: "Cerrar sesi\xF3n",
    size: "sm"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "log-out",
    size: 16
  })))), /*#__PURE__*/React.createElement("div", {
    style: shellStyles.main
  }, /*#__PURE__*/React.createElement("header", {
    style: shellStyles.topbar
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement("h1", {
    style: shellStyles.title
  }, title), subtitle && /*#__PURE__*/React.createElement("p", {
    style: shellStyles.subtitle
  }, subtitle)), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 10
    }
  }, /*#__PURE__*/React.createElement(IconButton, {
    label: "Notificaciones"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "bell",
    size: 19
  })), actions)), /*#__PURE__*/React.createElement("div", {
    style: shellStyles.content
  }, children)));
}
const shellStyles = {
  root: {
    display: "flex",
    height: "100%",
    minHeight: 0,
    background: "var(--surface-page)",
    fontFamily: "var(--font-sans)"
  },
  sidebar: {
    width: "var(--sidebar-width)",
    flex: "none",
    background: "var(--surface-card)",
    borderRight: "1px solid var(--border-subtle)",
    display: "flex",
    flexDirection: "column",
    padding: "18px 14px"
  },
  brand: {
    display: "flex",
    alignItems: "center",
    gap: 10,
    padding: "4px 8px 18px"
  },
  brandText: {
    fontFamily: "var(--font-display)",
    fontWeight: 700,
    fontSize: 19,
    color: "var(--text-strong)",
    letterSpacing: "-0.02em"
  },
  nav: {
    display: "flex",
    flexDirection: "column",
    gap: 2
  },
  navItem: {
    display: "flex",
    alignItems: "center",
    gap: 11,
    padding: "9px 11px",
    border: "none",
    background: "transparent",
    borderRadius: "var(--radius-md)",
    cursor: "pointer",
    fontFamily: "inherit",
    fontSize: "var(--text-md)",
    fontWeight: 500,
    color: "var(--text-muted)",
    textAlign: "left",
    transition: "background 120ms, color 120ms"
  },
  navItemActive: {
    background: "var(--surface-brand-soft)",
    color: "var(--brand-hover)",
    fontWeight: 600
  },
  upsell: {
    marginTop: "auto",
    background: "var(--gold-50)",
    border: "1px solid var(--gold-200)",
    borderRadius: "var(--radius-md)",
    padding: "12px",
    margin: "0 2px 14px"
  },
  userRow: {
    display: "flex",
    alignItems: "center",
    gap: 10,
    padding: "10px 8px 2px",
    borderTop: "1px solid var(--border-subtle)"
  },
  userName: {
    fontWeight: 600,
    fontSize: "var(--text-sm)",
    color: "var(--text-strong)",
    whiteSpace: "nowrap",
    overflow: "hidden",
    textOverflow: "ellipsis"
  },
  userMeta: {
    fontSize: "var(--text-xs)",
    color: "var(--text-subtle)"
  },
  main: {
    flex: 1,
    minWidth: 0,
    display: "flex",
    flexDirection: "column",
    overflow: "hidden"
  },
  topbar: {
    height: "var(--topbar-height)",
    flex: "none",
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    gap: 16,
    padding: "0 28px",
    borderBottom: "1px solid var(--border-subtle)",
    background: "var(--surface-card)"
  },
  title: {
    fontFamily: "var(--font-display)",
    fontWeight: 600,
    fontSize: "var(--text-xl)",
    color: "var(--text-strong)",
    margin: 0,
    lineHeight: 1.1,
    letterSpacing: "-0.01em"
  },
  subtitle: {
    margin: "2px 0 0",
    fontSize: "var(--text-sm)",
    color: "var(--text-muted)"
  },
  content: {
    flex: 1,
    overflow: "auto",
    padding: "28px"
  }
};
window.Shell = Shell;
window.TEACHER_NAV = NAV;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/teacher/Shell.jsx", error: String((e && e.message) || e) }); }

// ui_kits/teacher/StudentDetail.jsx
try { (() => {
// StudentDetail.jsx — shared student profile view + Breadcrumb + shared helpers
const {
  Avatar,
  Badge,
  Card,
  ProgressBar,
  RubricLevel,
  StatCard
} = window.GradeOpsAIDesignSystem_fcd12b;
const {
  getEvals
} = window.TEACHER_DATA;

// ── Shared helpers (exposed to sibling screens) ───────────────────────────────
function scoreColor(n) {
  return n < 4 ? "var(--danger-600)" : n >= 5.5 ? "var(--success-600)" : "var(--text-strong)";
}
function TrendIcon({
  t,
  size
}) {
  const sz = size || 16;
  return /*#__PURE__*/React.createElement(Icon, {
    name: t === "up" ? "trending-up" : t === "down" ? "trending-down" : "minus",
    size: sz,
    color: t === "up" ? "var(--success-500)" : t === "down" ? "var(--danger-500)" : "var(--text-subtle)"
  });
}

// ── Breadcrumb (used by both StudentsScreen and ReportsScreen) ────────────────
function Breadcrumb({
  items,
  onNavigate
}) {
  return /*#__PURE__*/React.createElement("nav", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 5,
      marginBottom: 20,
      flexWrap: "wrap"
    }
  }, items.map((item, i) => /*#__PURE__*/React.createElement(React.Fragment, {
    key: i
  }, i < items.length - 1 ? /*#__PURE__*/React.createElement("button", {
    onClick: () => onNavigate(i),
    style: {
      border: "none",
      background: "none",
      cursor: "pointer",
      padding: 0,
      fontSize: "var(--text-sm)",
      color: "var(--brand)",
      fontWeight: 500,
      fontFamily: "var(--font-sans)",
      lineHeight: 1
    },
    onMouseEnter: e => e.currentTarget.style.textDecoration = "underline",
    onMouseLeave: e => e.currentTarget.style.textDecoration = "none"
  }, item.label) : /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: "var(--text-sm)",
      color: "var(--text-strong)",
      fontWeight: 600
    }
  }, item.label), i < items.length - 1 && /*#__PURE__*/React.createElement(Icon, {
    name: "chevron-right",
    size: 13,
    color: "var(--text-subtle)"
  }))));
}

// ── StudentDetail ─────────────────────────────────────────────────────────────
function StudentDetail({
  student
}) {
  const [activeSubject, setActiveSubject] = React.useState(() => Object.keys(student.grades)[0]);
  const subjGrade = student.grades[activeSubject];
  const evals = getEvals(student.id, activeSubject);
  const riskSubjs = Object.entries(student.grades).filter(([, g]) => g.risk);
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 18
    }
  }, /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement("div", {
    style: {
      padding: "20px 24px",
      display: "flex",
      gap: 20,
      alignItems: "center"
    }
  }, /*#__PURE__*/React.createElement(Avatar, {
    name: student.name,
    size: "lg"
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-display)",
      fontWeight: 700,
      fontSize: "var(--text-2xl)",
      color: "var(--text-strong)",
      letterSpacing: "-0.02em"
    }
  }, student.name), /*#__PURE__*/React.createElement("div", {
    style: {
      fontSize: "var(--text-sm)",
      color: "var(--text-subtle)",
      marginTop: 3
    }
  }, student.course, " \xB7 ", student.email)), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      gap: 14,
      alignItems: "center"
    }
  }, /*#__PURE__*/React.createElement(StatCard, {
    label: "Promedio general",
    value: student.avg.toFixed(1),
    icon: /*#__PURE__*/React.createElement(TrendIcon, {
      t: student.trend,
      size: 18
    }),
    iconTone: student.trend === "up" ? "success" : student.trend === "down" ? "danger" : "info"
  }), riskSubjs.length > 0 && /*#__PURE__*/React.createElement(Badge, {
    tone: "danger",
    dot: true
  }, riskSubjs.length, " asig. en riesgo")))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: "220px 1fr",
      gap: 18,
      alignItems: "start"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 8
    }
  }, Object.entries(student.grades).map(([subj, g]) => {
    const active = subj === activeSubject;
    return /*#__PURE__*/React.createElement("button", {
      key: subj,
      onClick: () => setActiveSubject(subj),
      style: {
        border: `2px solid ${active ? "var(--brand)" : "var(--border-subtle)"}`,
        borderRadius: "var(--radius-md)",
        background: active ? "var(--surface-brand-soft)" : "var(--surface-card)",
        padding: "12px 14px",
        cursor: "pointer",
        textAlign: "left",
        transition: "border-color 120ms, background 120ms"
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        marginBottom: 5
      }
    }, /*#__PURE__*/React.createElement("span", {
      style: {
        fontWeight: 600,
        fontSize: "var(--text-sm)",
        color: "var(--text-strong)"
      }
    }, subj), /*#__PURE__*/React.createElement(TrendIcon, {
      t: g.trend
    })), /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        alignItems: "baseline",
        gap: 6
      }
    }, /*#__PURE__*/React.createElement("span", {
      style: {
        fontFamily: "var(--font-display)",
        fontWeight: 700,
        fontSize: "var(--text-2xl)",
        color: scoreColor(g.avg)
      }
    }, g.avg.toFixed(1)), /*#__PURE__*/React.createElement("span", {
      style: {
        fontSize: "var(--text-xs)",
        color: "var(--text-subtle)"
      }
    }, g.evals, " eval.")), /*#__PURE__*/React.createElement("div", {
      style: {
        marginTop: 8
      }
    }, /*#__PURE__*/React.createElement(ProgressBar, {
      value: g.avg / 7 * 100,
      tone: g.avg < 4 ? "danger" : g.avg >= 5.5 ? "success" : "brand"
    })), g.risk && /*#__PURE__*/React.createElement("div", {
      style: {
        marginTop: 7
      }
    }, /*#__PURE__*/React.createElement(Badge, {
      tone: "danger",
      dot: true
    }, "En riesgo")));
  })), /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, null, "Evaluaciones \xB7 ", activeSubject), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      gap: 8
    }
  }, /*#__PURE__*/React.createElement(Badge, {
    tone: "info"
  }, subjGrade.evals, " realizadas"), /*#__PURE__*/React.createElement(Badge, {
    tone: subjGrade.avg < 4 ? "danger" : subjGrade.avg >= 5.5 ? "success" : "warning"
  }, "Prom. ", subjGrade.avg.toFixed(1)))), /*#__PURE__*/React.createElement("div", null, evals.length > 0 ? evals.map((ev, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    style: {
      display: "flex",
      alignItems: "center",
      gap: 14,
      padding: "12px 20px",
      borderBottom: i < evals.length - 1 ? "1px solid var(--border-subtle)" : "none"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontWeight: 600,
      fontSize: "var(--text-sm)",
      color: "var(--text-strong)"
    }
  }, ev.title), /*#__PURE__*/React.createElement("div", {
    style: {
      fontSize: "var(--text-xs)",
      color: "var(--text-subtle)",
      marginTop: 2
    }
  }, ev.date)), /*#__PURE__*/React.createElement(RubricLevel, {
    level: ev.rubric
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-display)",
      fontWeight: 700,
      fontSize: "var(--text-xl)",
      color: scoreColor(ev.score),
      minWidth: 36,
      textAlign: "right"
    }
  }, ev.score.toFixed(1)))) : /*#__PURE__*/React.createElement("p", {
    style: {
      padding: "20px",
      color: "var(--text-subtle)",
      fontSize: "var(--text-sm)",
      margin: 0
    }
  }, "Sin evaluaciones registradas.")))));
}
window.Breadcrumb = Breadcrumb;
window.StudentDetail = StudentDetail;
window.scoreColor = scoreColor;
window.TrendIcon = TrendIcon;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/teacher/StudentDetail.jsx", error: String((e && e.message) || e) }); }

// ui_kits/teacher/StudentsScreen.jsx
try { (() => {
// StudentsScreen.jsx — drill-down: Global → Curso → Asignatura → Detalle alumno
const {
  Avatar,
  Badge,
  Button,
  Card,
  Select,
  ProgressBar,
  StatCard
} = window.GradeOpsAIDesignSystem_fcd12b;
const {
  students,
  SUBJECTS_BY_COURSE,
  COURSES,
  subjectStats,
  _avg,
  _r1
} = window.TEACHER_DATA;
const HDR = {
  fontSize: "var(--text-xs)",
  fontWeight: 700,
  color: "var(--text-subtle)",
  textTransform: "uppercase",
  letterSpacing: ".05em"
};

// ── Global view ───────────────────────────────────────────────────────────────
function StuGlobalView({
  onCourse,
  onStudent
}) {
  const [filterCourse, setFilterCourse] = React.useState("all");
  const list = filterCourse === "all" ? students : students.filter(s => s.course === filterCourse);
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 18
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: "repeat(4,1fr)",
      gap: 14
    }
  }, /*#__PURE__*/React.createElement(StatCard, {
    label: "Total estudiantes",
    value: students.length,
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "users",
      size: 19
    }),
    iconTone: "info"
  }), /*#__PURE__*/React.createElement(StatCard, {
    label: "Cursos activos",
    value: COURSES.length,
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "book-open",
      size: 19
    })
  }), /*#__PURE__*/React.createElement(StatCard, {
    label: "Promedio general",
    value: _r1(_avg(students.map(s => s.avg))).toFixed(1),
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "trending-up",
      size: 19
    }),
    iconTone: "success"
  }), /*#__PURE__*/React.createElement(StatCard, {
    label: "En riesgo",
    value: students.filter(s => s.risk).length,
    unit: "alumnos",
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "alert-triangle",
      size: 19
    }),
    iconTone: "danger"
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: "repeat(4,1fr)",
      gap: 14
    }
  }, COURSES.map(c => {
    const cs = students.filter(s => s.course === c);
    const ca = cs.length ? _r1(_avg(cs.map(s => s.avg))) : 0;
    const cr = cs.filter(s => s.risk).length;
    return /*#__PURE__*/React.createElement("button", {
      key: c,
      onClick: () => onCourse(c),
      style: {
        border: "1px solid var(--border-subtle)",
        borderRadius: "var(--radius-md)",
        background: "var(--surface-card)",
        padding: "16px 18px",
        cursor: "pointer",
        textAlign: "left",
        transition: "box-shadow 140ms, border-color 140ms"
      },
      onMouseEnter: e => {
        e.currentTarget.style.borderColor = "var(--brand)";
        e.currentTarget.style.boxShadow = "var(--shadow-md)";
      },
      onMouseLeave: e => {
        e.currentTarget.style.borderColor = "var(--border-subtle)";
        e.currentTarget.style.boxShadow = "none";
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "flex-start"
      }
    }, /*#__PURE__*/React.createElement("span", {
      style: {
        fontFamily: "var(--font-display)",
        fontWeight: 700,
        fontSize: "var(--text-xl)",
        color: "var(--text-strong)"
      }
    }, c), cr > 0 && /*#__PURE__*/React.createElement(Badge, {
      tone: "danger",
      dot: true
    }, cr)), /*#__PURE__*/React.createElement("div", {
      style: {
        fontFamily: "var(--font-display)",
        fontWeight: 700,
        fontSize: "var(--text-3xl)",
        color: scoreColor(ca),
        margin: "10px 0 6px"
      }
    }, ca.toFixed(1)), /*#__PURE__*/React.createElement("div", {
      style: {
        fontSize: "var(--text-xs)",
        color: "var(--text-subtle)",
        marginBottom: 10
      }
    }, cs.length, " estudiantes \xB7 ", (SUBJECTS_BY_COURSE[c] || []).length, " asignaturas"), /*#__PURE__*/React.createElement(ProgressBar, {
      value: ca / 7 * 100,
      tone: ca < 4 ? "danger" : ca >= 5.5 ? "success" : "brand"
    }), /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        gap: 6,
        marginTop: 10,
        flexWrap: "wrap"
      }
    }, (SUBJECTS_BY_COURSE[c] || []).map(sub => /*#__PURE__*/React.createElement("span", {
      key: sub,
      style: {
        fontSize: "var(--text-xs)",
        background: "var(--surface-sunken)",
        border: "1px solid var(--border-subtle)",
        borderRadius: "var(--radius-sm)",
        padding: "2px 7px",
        color: "var(--text-muted)"
      }
    }, sub))));
  })), /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, null, "Todos los estudiantes"), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      gap: 10,
      alignItems: "center"
    }
  }, list.filter(s => s.risk).length > 0 && /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: "var(--text-sm)",
      color: "var(--danger-600)",
      fontWeight: 600
    }
  }, list.filter(s => s.risk).length, " en riesgo"), /*#__PURE__*/React.createElement(Select, {
    style: {
      width: 200
    },
    value: filterCourse,
    onChange: e => setFilterCourse(e.target.value)
  }, /*#__PURE__*/React.createElement("option", {
    value: "all"
  }, "Todos los cursos (", students.length, ")"), COURSES.map(c => /*#__PURE__*/React.createElement("option", {
    key: c,
    value: c
  }, c, " (", students.filter(s => s.course === c).length, " alumnos)"))))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: "1fr 80px 110px 70px 90px",
      gap: 14,
      padding: "10px 20px",
      borderBottom: "1px solid var(--border-subtle)",
      background: "var(--surface-sunken)"
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: HDR
  }, "Estudiante"), /*#__PURE__*/React.createElement("span", {
    style: {
      ...HDR,
      textAlign: "center"
    }
  }, "Promedio"), /*#__PURE__*/React.createElement("span", {
    style: HDR
  }, "Evaluaciones"), /*#__PURE__*/React.createElement("span", {
    style: {
      ...HDR,
      textAlign: "center"
    }
  }, "Tendencia"), /*#__PURE__*/React.createElement("span", {
    style: HDR
  })), list.map((s, i) => {
    const tot = Object.values(s.grades).reduce((n, g) => n + g.evals, 0);
    return /*#__PURE__*/React.createElement("div", {
      key: s.id,
      onClick: () => onStudent(s),
      style: {
        display: "grid",
        gridTemplateColumns: "1fr 80px 110px 70px 90px",
        gap: 14,
        alignItems: "center",
        padding: "12px 20px",
        borderBottom: i < list.length - 1 ? "1px solid var(--border-subtle)" : "none",
        cursor: "pointer",
        transition: "background 100ms"
      },
      onMouseEnter: e => e.currentTarget.style.background = "var(--surface-sunken)",
      onMouseLeave: e => e.currentTarget.style.background = "transparent"
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        alignItems: "center",
        gap: 12
      }
    }, /*#__PURE__*/React.createElement(Avatar, {
      name: s.name,
      size: "sm"
    }), /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("div", {
      style: {
        fontWeight: 600,
        fontSize: "var(--text-md)",
        color: "var(--text-strong)"
      }
    }, s.name), /*#__PURE__*/React.createElement("div", {
      style: {
        fontSize: "var(--text-xs)",
        color: "var(--text-subtle)"
      }
    }, s.course, " \xB7 ", s.email))), /*#__PURE__*/React.createElement("div", {
      style: {
        textAlign: "center",
        fontFamily: "var(--font-display)",
        fontWeight: 700,
        fontSize: "var(--text-xl)",
        color: scoreColor(s.avg)
      }
    }, s.avg.toFixed(1)), /*#__PURE__*/React.createElement("div", {
      style: {
        fontSize: "var(--text-sm)",
        color: "var(--text-muted)"
      }
    }, tot, " realizadas"), /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        justifyContent: "center"
      }
    }, /*#__PURE__*/React.createElement(TrendIcon, {
      t: s.trend
    })), /*#__PURE__*/React.createElement("div", null, s.risk ? /*#__PURE__*/React.createElement(Badge, {
      tone: "danger",
      dot: true
    }, "En riesgo") : /*#__PURE__*/React.createElement("span", {
      style: {
        fontSize: "var(--text-xs)",
        color: "var(--text-subtle)"
      }
    }, "\u2014")));
  })));
}

// ── Course view ───────────────────────────────────────────────────────────────
function StuCourseView({
  course,
  onSubject,
  onStudent
}) {
  const cs = students.filter(s => s.course === course);
  const subjects = SUBJECTS_BY_COURSE[course] || [];
  const ca = cs.length ? _r1(_avg(cs.map(s => s.avg))) : 0;
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 18
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: "repeat(4,1fr)",
      gap: 14
    }
  }, /*#__PURE__*/React.createElement(StatCard, {
    label: "Estudiantes",
    value: cs.length,
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "users",
      size: 19
    }),
    iconTone: "info"
  }), /*#__PURE__*/React.createElement(StatCard, {
    label: "Promedio curso",
    value: ca.toFixed(1),
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "bar-chart-3",
      size: 19
    }),
    iconTone: "success"
  }), /*#__PURE__*/React.createElement(StatCard, {
    label: "Asignaturas",
    value: subjects.length,
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "book-open",
      size: 19
    })
  }), /*#__PURE__*/React.createElement(StatCard, {
    label: "En riesgo",
    value: cs.filter(s => s.risk).length,
    unit: "alumnos",
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "alert-triangle",
      size: 19
    }),
    iconTone: "danger"
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: `repeat(${subjects.length},1fr)`,
      gap: 14
    }
  }, subjects.map(sub => {
    const st = subjectStats(course, sub);
    if (!st) return null;
    return /*#__PURE__*/React.createElement("button", {
      key: sub,
      onClick: () => onSubject(sub),
      style: {
        border: "1px solid var(--border-subtle)",
        borderRadius: "var(--radius-md)",
        background: "var(--surface-card)",
        padding: "16px 18px",
        cursor: "pointer",
        textAlign: "left",
        transition: "box-shadow 140ms, border-color 140ms"
      },
      onMouseEnter: e => {
        e.currentTarget.style.borderColor = "var(--brand)";
        e.currentTarget.style.boxShadow = "var(--shadow-md)";
      },
      onMouseLeave: e => {
        e.currentTarget.style.borderColor = "var(--border-subtle)";
        e.currentTarget.style.boxShadow = "none";
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "flex-start"
      }
    }, /*#__PURE__*/React.createElement("span", {
      style: {
        fontWeight: 700,
        fontSize: "var(--text-md)",
        color: "var(--text-strong)"
      }
    }, sub), st.riskCount > 0 && /*#__PURE__*/React.createElement(Badge, {
      tone: "danger",
      dot: true
    }, st.riskCount)), /*#__PURE__*/React.createElement("div", {
      style: {
        fontFamily: "var(--font-display)",
        fontWeight: 700,
        fontSize: "var(--text-3xl)",
        color: scoreColor(st.avg),
        margin: "10px 0 6px"
      }
    }, st.avg.toFixed(1)), /*#__PURE__*/React.createElement("div", {
      style: {
        fontSize: "var(--text-xs)",
        color: "var(--text-subtle)",
        marginBottom: 10
      }
    }, st.count, " alumnos \xB7 ", st.totalEvals, " eval."), /*#__PURE__*/React.createElement(ProgressBar, {
      value: st.avg / 7 * 100,
      tone: st.avg < 4 ? "danger" : st.avg >= 5.5 ? "success" : "brand"
    }));
  })), /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, null, "Estudiantes \xB7 ", course)), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: `1fr ${subjects.map(() => "80px").join(" ")} 70px 90px`,
      gap: 12,
      padding: "10px 20px",
      borderBottom: "1px solid var(--border-subtle)",
      background: "var(--surface-sunken)"
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: HDR
  }, "Estudiante"), subjects.map(s => /*#__PURE__*/React.createElement("span", {
    key: s,
    style: {
      ...HDR,
      textAlign: "center"
    }
  }, s)), /*#__PURE__*/React.createElement("span", {
    style: {
      ...HDR,
      textAlign: "center"
    }
  }, "Tendencia"), /*#__PURE__*/React.createElement("span", {
    style: HDR
  })), cs.map((s, i) => /*#__PURE__*/React.createElement("div", {
    key: s.id,
    onClick: () => onStudent(s),
    style: {
      display: "grid",
      gridTemplateColumns: `1fr ${subjects.map(() => "80px").join(" ")} 70px 90px`,
      gap: 12,
      alignItems: "center",
      padding: "12px 20px",
      borderBottom: i < cs.length - 1 ? "1px solid var(--border-subtle)" : "none",
      cursor: "pointer",
      transition: "background 100ms"
    },
    onMouseEnter: e => e.currentTarget.style.background = "var(--surface-sunken)",
    onMouseLeave: e => e.currentTarget.style.background = "transparent"
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 10
    }
  }, /*#__PURE__*/React.createElement(Avatar, {
    name: s.name,
    size: "sm"
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      fontWeight: 600,
      fontSize: "var(--text-sm)",
      color: "var(--text-strong)"
    }
  }, s.name)), subjects.map(sub => {
    const g = s.grades[sub];
    return g ? /*#__PURE__*/React.createElement("div", {
      key: sub,
      style: {
        textAlign: "center",
        fontFamily: "var(--font-display)",
        fontWeight: 700,
        fontSize: "var(--text-lg)",
        color: scoreColor(g.avg)
      }
    }, g.avg.toFixed(1)) : /*#__PURE__*/React.createElement("div", {
      key: sub,
      style: {
        textAlign: "center",
        color: "var(--text-subtle)"
      }
    }, "\u2014");
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      justifyContent: "center"
    }
  }, /*#__PURE__*/React.createElement(TrendIcon, {
    t: s.trend
  })), /*#__PURE__*/React.createElement("div", null, s.risk ? /*#__PURE__*/React.createElement(Badge, {
    tone: "danger",
    dot: true
  }, "En riesgo") : /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: "var(--text-xs)",
      color: "var(--text-subtle)"
    }
  }, "\u2014"))))));
}

// ── Subject view ──────────────────────────────────────────────────────────────
function StuSubjectView({
  course,
  subject,
  onStudent
}) {
  const st = subjectStats(course, subject);
  if (!st) return null;
  const sorted = [...st.students].sort((a, b) => b.grades[subject].avg - a.grades[subject].avg);
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 18
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: "repeat(4,1fr)",
      gap: 14
    }
  }, /*#__PURE__*/React.createElement(StatCard, {
    label: "Promedio",
    value: st.avg.toFixed(1),
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "bar-chart-3",
      size: 19
    }),
    iconTone: "success"
  }), /*#__PURE__*/React.createElement(StatCard, {
    label: "Nota m\xE1s alta",
    value: st.max.toFixed(1),
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "trending-up",
      size: 19
    }),
    iconTone: "info"
  }), /*#__PURE__*/React.createElement(StatCard, {
    label: "Nota m\xE1s baja",
    value: st.min.toFixed(1),
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "trending-down",
      size: 19
    }),
    iconTone: "danger"
  }), /*#__PURE__*/React.createElement(StatCard, {
    label: "En riesgo",
    value: st.riskCount,
    unit: "alumnos",
    icon: /*#__PURE__*/React.createElement(Icon, {
      name: "alert-triangle",
      size: 19
    }),
    iconTone: "danger"
  })), /*#__PURE__*/React.createElement(Card, null, /*#__PURE__*/React.createElement(Card.Header, null, /*#__PURE__*/React.createElement(Card.Title, null, "Rendimiento \xB7 ", course, " ", subject), /*#__PURE__*/React.createElement(Badge, {
    tone: "info"
  }, st.totalEvals, " evaluaciones")), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: "40px 1fr 120px 70px 80px 90px",
      gap: 12,
      padding: "10px 20px",
      borderBottom: "1px solid var(--border-subtle)",
      background: "var(--surface-sunken)"
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: HDR
  }, "#"), /*#__PURE__*/React.createElement("span", {
    style: HDR
  }, "Estudiante"), /*#__PURE__*/React.createElement("span", {
    style: HDR
  }, "Progreso"), /*#__PURE__*/React.createElement("span", {
    style: {
      ...HDR,
      textAlign: "center"
    }
  }, "Nota"), /*#__PURE__*/React.createElement("span", {
    style: {
      ...HDR,
      textAlign: "center"
    }
  }, "Tendencia"), /*#__PURE__*/React.createElement("span", {
    style: HDR
  })), sorted.map((s, i) => {
    const g = s.grades[subject];
    return /*#__PURE__*/React.createElement("div", {
      key: s.id,
      onClick: () => onStudent(s),
      style: {
        display: "grid",
        gridTemplateColumns: "40px 1fr 120px 70px 80px 90px",
        gap: 12,
        alignItems: "center",
        padding: "12px 20px",
        borderBottom: i < sorted.length - 1 ? "1px solid var(--border-subtle)" : "none",
        cursor: "pointer",
        transition: "background 100ms"
      },
      onMouseEnter: e => e.currentTarget.style.background = "var(--surface-sunken)",
      onMouseLeave: e => e.currentTarget.style.background = "transparent"
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        fontFamily: "var(--font-display)",
        fontWeight: 700,
        fontSize: "var(--text-lg)",
        color: "var(--text-subtle)",
        textAlign: "center"
      }
    }, i + 1), /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        alignItems: "center",
        gap: 10
      }
    }, /*#__PURE__*/React.createElement(Avatar, {
      name: s.name,
      size: "sm"
    }), /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("div", {
      style: {
        fontWeight: 600,
        fontSize: "var(--text-sm)",
        color: "var(--text-strong)"
      }
    }, s.name), /*#__PURE__*/React.createElement("div", {
      style: {
        fontSize: "var(--text-xs)",
        color: "var(--text-subtle)"
      }
    }, g.evals, " eval."))), /*#__PURE__*/React.createElement(ProgressBar, {
      value: g.avg / 7 * 100,
      tone: g.avg < 4 ? "danger" : g.avg >= 5.5 ? "success" : "brand"
    }), /*#__PURE__*/React.createElement("div", {
      style: {
        textAlign: "center",
        fontFamily: "var(--font-display)",
        fontWeight: 700,
        fontSize: "var(--text-xl)",
        color: scoreColor(g.avg)
      }
    }, g.avg.toFixed(1)), /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        justifyContent: "center"
      }
    }, /*#__PURE__*/React.createElement(TrendIcon, {
      t: g.trend
    })), /*#__PURE__*/React.createElement("div", null, g.risk ? /*#__PURE__*/React.createElement(Badge, {
      tone: "danger",
      dot: true
    }, "En riesgo") : /*#__PURE__*/React.createElement("span", {
      style: {
        fontSize: "var(--text-xs)",
        color: "var(--text-subtle)"
      }
    }, "\u2014")));
  })));
}

// ── Orchestrator ──────────────────────────────────────────────────────────────
function StudentsScreen() {
  const [view, setView] = React.useState({
    level: "global"
  });
  // levels: "global" | "course" | "subject" | "student"

  const crumbs = [{
    label: "Estudiantes"
  }];
  if (view.course) crumbs.push({
    label: view.course
  });
  if (view.subject) crumbs.push({
    label: view.subject
  });
  if (view.student) crumbs.push({
    label: view.student.name
  });
  const goTo = idx => {
    if (idx === 0) setView({
      level: "global"
    });
    if (idx === 1) setView({
      level: "course",
      course: view.course
    });
    if (idx === 2) setView({
      level: "subject",
      course: view.course,
      subject: view.subject
    });
  };
  return /*#__PURE__*/React.createElement("div", {
    style: {
      maxWidth: "var(--content-max)",
      display: "flex",
      flexDirection: "column",
      gap: 0
    }
  }, crumbs.length > 1 && /*#__PURE__*/React.createElement(Breadcrumb, {
    items: crumbs,
    onNavigate: goTo
  }), view.level === "global" && /*#__PURE__*/React.createElement(StuGlobalView, {
    onCourse: c => setView({
      level: "course",
      course: c
    }),
    onStudent: s => setView({
      level: "student",
      student: s
    })
  }), view.level === "course" && /*#__PURE__*/React.createElement(StuCourseView, {
    course: view.course,
    onSubject: sub => setView({
      level: "subject",
      course: view.course,
      subject: sub
    }),
    onStudent: s => setView({
      level: "student",
      student: s,
      course: view.course
    })
  }), view.level === "subject" && /*#__PURE__*/React.createElement(StuSubjectView, {
    course: view.course,
    subject: view.subject,
    onStudent: s => setView({
      level: "student",
      student: s,
      course: view.course,
      subject: view.subject
    })
  }), view.level === "student" && /*#__PURE__*/React.createElement(StudentDetail, {
    student: view.student
  }));
}
window.StudentsScreen = StudentsScreen;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/teacher/StudentsScreen.jsx", error: String((e && e.message) || e) }); }

// ui_kits/teacher/TeacherApp.jsx
try { (() => {
// Teacher portal orchestrator: login → shell with routed screens + toasts.
const {
  Button,
  ToastViewport,
  ConfirmDialog
} = window.GradeOpsAIDesignSystem_fcd12b;
function TeacherApp() {
  const [authed, setAuthed] = React.useState(false);
  const [route, setRoute] = React.useState("dashboard");
  const [toasts, setToasts] = React.useState([]);
  const [confirmPublish, setConfirmPublish] = React.useState(false);
  const [publishing, setPublishing] = React.useState(false);
  const idRef = React.useRef(0);
  const push = t => {
    const id = ++idRef.current;
    setToasts(x => [...x, {
      id,
      ...t
    }]);
    if (!t.loading) setTimeout(() => dismiss(id), 3200);
    return id;
  };
  const dismiss = id => setToasts(x => x.filter(t => t.id !== id));
  const replace = (id, t) => setToasts(x => x.map(o => o.id === id ? {
    id,
    ...t
  } : o));
  const saveAssessment = () => {
    const id = push({
      loading: true,
      title: "Guardando evaluación…"
    });
    setTimeout(() => {
      replace(id, {
        tone: "success",
        title: "Evaluación guardada",
        message: "Prueba Unidad 3 — Fotosíntesis"
      });
      setTimeout(() => dismiss(id), 3000);
      setRoute("dashboard");
    }, 1300);
  };
  const doPublish = () => {
    setPublishing(true);
    setTimeout(() => {
      setPublishing(false);
      setConfirmPublish(false);
      const id = push({
        tone: "success",
        title: "Resultados publicados",
        message: "Se enviaron 28 enlaces mágicos a los estudiantes."
      });
      setTimeout(() => dismiss(id), 3400);
    }, 1500);
  };
  const TITLES = {
    dashboard: ["Panel de control", "Resumen de tus cursos y evaluaciones"],
    assessments: ["Evaluaciones", "Gestiona y revisa todas tus evaluaciones"],
    builder: ["Nueva evaluación", "Configura tipo, preguntas y fechas"],
    bank: ["Banco de preguntas", "Tus preguntas personales y el banco global"],
    students: ["Estudiantes", "Gestiona cursos y progreso académico"],
    reports: ["Reportes", "Rendimiento por grupo y evaluación"],
    grading: ["Corrección con IA", "Ensayo: Fotosíntesis · 2°A Biología"]
  };
  const [t, st] = TITLES[route] || TITLES.dashboard;
  if (!authed) return /*#__PURE__*/React.createElement(LoginScreen, {
    onLogin: () => {
      setAuthed(true);
      push({
        tone: "success",
        title: "¡Hola, Paula!",
        message: "Sesión iniciada correctamente."
      });
    }
  });
  const headerAction = route === "dashboard" || route === "assessments" ? /*#__PURE__*/React.createElement(Button, {
    iconLeft: /*#__PURE__*/React.createElement(Icon, {
      name: "plus",
      size: 17
    }),
    onClick: () => setRoute("builder")
  }, "Nueva evaluaci\xF3n") : route === "grading" || route === "builder" ? /*#__PURE__*/React.createElement(Button, {
    variant: "secondary",
    iconLeft: /*#__PURE__*/React.createElement(Icon, {
      name: "arrow-left",
      size: 16
    }),
    onClick: () => setRoute(route === "builder" ? "assessments" : "dashboard")
  }, "Volver") : route === "bank" ? /*#__PURE__*/React.createElement(Button, {
    variant: "secondary",
    iconLeft: /*#__PURE__*/React.createElement(Icon, {
      name: "plus",
      size: 16
    }),
    onClick: () => {}
  }, "Nueva pregunta") : route === "students" ? /*#__PURE__*/React.createElement(Button, {
    variant: "secondary",
    iconLeft: /*#__PURE__*/React.createElement(Icon, {
      name: "user-plus",
      size: 16
    }),
    onClick: () => {}
  }, "A\xF1adir estudiante") : null;
  return /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement(Shell, {
    active: route === "grading" || route === "builder" ? "assessments" : route,
    onNavigate: setRoute,
    title: t,
    subtitle: st,
    actions: headerAction
  }, route === "dashboard" && /*#__PURE__*/React.createElement(DashboardScreen, {
    onOpenAssessment: () => setRoute("grading"),
    onGoBank: () => setRoute("bank")
  }), route === "assessments" && /*#__PURE__*/React.createElement(AssessmentsScreen, {
    onGrade: () => setRoute("grading")
  }), route === "builder" && /*#__PURE__*/React.createElement(BuilderScreen, {
    onCancel: () => setRoute("assessments"),
    onSave: saveAssessment
  }), route === "bank" && /*#__PURE__*/React.createElement(BankScreen, null), route === "grading" && /*#__PURE__*/React.createElement(GradingScreen, {
    onPublish: () => setConfirmPublish(true)
  }), route === "students" && /*#__PURE__*/React.createElement(StudentsScreen, null), route === "reports" && /*#__PURE__*/React.createElement(ReportsScreen, null)), /*#__PURE__*/React.createElement(ConfirmDialog, {
    open: confirmPublish,
    tone: "brand",
    title: "\xBFPublicar resultados?",
    message: "Se enviar\xE1 un enlace m\xE1gico de resultados a 28 estudiantes. Podr\xE1n ver su nota, la r\xFAbrica y la retroalimentaci\xF3n.",
    confirmLabel: "Publicar y enviar",
    loading: publishing,
    onCancel: () => setConfirmPublish(false),
    onConfirm: doPublish
  }), /*#__PURE__*/React.createElement(ToastViewport, {
    toasts: toasts,
    onDismiss: dismiss
  }));
}
function Placeholder({
  name
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      justifyContent: "center",
      gap: 12,
      padding: 80,
      color: "var(--text-subtle)"
    }
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "hammer",
    size: 36,
    color: "var(--slate-400)"
  }), /*#__PURE__*/React.createElement("p", {
    style: {
      margin: 0,
      fontFamily: "var(--font-display)",
      fontWeight: 600,
      fontSize: "var(--text-lg)",
      color: "var(--text-muted)"
    }
  }, name), /*#__PURE__*/React.createElement("p", {
    style: {
      margin: 0,
      fontSize: "var(--text-sm)"
    }
  }, "Vista no incluida en este UI kit."));
}
ReactDOM.createRoot(document.getElementById("root")).render(/*#__PURE__*/React.createElement(TeacherApp, null));
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/teacher/TeacherApp.jsx", error: String((e && e.message) || e) }); }

// ui_kits/teacher/icons.jsx
try { (() => {
// Lucide → React icon helper for GradeOps UI kits.
// Builds the SVG from lucide's icon-node data (idempotent across re-renders).
function Icon({
  name,
  size = 20,
  strokeWidth = 2,
  color = "currentColor",
  style,
  className
}) {
  const pascal = String(name).split("-").map(s => s[0].toUpperCase() + s.slice(1)).join("");
  const node = window.lucide && window.lucide.icons && window.lucide.icons[pascal] || [];
  const children = node.map(([tag, attrs], i) => React.createElement(tag, {
    key: i,
    ...attrs
  }));
  return React.createElement("svg", {
    className,
    width: size,
    height: size,
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: color,
    strokeWidth,
    strokeLinecap: "round",
    strokeLinejoin: "round",
    style: {
      flex: "none",
      display: "block",
      ...style
    }
  }, children);
}
window.Icon = Icon;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/teacher/icons.jsx", error: String((e && e.message) || e) }); }

// ui_kits/teacher/teacherData.js
try { (() => {
// teacherData.js — shared mock data: students, courses, subjects, evaluations
window.TEACHER_DATA = (() => {
  const SUBJECTS_BY_COURSE = {
    "1°B": ["Biología", "Historia", "Matemática"],
    "2°A": ["Historia", "Lenguaje", "Matemática"],
    "3°A": ["Lenguaje", "Matemática", "Filosofía"],
    "2°B": ["Matemática", "Historia", "Ciencias"]
  };
  const COURSES = Object.keys(SUBJECTS_BY_COURSE);
  const EVAL_NAMES = {
    "Biología": ["Control U1", "Prueba Fotosíntesis", "Trabajo Ecosistemas", "Control U2", "Prueba U2", "Control U3", "Trabajo Evolución", "Prueba U3"],
    "Historia": ["Control U1", "Ensayo Renacimiento", "Prueba U1", "Control U2", "Ensayo R. Industrial", "Prueba U2"],
    "Matemática": ["Control U1", "Prueba Funciones", "Control U2", "Prueba Derivadas", "Control U3", "Prueba Integrales", "Control U4"],
    "Lenguaje": ["Control U1", "Ensayo Literario", "Prueba Novela", "Control U2", "Análisis Poético"],
    "Filosofía": ["Control U1", "Ensayo Ética", "Prueba U1", "Control U2"],
    "Ciencias": ["Control U1", "Prueba Química", "Trabajo Lab", "Control U2", "Prueba Física"]
  };
  const DATES = ["Mar 5", "Mar 22", "Abr 8", "Abr 25", "May 10", "May 28", "Jun 5", "Jun 18"];
  const _avg = a => a.reduce((s, x) => s + x, 0) / a.length;
  const _r1 = n => Math.round(n * 10) / 10;
  const _trend = a => {
    if (a.length < 2) return "flat";
    const h = Math.floor(a.length / 2),
      d = _avg(a.slice(h)) - _avg(a.slice(0, h));
    return d > 0.3 ? "up" : d < -0.3 ? "down" : "flat";
  };
  const _rubric = s => s >= 6.0 ? "advanced" : s >= 5.0 ? "proficient" : s >= 4.0 ? "developing" : "beginning";
  const RAW = [{
    id: "s01",
    name: "Antonia Bravo",
    course: "1°B",
    email: "a.bravo@school.cl",
    grades: {
      "Biología": [6.2, 6.8, 6.4, 7.0, 6.5, 6.8, 6.3, 6.7],
      "Historia": [5.5, 6.0, 6.0, 5.8, 5.5],
      "Matemática": [5.0, 5.5, 5.2, 5.8, 5.5]
    }
  }, {
    id: "s02",
    name: "Diego Soto",
    course: "1°B",
    email: "d.soto@school.cl",
    grades: {
      "Biología": [5.0, 5.2, 5.1, 4.8, 5.5, 5.0, 5.2, 5.1],
      "Historia": [4.0, 4.8, 4.5, 4.5, 4.8],
      "Matemática": [4.0, 4.5, 4.2, 4.0, 4.2]
    }
  }, {
    id: "s03",
    name: "Josefa Díaz",
    course: "1°B",
    email: "j.diaz@school.cl",
    grades: {
      "Biología": [3.8, 4.2, 4.1, 4.0, 4.2, 4.0, 3.9, 4.1],
      "Historia": [3.2, 3.8, 3.5, 3.4, 3.5],
      "Matemática": [3.5, 4.0, 3.8, 3.8, 3.8]
    }
  }, {
    id: "s04",
    name: "Camila Rojas",
    course: "2°A",
    email: "c.rojas@school.cl",
    grades: {
      "Historia": [6.5, 6.8, 6.5, 6.7, 6.5, 6.5],
      "Lenguaje": [6.0, 6.5, 6.2, 6.0, 6.5],
      "Matemática": [5.5, 6.0, 5.8, 5.8, 6.0]
    }
  }, {
    id: "s05",
    name: "Martín Cáceres",
    course: "2°A",
    email: "m.caceres@school.cl",
    grades: {
      "Historia": [5.5, 6.0, 5.8, 6.0, 5.8, 5.5],
      "Lenguaje": [5.0, 5.5, 5.2, 5.2, 5.0],
      "Matemática": [5.2, 5.8, 5.5, 5.5, 5.5]
    }
  }, {
    id: "s06",
    name: "Isidora Guzmán",
    course: "2°A",
    email: "i.guzman@school.cl",
    grades: {
      "Historia": [3.5, 4.0, 3.8, 3.8, 3.5, 4.0],
      "Lenguaje": [4.0, 4.5, 4.2, 4.0, 4.2],
      "Matemática": [3.2, 3.8, 3.5, 3.5, 3.2]
    }
  }, {
    id: "s07",
    name: "Benjamín Flores",
    course: "2°A",
    email: "b.flores@school.cl",
    grades: {
      "Historia": [5.0, 5.5, 5.2, 5.2, 5.0, 5.2],
      "Lenguaje": [4.5, 5.0, 4.8, 5.0, 4.8],
      "Matemática": [4.8, 5.2, 5.0, 5.0, 5.0]
    }
  }, {
    id: "s08",
    name: "Valentina Mora",
    course: "3°A",
    email: "v.mora@school.cl",
    grades: {
      "Lenguaje": [6.0, 6.5, 6.0, 6.0, 6.0],
      "Matemática": [5.5, 6.0, 5.8, 5.8, 6.0],
      "Filosofía": [5.2, 5.8, 5.5, 5.5]
    }
  }, {
    id: "s09",
    name: "Tomás Vidal",
    course: "3°A",
    email: "t.vidal@school.cl",
    grades: {
      "Lenguaje": [3.0, 3.5, 3.2, 3.0, 3.5],
      "Matemática": [3.5, 4.0, 3.8, 3.8, 3.5],
      "Filosofía": [4.0, 4.5, 4.2, 4.2]
    }
  }, {
    id: "s10",
    name: "Sofía Herrera",
    course: "3°A",
    email: "s.herrera@school.cl",
    grades: {
      "Lenguaje": [5.5, 5.8, 5.5, 5.5, 6.0],
      "Matemática": [4.8, 5.2, 5.0, 5.0, 5.2],
      "Filosofía": [5.5, 5.8, 5.8, 5.5]
    }
  }, {
    id: "s11",
    name: "Gabriel Torres",
    course: "2°B",
    email: "g.torres@school.cl",
    grades: {
      "Matemática": [4.0, 4.5, 3.8, 4.2, 4.0, 4.2, 3.9],
      "Historia": [5.0, 5.5, 5.2, 5.0, 5.2],
      "Ciencias": [5.2, 5.8, 5.5, 5.5, 5.0]
    }
  }, {
    id: "s12",
    name: "Daniela Parra",
    course: "2°B",
    email: "d.parra@school.cl",
    grades: {
      "Matemática": [3.5, 3.8, 3.2, 3.5, 3.8, 3.5, 3.2],
      "Historia": [4.8, 5.0, 4.8, 5.0, 4.5],
      "Ciencias": [4.5, 5.0, 4.8, 4.5, 4.8]
    }
  }, {
    id: "s13",
    name: "Rodrigo Núñez",
    course: "2°B",
    email: "r.nunez@school.cl",
    grades: {
      "Matemática": [5.5, 5.8, 6.0, 5.5, 5.8, 5.5, 6.0],
      "Historia": [5.5, 5.8, 5.5, 5.8, 5.5],
      "Ciencias": [6.0, 6.2, 6.0, 6.0, 5.8]
    }
  }];
  const students = RAW.map(s => {
    const grades = {};
    for (const [sub, scores] of Object.entries(s.grades)) {
      const a = _avg(scores);
      grades[sub] = {
        avg: _r1(a),
        scores,
        evals: scores.length,
        trend: _trend(scores),
        risk: a < 4.0
      };
    }
    const allAvgs = Object.values(grades).map(g => g.avg);
    return {
      ...s,
      grades,
      avg: _r1(_avg(allAvgs)),
      trend: _trend(allAvgs),
      risk: Object.values(grades).some(g => g.risk)
    };
  });

  // Returns individual evaluation rows for a student+subject
  const getEvals = (studentId, subject) => {
    const s = students.find(x => x.id === studentId);
    if (!s || !s.grades[subject]) return [];
    return s.grades[subject].scores.map((score, i) => ({
      title: (EVAL_NAMES[subject] || [])[i] || `Evaluación ${i + 1}`,
      date: DATES[i] || "—",
      score,
      rubric: _rubric(score)
    }));
  };

  // Returns aggregate stats for a course-subject pair
  const subjectStats = (course, subject) => {
    const ss = students.filter(s => s.course === course && s.grades[subject]);
    if (!ss.length) return null;
    const avgs = ss.map(s => s.grades[subject].avg);
    const globalAvg = _r1(_avg(avgs));
    const maxScore = _r1(Math.max(...avgs));
    const minScore = _r1(Math.min(...avgs));
    const riskCount = ss.filter(s => s.grades[subject].risk).length;
    const totalEvals = ss[0].grades[subject].evals;
    return {
      students: ss,
      count: ss.length,
      avg: globalAvg,
      max: maxScore,
      min: minScore,
      riskCount,
      totalEvals
    };
  };
  return {
    students,
    SUBJECTS_BY_COURSE,
    COURSES,
    getEvals,
    subjectStats,
    _avg,
    _r1
  };
})();
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/teacher/teacherData.js", error: String((e && e.message) || e) }); }

__ds_ns.Avatar = __ds_scope.Avatar;

__ds_ns.Badge = __ds_scope.Badge;

__ds_ns.Button = __ds_scope.Button;

__ds_ns.Card = __ds_scope.Card;

__ds_ns.IconButton = __ds_scope.IconButton;

__ds_ns.Tag = __ds_scope.Tag;

__ds_ns.Tooltip = __ds_scope.Tooltip;

__ds_ns.RubricLevel = __ds_scope.RubricLevel;

__ds_ns.StatCard = __ds_scope.StatCard;

__ds_ns.Tabs = __ds_scope.Tabs;

__ds_ns.Dialog = __ds_scope.Dialog;

__ds_ns.ConfirmDialog = __ds_scope.ConfirmDialog;

__ds_ns.ProgressBar = __ds_scope.ProgressBar;

__ds_ns.Spinner = __ds_scope.Spinner;

__ds_ns.LoadingOverlay = __ds_scope.LoadingOverlay;

__ds_ns.Toast = __ds_scope.Toast;

__ds_ns.ToastViewport = __ds_scope.ToastViewport;

__ds_ns.Checkbox = __ds_scope.Checkbox;

__ds_ns.Field = __ds_scope.Field;

__ds_ns.Input = __ds_scope.Input;

__ds_ns.Radio = __ds_scope.Radio;

__ds_ns.Select = __ds_scope.Select;

__ds_ns.Switch = __ds_scope.Switch;

__ds_ns.Textarea = __ds_scope.Textarea;

})();
