import api from './api';

export interface Diagnostico {
    id: number;
    codigoIcd10: string;
    nombre: string;
}

export interface Medicamento {
    id: number;
    nombre: string;
    descripcion: string;
    stock: number;
}

export interface Receta {
    medicamento: Medicamento;
    dosis: string;
    frecuencia: string;
    duracion: string;
}

export interface ConsultationData {
    consulta: {
        motivoConsulta: string;
        notasMedicas: string;
        fechaConsulta: string;
        expediente?: { id: number };
    };
    signosVitales: {
        peso: number;
        altura: number;
        presionArterial: string;
        temperatura: number;
        frecuenciaCardiaca: number;
    };
    diagnostico: { id: number };
    recetas: {
        dosis: string;
        frecuencia: string;
        duracion: string;
        medicamento: { id: number };
    }[];
}

export const ConsultationService = {
    searchDiagnosticos: async (query: string): Promise<Diagnostico[]> => {
        const response = await api.get(`/api/diagnosticos/search?query=${query}`);
        return response.data;
    },

    searchMedicamentos: async (query: string): Promise<Medicamento[]> => {
        const response = await api.get(`/api/medicamentos?nombre.contains=${query}`);
        return response.data;
    },

    saveConsultation: async (data: ConsultationData) => {
        const response = await api.post('/api/atencion-medica/finalizar-consulta', data);
        return response.data;
    }
};
