const AUTO_DOWNLOAD_BACKUPS_KEY = 'control-expedientes-auto-download-backups';
const DEVICE_PREFERENCES_EVENT = 'control-expedientes-device-preferences-changed';

export const DevicePreferencesService = {
  devicePreferencesEvent: DEVICE_PREFERENCES_EVENT,

  shouldAutoDownloadBackups: (): boolean => {
    try {
      return localStorage.getItem(AUTO_DOWNLOAD_BACKUPS_KEY) !== 'false';
    } catch {
      return true;
    }
  },

  setAutoDownloadBackups: (enabled: boolean): void => {
    try {
      localStorage.setItem(AUTO_DOWNLOAD_BACKUPS_KEY, String(enabled));
    } catch {
      // ignore storage failures
    }
    window.dispatchEvent(new Event(DEVICE_PREFERENCES_EVENT));
  },
};
