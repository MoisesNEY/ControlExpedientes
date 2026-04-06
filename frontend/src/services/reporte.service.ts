import api from './api';

export const ReporteService = {
    descargarRecetaPdf: async (citaId: number): Promise<void> => {
        const response = await api.get(`/api/reportes/receta/${citaId}`, {
            responseType: 'blob',
        });
        downloadBlob(response.data, `receta-cita-${citaId}.pdf`);
    },

    descargarHistorialPdf: async (pacienteId: number): Promise<void> => {
        const response = await api.get(`/api/reportes/historial/${pacienteId}`, {
            responseType: 'blob',
        });
        downloadBlob(response.data, `historial-clinico-${pacienteId}.pdf`);
    },

    descargarExpedientePdf: async (expedienteId: number): Promise<void> => {
        const response = await api.get(`/api/reportes/expediente/${expedienteId}`, {
            responseType: 'blob',
        });
        downloadBlob(response.data, `expediente-${expedienteId}.pdf`);
    },
};

function downloadBlob(blob: Blob, filename: string) {
    const url = window.URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
}
