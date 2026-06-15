import { defineConfig } from 'astro/config';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
  site: 'https://cmartinezs.github.io',
  base: '/grade-ops-ai',
  output: 'static',
  vite: {
    plugins: [tailwindcss()],
  },
});
