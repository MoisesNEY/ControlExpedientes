import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { useAuth } from './AuthContext';
import { useTheme } from './ThemeContext';
import {
  defaultSystemSettings,
  defaultUserPreferences,
  SystemSettingsService,
  type SystemSettings,
  type UserPreferences,
} from '../services/system-settings.service';

interface EffectivePreferences {
  applicationName: string;
  brandName: string;
  primaryColor: string;
  sidebarColor: string;
  theme: 'light' | 'dark';
  deviceBackupDownloadsEnabled: boolean;
  showBackupDownloadReminders: boolean;
}

interface SystemSettingsContextType {
  systemSettings: SystemSettings;
  userPreferences: UserPreferences;
  effectivePreferences: EffectivePreferences;
  loading: boolean;
  refreshPreferences: () => Promise<void>;
  updateSystemSettings: (payload: SystemSettings) => Promise<SystemSettings>;
  updateUserPreferences: (payload: UserPreferences) => Promise<UserPreferences>;
}

const SystemSettingsContext = createContext<SystemSettingsContextType | null>(null);
const SYSTEM_SETTINGS_CACHE_KEY = 'control-expedientes-system-settings';
const USER_PREFERENCES_CACHE_PREFIX = 'control-expedientes-user-preferences:';

const isSystemSettings = (value: unknown): value is SystemSettings => {
  const candidate = value as Partial<SystemSettings> | null;
  return Boolean(
    candidate &&
      typeof candidate.applicationName === 'string' &&
      typeof candidate.brandName === 'string' &&
      typeof candidate.primaryColor === 'string' &&
      typeof candidate.sidebarColor === 'string' &&
      (candidate.defaultTheme === 'light' || candidate.defaultTheme === 'dark') &&
      typeof candidate.defaultDeviceBackupDownloadsEnabled === 'boolean' &&
      typeof candidate.showBackupDownloadReminders === 'boolean',
  );
};

const isUserPreferences = (value: unknown): value is UserPreferences => {
  const candidate = value as Partial<UserPreferences> | null;
  return Boolean(
    candidate &&
      (candidate.primaryColor === null || typeof candidate.primaryColor === 'string') &&
      (candidate.sidebarColor === null || typeof candidate.sidebarColor === 'string') &&
      (candidate.theme === null || candidate.theme === 'light' || candidate.theme === 'dark') &&
      (candidate.deviceBackupDownloadsEnabled === null || typeof candidate.deviceBackupDownloadsEnabled === 'boolean'),
  );
};

const readJsonCache = <T,>(key: string, guard: (value: unknown) => value is T): T | null => {
  if (typeof window === 'undefined') return null;
  try {
    const raw = window.localStorage.getItem(key);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as unknown;
    return guard(parsed) ? parsed : null;
  } catch {
    return null;
  }
};

const writeJsonCache = (key: string, value: unknown) => {
  if (typeof window === 'undefined') return;
  try {
    window.localStorage.setItem(key, JSON.stringify(value));
  } catch {
    // El cache evita parpadeos, pero no debe bloquear si el navegador no permite storage.
  }
};

const readCachedSystemSettings = () => readJsonCache(SYSTEM_SETTINGS_CACHE_KEY, isSystemSettings) ?? defaultSystemSettings;
const readCachedUserPreferences = (login: string) =>
  readJsonCache(`${USER_PREFERENCES_CACHE_PREFIX}${login}`, isUserPreferences) ?? defaultUserPreferences;

const normalizeHex = (hex: string | null | undefined, fallback: string) =>
  /^#[0-9A-Fa-f]{6}$/.test(hex ?? '') ? (hex as string) : fallback;

const hexToRgb = (hex: string, fallback = defaultSystemSettings.primaryColor) => {
  const normalized = normalizeHex(hex, fallback).slice(1);
  return {
    r: Number.parseInt(normalized.slice(0, 2), 16),
    g: Number.parseInt(normalized.slice(2, 4), 16),
    b: Number.parseInt(normalized.slice(4, 6), 16),
  };
};

const mix = (from: number, to: number, weight: number) => Math.round(from + (to - from) * weight);
const toHex = (value: number) => value.toString(16).padStart(2, '0');

const mixHex = (hex: string, target: '#ffffff' | '#000000', weight: number) => {
  const source = hexToRgb(hex);
  const destination = target === '#ffffff' ? { r: 255, g: 255, b: 255 } : { r: 0, g: 0, b: 0 };
  return `#${toHex(mix(source.r, destination.r, weight))}${toHex(mix(source.g, destination.g, weight))}${toHex(mix(source.b, destination.b, weight))}`;
};

const applyPrimaryColor = (primaryColor: string) => {
  const root = document.documentElement;
  const color = normalizeHex(primaryColor, defaultSystemSettings.primaryColor);
  root.style.setProperty('--system-primary-50', mixHex(color, '#ffffff', 0.94));
  root.style.setProperty('--system-primary-100', mixHex(color, '#ffffff', 0.86));
  root.style.setProperty('--system-primary-200', mixHex(color, '#ffffff', 0.72));
  root.style.setProperty('--system-primary-300', mixHex(color, '#ffffff', 0.52));
  root.style.setProperty('--system-primary-400', mixHex(color, '#ffffff', 0.28));
  root.style.setProperty('--system-primary-500', color);
  root.style.setProperty('--system-primary-600', mixHex(color, '#000000', 0.12));
  root.style.setProperty('--system-primary-700', mixHex(color, '#000000', 0.24));
  root.style.setProperty('--system-primary-800', mixHex(color, '#000000', 0.36));
};

const applySidebarColor = (sidebarColor: string) => {
  const root = document.documentElement;
  const color = normalizeHex(sidebarColor, defaultSystemSettings.sidebarColor);
  root.style.setProperty('--system-sidebar-500', color);
  root.style.setProperty('--system-sidebar-600', mixHex(color, '#000000', 0.12));
  root.style.setProperty('--system-sidebar-700', mixHex(color, '#000000', 0.24));
  root.style.setProperty('--system-sidebar-soft', mixHex(color, '#ffffff', 0.9));
};

const resolveEffectivePreferences = (systemSettings: SystemSettings, userPreferences: UserPreferences): EffectivePreferences => ({
  applicationName: systemSettings.applicationName || defaultSystemSettings.applicationName,
  brandName: systemSettings.brandName || defaultSystemSettings.brandName,
  primaryColor: userPreferences.primaryColor || systemSettings.primaryColor || defaultSystemSettings.primaryColor,
  sidebarColor: userPreferences.sidebarColor || systemSettings.sidebarColor || defaultSystemSettings.sidebarColor,
  theme: userPreferences.theme || systemSettings.defaultTheme || defaultSystemSettings.defaultTheme,
  deviceBackupDownloadsEnabled:
    userPreferences.deviceBackupDownloadsEnabled ?? systemSettings.defaultDeviceBackupDownloadsEnabled,
  showBackupDownloadReminders: systemSettings.showBackupDownloadReminders,
});

export const SystemSettingsProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, loading: authLoading, user } = useAuth();
  const { setTheme } = useTheme();
  const [systemSettings, setSystemSettings] = useState<SystemSettings>(readCachedSystemSettings);
  const [userPreferences, setUserPreferences] = useState<UserPreferences>(defaultUserPreferences);
  const [loading, setLoading] = useState(true);
  const [initialSettingsLoaded, setInitialSettingsLoaded] = useState(false);
  const [loadedUserPreferencesForLogin, setLoadedUserPreferencesForLogin] = useState<string | null>(null);
  const currentLogin = user?.preferred_username ?? null;

  const effectivePreferences = useMemo(
    () => resolveEffectivePreferences(systemSettings, isAuthenticated ? userPreferences : defaultUserPreferences),
    [isAuthenticated, systemSettings, userPreferences],
  );

  useEffect(() => {
    applyPrimaryColor(effectivePreferences.primaryColor);
    applySidebarColor(effectivePreferences.sidebarColor);
    setTheme(effectivePreferences.theme);
    document.title = effectivePreferences.applicationName;
  }, [effectivePreferences, setTheme]);

  const refreshPreferences = useCallback(async () => {
    if (authLoading) {
      return;
    }

    setLoading(true);
    if (!isAuthenticated) {
      setUserPreferences(defaultUserPreferences);
      setLoadedUserPreferencesForLogin(null);
    }
    try {
      const systemData = await SystemSettingsService.getSettings();
      setSystemSettings(systemData);
      writeJsonCache(SYSTEM_SETTINGS_CACHE_KEY, systemData);
      if (isAuthenticated) {
        const login = user?.preferred_username;
        if (login) {
          setUserPreferences(readCachedUserPreferences(login));
        }
        const userData = await SystemSettingsService.getUserPreferences();
        setUserPreferences(userData);
        if (login) {
          writeJsonCache(`${USER_PREFERENCES_CACHE_PREFIX}${login}`, userData);
          setLoadedUserPreferencesForLogin(login);
        }
      } else {
        setUserPreferences(defaultUserPreferences);
        setLoadedUserPreferencesForLogin(null);
      }
    } catch {
      setSystemSettings(readCachedSystemSettings());
      if (isAuthenticated && user?.preferred_username) {
        setUserPreferences(readCachedUserPreferences(user.preferred_username));
        setLoadedUserPreferencesForLogin(user.preferred_username);
      } else {
        setUserPreferences(defaultUserPreferences);
        setLoadedUserPreferencesForLogin(null);
      }
    } finally {
      setLoading(false);
      setInitialSettingsLoaded(true);
    }
  }, [authLoading, isAuthenticated, user?.preferred_username]);

  const updateSystemSettings = useCallback(async (payload: SystemSettings) => {
    const updated = await SystemSettingsService.updateSettings(payload);
    setSystemSettings(updated);
    writeJsonCache(SYSTEM_SETTINGS_CACHE_KEY, updated);
    return updated;
  }, []);

  const updateUserPreferences = useCallback(async (payload: UserPreferences) => {
    const updated = await SystemSettingsService.updateUserPreferences(payload);
    setUserPreferences(updated);
    if (currentLogin) {
      writeJsonCache(`${USER_PREFERENCES_CACHE_PREFIX}${currentLogin}`, updated);
      setLoadedUserPreferencesForLogin(currentLogin);
    }
    return updated;
  }, [currentLogin]);

  useEffect(() => {
    void refreshPreferences();
  }, [refreshPreferences, user?.preferred_username]);

  const shouldBlockRender =
    !initialSettingsLoaded ||
    (isAuthenticated && Boolean(currentLogin) && loadedUserPreferencesForLogin !== currentLogin);

  const value = useMemo(
    () => ({
      systemSettings,
      userPreferences,
      effectivePreferences,
      loading,
      refreshPreferences,
      updateSystemSettings,
      updateUserPreferences,
    }),
    [effectivePreferences, loading, refreshPreferences, systemSettings, updateSystemSettings, updateUserPreferences, userPreferences],
  );

  return (
    <SystemSettingsContext.Provider value={value}>
      {shouldBlockRender ? (
        <div className="flex min-h-screen items-center justify-center bg-slate-50 text-slate-500 dark:bg-[#0b1a24] dark:text-slate-300">
          <div className="h-9 w-9 animate-spin rounded-full border-4 border-slate-200 border-t-sky-500 dark:border-white/10 dark:border-t-sky-400" />
        </div>
      ) : (
        children
      )}
    </SystemSettingsContext.Provider>
  );
};

export const useSystemSettings = () => {
  const context = useContext(SystemSettingsContext);
  if (!context) {
    throw new Error('useSystemSettings must be used within a SystemSettingsProvider');
  }
  return context;
};
