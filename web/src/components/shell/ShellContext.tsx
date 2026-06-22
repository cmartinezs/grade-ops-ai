"use client";

import React, { createContext, useContext, useState, useEffect } from "react";

export interface ShellConfig {
  title: string;
  subtitle?: string;
  actions?: React.ReactNode;
}

interface ShellContextValue {
  config: ShellConfig;
  setConfig: (c: ShellConfig) => void;
}

const DEFAULT: ShellConfig = { title: "GradeOps AI" };

const ShellContext = createContext<ShellContextValue>({
  config: DEFAULT,
  setConfig: () => {},
});

export function ShellProvider({ children }: { children: React.ReactNode }) {
  const [config, setConfig] = useState<ShellConfig>(DEFAULT);
  return (
    <ShellContext.Provider value={{ config, setConfig }}>
      {children}
    </ShellContext.Provider>
  );
}

export function useShell(): ShellContextValue {
  return useContext(ShellContext);
}

export function useShellConfig(config: ShellConfig): void {
  const { setConfig } = useContext(ShellContext);
  useEffect(() => {
    setConfig(config);
    // Depend only on title: subtitle/actions are stable within a page render
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [config.title]);
}
