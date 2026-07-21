import type { Config } from "jest";

const config: Config = {
  testEnvironment: "jsdom",
  // e2e/*.spec.ts use @playwright/test's own test runner and fixtures, not jest's — without
  // this, jest's default testMatch picks them up too and fails on the unfamiliar API.
  testPathIgnorePatterns: ["<rootDir>/node_modules/", "<rootDir>/e2e/"],
  transform: {
    "^.+\\.(ts|tsx)$": ["ts-jest", { tsconfig: { jsx: "react-jsx" } }],
  },
  moduleNameMapper: {
    "^@/(.*)$": "<rootDir>/src/$1",
    "^firebase/(.*)$": "<rootDir>/src/test/__mocks__/firebase/$1",
  },
  setupFilesAfterEnv: ["@testing-library/jest-dom"],
};

export default config;
