import AuthGuard from "@/components/auth/AuthGuard";
import { ShellProvider } from "@/components/shell/ShellContext";
import AppShell from "@/components/shell/AppShell";

export default function ProtectedLayout({ children }: { children: React.ReactNode }) {
  return (
    <AuthGuard>
      <ShellProvider>
        <AppShell>{children}</AppShell>
      </ShellProvider>
    </AuthGuard>
  );
}
