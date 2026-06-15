import { es, type UIKey } from '../i18n/es';
import { en } from '../i18n/en';

const translations = { es, en } as const;
export type Lang = keyof typeof translations;

export function useTranslations(lang: Lang) {
  return function t(key: UIKey): string {
    const dict = translations[lang] as Record<string, string>;
    return dict[key] ?? (translations['es'] as Record<string, string>)[key] ?? key;
  };
}

export function getAlternatePath(lang: Lang, path: string, base: string): string {
  if (lang === 'es') return path === '/' ? `${base}/en` : `${base}/en${path}`;
  const esPath = path.replace(/^\/en/, '');
  return esPath ? `${base}${esPath}` : base;
}
