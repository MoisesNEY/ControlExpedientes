import api from './api';

export interface ExpedienteClinicoDTO {
    id?: number;
    numeroExpediente: string;
    fechaApertura: string;
    observaciones?: string;
    paciente?: { id: number; nombres?: string; apellidos?: string };
}

export const ExpedienteService = {
    getAll: async (params?: Record<string, any>): Promise<ExpedienteClinicoDTO[]> => {
        const response = await api.get('/api/expediente-clinicos', { params });
        return response.data;
    },

    getById: async (id: number): Promise<ExpedienteClinicoDTO> => {
        const response = await api.get(`/api/expediente-clinicos/${id}`);
        return response.data;
    },

    getByPacienteId: async (pacienteId: number): Promise<ExpedienteClinicoDTO> => {
        const response = await api.get(`/api/expediente-clinicos/paciente/${pacienteId}`);
        return response.data;
    },

    count: async (params?: Record<string, any>): Promise<number> => {
        const response = await api.get('/api/expediente-clinicos/count', { params });
        return response.data;
    },

    create: async (expediente: ExpedienteClinicoDTO): Promise<ExpedienteClinicoDTO> => {
        const response = await api.post('/api/expediente-clinicos', expediente);
        return response.data;
    },

    update: async (id: number, expediente: ExpedienteClinicoDTO): Promise<ExpedienteClinicoDTO> => {
        const response = await api.put(`/api/expediente-clinicos/${id}`, { ...expediente, id });
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await api.delete(`/api/expediente-clinicos/${id}`);
    },
};
