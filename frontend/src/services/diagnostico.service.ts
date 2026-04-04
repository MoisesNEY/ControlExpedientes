import api from './api';

export interface Diagnostico {
    id: number;
    codigoCie10: string;
    descripcion: string;
}

export const DiagnosticoService = {
    search: async (query: string): Promise<Diagnostico[]> => {
        if (!query || query.length < 2) return [];
        // Spring Boot JHipster generates filtering endpoints using criteria
        // We will search by 'descripcion.contains' or 'codigoCie10.contains'
        // For simplicity we will fetch 'descripcion.contains' first.
        const response = await api.get('/api/diagnosticos', {
            params: {
                'descripcion.contains': query,
                'size': 10 // Limit results
            }
        });
        return response.data;
    },

    create: async (codigoCie10: string, descripcion: string): Promise<Diagnostico> => {
        const response = await api.post('/api/diagnosticos', { codigoCie10, descripcion });
        return response.data;
    }
};
