import api from './api';

export interface CitaMedicaDTO {
    id?: number;
    fechaHora: string;
    estado: 'PROGRAMADA' | 'EN_SALA_ESPERA' | 'EN_TRIAGE' | 'ESPERANDO_MEDICO' | 'EN_CONSULTA' | 'CANCELADA' | 'ATENDIDA';
    observaciones?: string;
    user?: { id: string; login?: string };
    paciente?: { id: number; nombres?: string; apellidos?: string };
}

export const CitaService = {
    getAll: async (params?: Record<string, any>): Promise<CitaMedicaDTO[]> => {
        const response = await api.get('/api/cita-medicas', { params });
        return response.data;
    },

    getById: async (id: number): Promise<CitaMedicaDTO> => {
        const response = await api.get(`/api/cita-medicas/${id}`);
        return response.data;
    },

    count: async (params?: Record<string, any>): Promise<number> => {
        const response = await api.get('/api/cita-medicas/count', { params });
        return response.data;
    },

    create: async (cita: CitaMedicaDTO): Promise<CitaMedicaDTO> => {
        const response = await api.post('/api/cita-medicas', cita);
        return response.data;
    },

    update: async (id: number, cita: CitaMedicaDTO): Promise<CitaMedicaDTO> => {
        const response = await api.put(`/api/cita-medicas/${id}`, { ...cita, id });
        return response.data;
    },

    patch: async (id: number, data: Partial<CitaMedicaDTO>): Promise<CitaMedicaDTO> => {
        const response = await api.patch(`/api/cita-medicas/${id}`, { ...data, id }, {
            headers: { 'Content-Type': 'application/merge-patch+json' }
        });
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await api.delete(`/api/cita-medicas/${id}`);
    },
};
