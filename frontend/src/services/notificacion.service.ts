import api from './api';
import type { Notificacion } from '../hooks/useWebSocket';

export const NotificacionService = {
    getMine: async (): Promise<Notificacion[]> => {
        const response = await api.get('/api/notificaciones/me');
        return response.data;
    },

    markRead: async (id: number): Promise<void> => {
        await api.patch(`/api/notificaciones/me/${id}/read`);
    },

    markAllRead: async (): Promise<void> => {
        await api.patch('/api/notificaciones/me/read');
    },
};
