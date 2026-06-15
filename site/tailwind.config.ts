import type { Config } from 'tailwindcss';

export default {
  content: ['./src/**/*.{astro,html,js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        teal:   { DEFAULT: '#12B8A6', dark: '#075E63' },
        coral:  '#F97316',
        indigo: '#6366F1',
        muted:  '#7890A8',
      },
    },
  },
} satisfies Config;
