"use client";

import { useState, useEffect } from "react";
import { usePathname } from "next/navigation";
import { onAuthStateChanged, User } from "firebase/auth";
import { auth } from "@/lib/firebase/client";
import AppLogo from "@/components/brand/AppLogo";
import { Avatar, IconButton, LucideIcon } from "@/components/ds";
import type { LucideIconName } from "@/components/ds";
import SignOutButton from "@/components/auth/SignOutButton";
import { useShell } from "@/components/shell/ShellContext";

const NAV_ITEMS = [
  { id: "dashboard", label: "Panel", href: "/dashboard", icon: "layout-dashboard" },
  { id: "assessments", label: "Evaluaciones", href: "/assessments", icon: "file-pen-line" },
  { id: "bank", label: "Banco de preguntas", href: "/bank", icon: "library" },
  { id: "students", label: "Estudiantes", href: "/students", icon: "users" },
  { id: "reports", label: "Reportes", href: "/reports", icon: "bar-chart-3" },
] as const satisfies ReadonlyArray<{ id: string; label: string; href: string; icon: LucideIconName }>;

function NavItem({
  item,
  isActive,
}: {
  item: (typeof NAV_ITEMS)[number];
  isActive: boolean;
}) {
  const [hovered, setHovered] = useState(false);

  return (
    <a
      href={item.href}
      style={{
        display: "flex",
        alignItems: "center",
        gap: 10,
        padding: "8px 10px",
        borderRadius: "var(--radius-md)",
        textDecoration: "none",
        fontFamily: "var(--font-sans)",
        fontSize: "var(--text-sm)",
        fontWeight: isActive ? 600 : 400,
        color: isActive ? "var(--brand-hover)" : "var(--text-muted)",
        background: isActive
          ? "var(--surface-brand-soft)"
          : hovered
          ? "var(--surface-sunken)"
          : "transparent",
        transition: "background 120ms, color 120ms",
      }}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
    >
      <LucideIcon
        name={item.icon}
        size={17}
        color={isActive ? "var(--brand)" : "currentColor"}
      />
      {item.label}
    </a>
  );
}

function CreditsWidget() {
  return (
    <div
      style={{
        margin: "16px 0",
        padding: "12px 14px",
        background: "var(--gold-50)",
        border: "1px solid var(--gold-200)",
        borderRadius: "var(--radius-md)",
        display: "flex",
        gap: 10,
        alignItems: "flex-start",
      }}
    >
      <LucideIcon
        name="sparkles"
        size={16}
        color="var(--gold-600)"
        style={{ marginTop: 1, flexShrink: 0 }}
      />
      <div>
        <p
          style={{
            fontFamily: "var(--font-sans)",
            fontSize: "var(--text-xs)",
            fontWeight: 600,
            color: "var(--gold-700, var(--gold-600))",
            margin: 0,
            lineHeight: 1.4,
          }}
        >
          Créditos IA
        </p>
        <p
          style={{
            fontFamily: "var(--font-sans)",
            fontSize: "var(--text-xs)",
            color: "var(--gold-600)",
            margin: 0,
            lineHeight: 1.4,
          }}
        >
          742 correcciones restantes este mes.
        </p>
      </div>
    </div>
  );
}

function UserRow({ user }: { user: User | null }) {
  const displayName = user?.displayName ?? "Usuario";

  return (
    <div
      style={{
        display: "flex",
        alignItems: "center",
        gap: 10,
        padding: "10px 4px",
        borderTop: "1px solid var(--border-subtle)",
        marginTop: 4,
      }}
    >
      <Avatar name={displayName} size="sm" />
      <div style={{ flex: 1, minWidth: 0 }}>
        <p
          style={{
            fontFamily: "var(--font-sans)",
            fontSize: "var(--text-sm)",
            fontWeight: 500,
            color: "var(--text-strong)",
            margin: 0,
            overflow: "hidden",
            textOverflow: "ellipsis",
            whiteSpace: "nowrap",
          }}
        >
          {displayName}
        </p>
        <p
          style={{
            fontFamily: "var(--font-sans)",
            fontSize: "var(--text-xs)",
            color: "var(--text-muted)",
            margin: 0,
          }}
        >
          GradeOps AI
        </p>
      </div>
      <SignOutButton />
    </div>
  );
}

export default function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const { config } = useShell();
  const [user, setUser] = useState<User | null>(auth.currentUser);

  useEffect(() => {
    return onAuthStateChanged(auth, setUser);
  }, []);

  return (
    <div style={{ display: "flex", height: "100vh", overflow: "hidden" }}>
      {/* Sidebar */}
      <aside
        style={{
          width: 256,
          flexShrink: 0,
          display: "flex",
          flexDirection: "column",
          background: "var(--surface-card)",
          borderRight: "1px solid var(--border-subtle)",
          padding: "18px 14px",
        }}
      >
        {/* Brand */}
        <div style={{ paddingBottom: 18 }}>
          <AppLogo size="md" />
        </div>

        {/* Nav */}
        <nav
          style={{
            flex: 1,
            display: "flex",
            flexDirection: "column",
            gap: 2,
          }}
        >
          {NAV_ITEMS.map((item) => (
            <NavItem
              key={item.id}
              item={item}
              isActive={pathname.startsWith(item.href)}
            />
          ))}
        </nav>

        {/* Credits widget */}
        <CreditsWidget />

        {/* User row */}
        <UserRow user={user} />
      </aside>

      {/* Main area */}
      <div
        style={{
          flex: 1,
          minWidth: 0,
          display: "flex",
          flexDirection: "column",
          overflow: "hidden",
        }}
      >
        {/* Topbar */}
        <header
          style={{
            height: 64,
            flexShrink: 0,
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            padding: "0 28px",
            background: "var(--surface-card)",
            borderBottom: "1px solid var(--border-subtle)",
            gap: 16,
          }}
        >
          <div style={{ flex: 1, minWidth: 0 }}>
            <h1
              style={{
                fontFamily: "var(--font-display)",
                fontWeight: 600,
                fontSize: "var(--text-xl)",
                color: "var(--text-strong)",
                lineHeight: 1.2,
                margin: 0,
                overflow: "hidden",
                textOverflow: "ellipsis",
                whiteSpace: "nowrap",
              }}
            >
              {config.title}
            </h1>
            {config.subtitle && (
              <p
                style={{
                  fontSize: "var(--text-sm)",
                  color: "var(--text-muted)",
                  margin: 0,
                  lineHeight: 1.4,
                }}
              >
                {config.subtitle}
              </p>
            )}
          </div>
          <div
            style={{
              display: "flex",
              alignItems: "center",
              gap: 8,
              flexShrink: 0,
            }}
          >
            <IconButton label="Notificaciones">
              <LucideIcon name="bell" size={18} />
            </IconButton>
            {config.actions}
          </div>
        </header>

        {/* Content area */}
        <main
          style={{
            flex: 1,
            minHeight: 0,
            overflow: "auto",
            padding: 28,
            background: "var(--surface-page)",
          }}
        >
          {children}
        </main>
      </div>
    </div>
  );
}
