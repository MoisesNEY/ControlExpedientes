import api from './api';
import { downloadBlob, getFilenameFromDisposition } from '../utils/download';

export const DatabaseAdminService = {
    exportDatabase: async (): Promise<void> => {
        const response = await api.get('/api/admin/database/export', {
            responseType: 'blob',
        });
        downloadBlob(response.data, getFilenameFromDisposition(response.headers['content-disposition']) ?? `control-expedientes-backup-${Date.now()}.backup`);
    },

    restoreDatabase: async (file: File): Promise<void> => {
        const formData = new FormData();
        formData.append('file', file);
        await api.post('/api/admin/database/restore', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
    },
};
