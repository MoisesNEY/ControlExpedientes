import api from './api';

export interface InteraccionMedicamentosaDTO {
    id: number;
    medicamentoA: { id: number; nombre: string };
    medicamentoB: { id: number; nombre: string };
    severidad: 'LEVE' | 'MODERADA' | 'GRAVE';
    descripcion: string;
    recomendacion?: string;
}

export interface InteraccionMedicamentosaInput {
    medicamentoA: { id: number };
    medicamentoB: { id: number };
    severidad: 'LEVE' | 'MODERADA' | 'GRAVE';
    descripcion: string;
    recomendacion?: string;
}

export const InteraccionService = {
    verificarInteracciones: async (medicamentoIds: number[]): Promise<InteraccionMedicamentosaDTO[]> => {
        if (!medicamentoIds || medicamentoIds.length < 2) return [];
        const response = await api.post('/api/interacciones-medicamentosas/verificar', medicamentoIds);
        return response.data;
    },

    getAll: async (params?: Record<string, string | number | boolean>): Promise<InteraccionMedicamentosaDTO[]> => {
        const response = await api.get('/api/interacciones-medicamentosas', { params });
        return response.data;
    },

    create: async (data: InteraccionMedicamentosaInput): Promise<InteraccionMedicamentosaDTO> => {
        const response = await api.post('/api/interacciones-medicamentosas', data);
        return response.data;
    },

    update: async (id: number, data: InteraccionMedicamentosaInput): Promise<InteraccionMedicamentosaDTO> => {
        const response = await api.put(`/api/interacciones-medicamentosas/${id}`, { id, ...data });
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await api.delete(`/api/interacciones-medicamentosas/${id}`);
    },
};
