import api from './api';

export interface SystemSettings {
  applicationName: string;
  brandName: string;
  primaryColor: string;
  sidebarColor: string;
  defaultTheme: 'light' | 'dark';
  defaultDeviceBackupDownloadsEnabled: boolean;
  showBackupDownloadReminders: boolean;
}

export interface UserPreferences {
  primaryColor: string | null;
  sidebarColor: string | null;
  theme: 'light' | 'dark' | null;
  deviceBackupDownloadsEnabled: boolean | null;
}

export const defaultSystemSettings: SystemSettings = {
  applicationName: 'Control Expedientes',
  brandName: 'ClinData',
  primaryColor: '#0284c7',
  sidebarColor: '#071e2b',
  defaultTheme: 'light',
  defaultDeviceBackupDownloadsEnabled: true,
  showBackupDownloadReminders: true,
};

export const defaultUserPreferences: UserPreferences = {
  primaryColor: null,
  sidebarColor: null,
  theme: null,
  deviceBackupDownloadsEnabled: null,
};

export const SystemSettingsService = {
  getSettings: async (): Promise<SystemSettings> => {
    const response = await api.get<SystemSettings>('/api/system/settings');
    return response.data;
  },

  updateSettings: async (payload: SystemSettings): Promise<SystemSettings> => {
    const response = await api.put<SystemSettings>('/api/system/settings', payload);
    return response.data;
  },

  getUserPreferences: async (): Promise<UserPreferences> => {
    const response = await api.get<UserPreferences>('/api/account/preferences');
    return response.data;
  },

  updateUserPreferences: async (payload: UserPreferences): Promise<UserPreferences> => {
    const response = await api.put<UserPreferences>('/api/account/preferences', payload);
    return response.data;
  },
};
