import { ReactElement } from "react";
import { render, RenderOptions } from "@testing-library/react";
import { ShellProvider } from "@/components/shell/ShellContext";

interface ProtectedPageRenderOptions extends Omit<RenderOptions, "wrapper"> {
  /**
   * Optional shell config to set on initial render.
   * Default: { title: "Test Page" }
   */
  shellConfig?: { title: string; subtitle?: string; actions?: React.ReactNode };
}

/**
 * Render a component wrapped in the providers needed for protected routes:
 * - ShellProvider (for useShellConfig hook)
 *
 * Use this when testing components from `src/app/(protected)/*` that require authentication.
 * You must separately mock:
 * - @/components/auth/AuthGuard (return children, simulating authenticated user)
 * - next/navigation (useRouter, usePathname, useSearchParams)
 *
 * @param component - The component to render
 * @param options - RTL render options + shellConfig
 *
 * @example
 * // In your test file:
 * jest.mock("@/components/auth/AuthGuard", () => {
 *   return function MockAuthGuard({ children }: { children: React.ReactNode }) {
 *     return <>{children}</>;
 *   };
 * });
 *
 * it("renders the component inside protected layout", () => {
 *   const { getByText } = renderProtectedPage(
 *     <MyProtectedComponent />,
 *     { shellConfig: { title: "My Page" } }
 *   );
 *   expect(getByText(/content/i)).toBeInTheDocument();
 * });
 */
export function renderProtectedPage(
  component: ReactElement,
  options?: ProtectedPageRenderOptions
) {
  const Wrapper = ({ children }: { children: React.ReactNode }) => (
    <ShellProvider>{children}</ShellProvider>
  );

  return render(component, { wrapper: Wrapper, ...options });
}
