import api from './api';
import { downloadBlob, getFilenameFromDisposition } from '../utils/download';

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

interface ConsultasResumenPayload {
    fechaInicio: string;
    fechaFin: string;
    pacienteId?: number;
    doctorLogin?: string;
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

    descargarHistorialExcel: async (pacienteId: number): Promise<void> => {
        const response = await api.get(`/api/reportes/historial/${pacienteId}/excel`, {
            responseType: 'blob',
        });
        downloadBlob(response.data, getFilenameFromDisposition(response.headers['content-disposition']) ?? `historial-clinico-${pacienteId}.xlsx`);
    },

    descargarExpedientePdf: async (expedienteId: number): Promise<void> => {
        const response = await api.get(`/api/reportes/expediente/${expedienteId}`, {
            responseType: 'blob',
        });
        downloadBlob(response.data, getFilenameFromDisposition(response.headers['content-disposition']) ?? `expediente-${expedienteId}.pdf`);
    },

    descargarExpedienteExcel: async (expedienteId: number): Promise<void> => {
        const response = await api.get(`/api/reportes/expediente/${expedienteId}/excel`, {
            responseType: 'blob',
        });
        downloadBlob(response.data, getFilenameFromDisposition(response.headers['content-disposition']) ?? `expediente-${expedienteId}.xlsx`);
    },

    descargarResumenConsultasPdf: async (payload: ConsultasResumenPayload): Promise<void> => {
        const response = await api.get('/api/reportes/consultas/resumen', {
            params: payload,
            responseType: 'blob',
        });
        downloadBlob(response.data, getFilenameFromDisposition(response.headers['content-disposition']) ?? `consultas-${payload.fechaInicio}-${payload.fechaFin}.pdf`);
    },

    descargarResumenConsultasExcel: async (payload: ConsultasResumenPayload): Promise<void> => {
        const response = await api.get('/api/reportes/consultas/resumen/excel', {
            params: payload,
            responseType: 'blob',
        });
        downloadBlob(response.data, getFilenameFromDisposition(response.headers['content-disposition']) ?? `consultas-${payload.fechaInicio}-${payload.fechaFin}.xlsx`);
    },
};
