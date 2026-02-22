import api from './api';

export interface AuditoriaAccionesDTO {
    id?: number;
    entidad: string;
    accion: string;
    fecha: string;
    descripcion?: string;
    user?: { id: string; login?: string };
}

export const AuditoriaService = {
    getAll: async (params?: Record<string, any>): Promise<AuditoriaAccionesDTO[]> => {
        const response = await api.get('/api/auditoria-acciones', { params });
        return response.data;
    },

    getById: async (id: number): Promise<AuditoriaAccionesDTO> => {
        const response = await api.get(`/api/auditoria-acciones/${id}`);
        return response.data;
    },

    count: async (params?: Record<string, any>): Promise<number> => {
        const response = await api.get('/api/auditoria-acciones/count', { params });
        return response.data;
    },
};
