import api from './api';

export interface MedicamentoDTO {
    id?: number;
    nombre: string;
    descripcion?: string;
    stock: number;
}

export const MedicamentoService = {
    getAll: async (params?: Record<string, any>): Promise<MedicamentoDTO[]> => {
        const response = await api.get('/api/medicamentos', { params });
        return response.data;
    },

    getById: async (id: number): Promise<MedicamentoDTO> => {
        const response = await api.get(`/api/medicamentos/${id}`);
        return response.data;
    },

    count: async (params?: Record<string, any>): Promise<number> => {
        const response = await api.get('/api/medicamentos/count', { params });
        return response.data;
    },

    getLowStock: async (): Promise<MedicamentoDTO[]> => {
        const response = await api.get('/api/medicamentos/low-stock');
        return response.data;
    },

    create: async (medicamento: MedicamentoDTO): Promise<MedicamentoDTO> => {
        const response = await api.post('/api/medicamentos', medicamento);
        return response.data;
    },

    update: async (id: number, medicamento: MedicamentoDTO): Promise<MedicamentoDTO> => {
        const response = await api.put(`/api/medicamentos/${id}`, { ...medicamento, id });
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await api.delete(`/api/medicamentos/${id}`);
    },
};
