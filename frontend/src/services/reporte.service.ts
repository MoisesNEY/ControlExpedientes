import api from './api';

interface RecetaPreviewPayload {
    citaId?: number;
    fechaConsulta?: string;
    nombrePaciente: string;
    codigoPaciente?: string;
    cedulaPaciente?: string;
    motivoConsulta?: string;
    codigoDiagnostico?: string;
    descripcionDiagnostico: string;
    notasMedicas?: string;
    doctorName?: string;
    recetas: Array<{
        medicamento: string;
        dosis: string;
        frecuencia: string;
        duracion: string;
    }>;
}

export const ReporteService = {
    descargarRecetaPdf: async (citaId: number): Promise<void> => {
        const response = await api.get(`/api/reportes/receta/${citaId}`, {
            responseType: 'blob',
        });
        downloadBlob(response.data, getFilenameFromDisposition(response.headers['content-disposition']) ?? `receta-cita-${citaId}.pdf`);
    },

    descargarRecetaPdfPorConsulta: async (consultaId: number): Promise<void> => {
        const response = await api.get(`/api/reportes/receta/consulta/${consultaId}`, {
            responseType: 'blob',
        });
        downloadBlob(response.data, getFilenameFromDisposition(response.headers['content-disposition']) ?? `receta-consulta-${consultaId}.pdf`);
    },

    descargarRecetaPreviewPdf: async (payload: RecetaPreviewPayload): Promise<void> => {
        const response = await api.post('/api/reportes/receta/preview', payload, {
            responseType: 'blob',
        });
        downloadBlob(response.data, getFilenameFromDisposition(response.headers['content-disposition']) ?? `receta-preliminar-${payload.citaId ?? Date.now()}.pdf`);
    },

    descargarHistorialPdf: async (pacienteId: number): Promise<void> => {
        const response = await api.get(`/api/reportes/historial/${pacienteId}`, {
            responseType: 'blob',
        });
        downloadBlob(response.data, getFilenameFromDisposition(response.headers['content-disposition']) ?? `historial-clinico-${pacienteId}.pdf`);
    },

    descargarExpedientePdf: async (expedienteId: number): Promise<void> => {
        const response = await api.get(`/api/reportes/expediente/${expedienteId}`, {
            responseType: 'blob',
        });
        downloadBlob(response.data, getFilenameFromDisposition(response.headers['content-disposition']) ?? `expediente-${expedienteId}.pdf`);
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

function getFilenameFromDisposition(contentDisposition?: string): string | null {
    if (!contentDisposition) return null;
    const match = contentDisposition.match(/filename="?([^"]+)"?/i);
    return match?.[1] ?? null;
}
