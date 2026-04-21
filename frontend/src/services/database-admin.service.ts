import api from './api';

export const DatabaseAdminService = {
    exportDatabase: async (): Promise<void> => {
        const response = await api.get('/api/admin/database/export', {
            responseType: 'blob',
        });
        downloadResponseBlob(response.data, getFilenameFromDisposition(response.headers['content-disposition']) ?? `control-expedientes-backup-${Date.now()}.backup`);
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

function downloadResponseBlob(blob: Blob, filename: string) {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
}

function getFilenameFromDisposition(contentDisposition?: string): string | null {
    if (!contentDisposition) return null;
    const match = contentDisposition.match(/filename="?([^"]+)"?/i);
    return match?.[1] ?? null;
}
