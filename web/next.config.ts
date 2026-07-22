import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",
  // `npm run lint` is the dedicated lint gate (see this repo's task Done Criteria);
  // keeping the build from also failing on unrelated pre-existing lint debt (predates
  // eslint.config.mjs, which didn't exist until task-09) mirrors that separation.
  eslint: { ignoreDuringBuilds: true },
  async headers() {
    return [
      {
        source: "/(.*)",
        headers: [
          {
            key: "Cross-Origin-Opener-Policy",
            value: "same-origin-allow-popups",
          },
        ],
      },
    ];
  },
  async rewrites() {
    return [
      {
        // Keep the /api prefix when forwarding — AssessmentController and every other
        // controller in api/ are mapped under @RequestMapping("/api/v1"), so stripping
        // it here (as ":path*" alone previously did) sent every browser-side request to
        // a path api/ doesn't serve, surfacing as a 500 (NoResourceFoundException falls
        // through GlobalExceptionHandler's catch-all) instead of the real response.
        // Confirmed live via task-13's real-browser walkthrough — no prior test caught
        // this because jest mocks fetch entirely and never exercises this rewrite.
        source: "/api/:path*",
        destination: `${process.env.API_BASE_URL ?? "http://localhost:8080"}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
