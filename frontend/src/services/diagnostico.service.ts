import api from './api';

export interface Diagnostico {
    id: number;
    codigoCIE: string;
    descripcion: string;
}

interface DiagnosticoApiResponse {
    id?: number;
    codigoCIE?: string | null;
    codigoCie10?: string | null;
    descripcion?: string | null;
}

const mapDiagnostico = (item: DiagnosticoApiResponse): Diagnostico => ({
    id: item.id ?? 0,
    codigoCIE: item.codigoCIE ?? item.codigoCie10 ?? '',
    descripcion: item.descripcion ?? '',
});

export const DiagnosticoService = {
    search: async (query: string): Promise<Diagnostico[]> => {
        if (!query || query.length < 2) return [];
        const response = await api.get('/api/diagnosticos/search', {
            params: {
                query,
                size: 10,
                sort: 'descripcion,asc',
            }
        });
        return (response.data as DiagnosticoApiResponse[]).map(mapDiagnostico);
    },

    getAll: async (params?: Record<string, string | number | boolean>): Promise<Diagnostico[]> => {
        const response = await api.get('/api/diagnosticos', {
            params: {
                'consultaId.specified': false,
                sort: 'descripcion,asc',
                ...params,
            },
        });
        return (response.data as DiagnosticoApiResponse[]).map(mapDiagnostico);
    },

    create: async (codigoCIE: string, descripcion: string): Promise<Diagnostico> => {
        const response = await api.post('/api/diagnosticos', { codigoCIE, descripcion });
        return mapDiagnostico(response.data as DiagnosticoApiResponse);
    },

    update: async (id: number, diagnostico: Omit<Diagnostico, 'id'>): Promise<Diagnostico> => {
        const response = await api.put(`/api/diagnosticos/${id}`, { id, ...diagnostico });
        return mapDiagnostico(response.data as DiagnosticoApiResponse);
    },

    delete: async (id: number): Promise<void> => {
        await api.delete(`/api/diagnosticos/${id}`);
    },
};
