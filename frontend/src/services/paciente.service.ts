import api from './api';

export interface PacienteDTO {
    id?: number;
    codigo: string;
    nombres: string;
    apellidos: string;
    sexo: 'MASCULINO' | 'FEMENINO' | 'OTRO';
    fechaNacimiento: string;
    cedula?: string;
    telefono?: string;
    direccion?: string;
    estadoCivil?: 'SOLTERO' | 'CASADO' | 'DIVORCIADO' | 'VIUDO';
    email?: string;
    activo: boolean;
    expediente?: { id: number };
}

export const PacienteService = {
    getAll: async (params?: Record<string, any>): Promise<PacienteDTO[]> => {
        const response = await api.get('/api/pacientes', { params });
        return response.data;
    },

    getById: async (id: number): Promise<PacienteDTO> => {
        const response = await api.get(`/api/pacientes/${id}`);
        return response.data;
    },

    count: async (params?: Record<string, any>): Promise<number> => {
        const response = await api.get('/api/pacientes/count', { params });
        return response.data;
    },

    create: async (paciente: PacienteDTO): Promise<PacienteDTO> => {
        const response = await api.post('/api/pacientes', paciente);
        return response.data;
    },

    update: async (id: number, paciente: PacienteDTO): Promise<PacienteDTO> => {
        const response = await api.put(`/api/pacientes/${id}`, { ...paciente, id });
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await api.delete(`/api/pacientes/${id}`);
    },
};
