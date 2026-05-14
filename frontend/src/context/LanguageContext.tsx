import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { englishTranslations, type Language } from '../i18n/translations';

interface LanguageContextType {
  language: Language;
  setLanguage: (language: Language) => void;
  t: (text: string) => string;
}

const LANGUAGE_STORAGE_KEY = 'control-expedientes-language';
const LanguageContext = createContext<LanguageContextType | null>(null);

const readStoredLanguage = (): Language => {
  if (typeof window === 'undefined') return 'es';
  const stored = window.localStorage.getItem(LANGUAGE_STORAGE_KEY);
  return stored === 'en' || stored === 'es' ? stored : 'es';
};

export const translateText = (text: string, language: Language) => {
  if (language === 'es') return text;
  return englishTranslations[text] ?? text;
};

export const LanguageProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [language, setLanguageState] = useState<Language>(readStoredLanguage);

  const setLanguage = (nextLanguage: Language) => {
    setLanguageState(nextLanguage);
    try {
      window.localStorage.setItem(LANGUAGE_STORAGE_KEY, nextLanguage);
    } catch {
      // El idioma debe seguir funcionando aunque el navegador bloquee localStorage.
    }
  };

  useEffect(() => {
    document.documentElement.lang = language;
  }, [language]);

  const value = useMemo(
    () => ({
      language,
      setLanguage,
      t: (text: string) => translateText(text, language),
    }),
    [language],
  );

  return <LanguageContext.Provider value={value}>{children}</LanguageContext.Provider>;
};

export const useLanguage = () => {
  const context = useContext(LanguageContext);
  if (!context) {
    throw new Error('useLanguage must be used within a LanguageProvider');
  }
  return context;
};
