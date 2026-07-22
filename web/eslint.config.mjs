import { dirname } from "path";
import { fileURLToPath } from "url";
import { FlatCompat } from "@eslint/eslintrc";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const compat = new FlatCompat({
  baseDirectory: __dirname,
});

const eslintConfig = [
  // e2e/ uses Playwright's own `use` fixture parameter (base.extend<T>(...)), which
  // react-hooks/rules-of-hooks false-positives on as React's use() hook — this is Playwright
  // test code, not React. scripts/ is plain Node/bash tooling, not application source either.
  { ignores: ["e2e/**", "scripts/**"] },
  ...compat.extends("next/core-web-vitals", "next/typescript"),
  {
    rules: {
      "@typescript-eslint/no-unused-vars": [
        "warn",
        {
          argsIgnorePattern: "^_",
        },
      ],
    },
  },
];

export default eslintConfig;
