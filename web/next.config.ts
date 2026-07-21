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
        source: "/api/:path*",
        destination: `${process.env.API_BASE_URL ?? "http://localhost:8080"}/:path*`,
      },
    ];
  },
};

export default nextConfig;
