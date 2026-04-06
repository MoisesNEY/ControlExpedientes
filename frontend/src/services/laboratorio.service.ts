import api from './api';

export interface ResultadoLaboratorioDTO {
    id?: number;
    tipoExamen: string;
    resultado: string;
    valorReferencia?: string;
    unidad?: string;
    observaciones?: string;
    fechaExamen: string; // ISO date
    paciente?: { id: number; nombres?: string; apellidos?: string };
    consulta?: { id: number };
}

export const LaboratorioService = {
    getAll: async (params?: Record<string, any>): Promise<ResultadoLaboratorioDTO[]> => {
        const response = await api.get('/api/resultados-laboratorio', { params });
        return response.data;
    },

    getById: async (id: number): Promise<ResultadoLaboratorioDTO> => {
        const response = await api.get(`/api/resultados-laboratorio/${id}`);
        return response.data;
    },

    getByPaciente: async (pacienteId: number, params?: Record<string, any>): Promise<ResultadoLaboratorioDTO[]> => {
        const response = await api.get(`/api/resultados-laboratorio/paciente/${pacienteId}`, { params });
        return response.data;
    },

    getByConsulta: async (consultaId: number): Promise<ResultadoLaboratorioDTO[]> => {
        const response = await api.get(`/api/resultados-laboratorio/consulta/${consultaId}`);
        return response.data;
    },

    create: async (data: ResultadoLaboratorioDTO): Promise<ResultadoLaboratorioDTO> => {
        const response = await api.post('/api/resultados-laboratorio', data);
        return response.data;
    },

    update: async (id: number, data: ResultadoLaboratorioDTO): Promise<ResultadoLaboratorioDTO> => {
        const response = await api.put(`/api/resultados-laboratorio/${id}`, { ...data, id });
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await api.delete(`/api/resultados-laboratorio/${id}`);
    },
};
