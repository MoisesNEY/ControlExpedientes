import api from './api';
import { downloadBlob, getFilenameFromDisposition } from '../utils/download';

export type DatabaseBackupFrequency = 'DAILY' | 'WEEKLY' | 'INTERVAL_HOURS';

export interface DatabaseBackupSettings {
    enabled: boolean;
    frequency: DatabaseBackupFrequency;
    intervalHours: number | null;
    dayOfWeek: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY' | null;
    time: string | null;
    lastBackupAt: string | null;
    lastAutomaticExecutionAt: string | null;
    nextExecutionAt: string | null;
    lastBackupFilename: string | null;
}

export interface DatabaseBackupHistoryItem {
    filename: string;
    sizeBytes: number;
    createdAt: string;
    trigger: string;
    automatic: boolean;
}

export interface DatabaseBackupSummary {
    settings: DatabaseBackupSettings;
    backups: DatabaseBackupHistoryItem[];
}

export const DatabaseAdminService = {
    getSummary: async (): Promise<DatabaseBackupSummary> => {
        const response = await api.get<DatabaseBackupSummary>('/api/admin/database/summary');
        return response.data;
    },

    saveSettings: async (settings: DatabaseBackupSettings): Promise<DatabaseBackupSettings> => {
        const response = await api.put<DatabaseBackupSettings>('/api/admin/database/settings', settings);
        return response.data;
    },

    exportDatabase: async (): Promise<void> => {
        const response = await api.get('/api/admin/database/export', {
            responseType: 'blob',
        });
        downloadBlob(response.data, getFilenameFromDisposition(response.headers['content-disposition']) ?? `control-expedientes-backup-${Date.now()}.backup`);
    },

    restoreDatabase: async (file: File, password: string): Promise<void> => {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('password', password);
        await api.post('/api/admin/database/restore', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
    },

    restoreStoredBackup: async (filename: string, password: string): Promise<void> => {
        await api.post('/api/admin/database/restore/stored', null, {
            params: { filename, password },
        });
    },

    downloadStoredBackup: async (filename: string): Promise<void> => {
        const response = await api.get('/api/admin/database/stored', {
            params: { filename },
            responseType: 'blob',
        });
        downloadBlob(response.data, getFilenameFromDisposition(response.headers['content-disposition']) ?? filename);
    },
};
