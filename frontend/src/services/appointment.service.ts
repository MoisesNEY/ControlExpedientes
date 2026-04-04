import api from './api';

export interface Appointment {
    id: number;
    fechaHora: string;
    estado: 'PROGRAMADA' | 'EN_SALA_ESPERA' | 'EN_TRIAGE' | 'ESPERANDO_MEDICO' | 'EN_CONSULTA' | 'CANCELADA' | 'ATENDIDA';
    observaciones?: string;
    paciente?: {
        id: number;
        nombres?: string;
        apellidos?: string;
    };
    user?: {
        id: string;
        login?: string;
    };
}

export const AppointmentService = {
    getTodayAppointments: async (userId: string): Promise<Appointment[]> => {
        const today = new Date();
        const startOfDay = new Date(today.setHours(0, 0, 0, 0)).toISOString();
        const endOfDay = new Date(today.setHours(23, 59, 59, 999)).toISOString();

        // JHipster criteria: fechaHora.greaterThanOrEqual and fechaHora.lessThanOrEqual
        // Also userId.equals
        const response = await api.get('/api/cita-medicas', {
            params: {
                'userId.equals': userId,
                'fechaHora.greaterThanOrEqual': startOfDay,
                'fechaHora.lessThanOrEqual': endOfDay,
                'sort': 'fechaHora,asc'
            }
        });
        return response.data;
    },

    getById: async (id: number): Promise<Appointment> => {
        const response = await api.get(`/api/cita-medicas/${id}`);
        return response.data;
    },

    getTriageCompletedAppointments: async (userId: string): Promise<Appointment[]> => {
        const today = new Date();
        const startOfDay = new Date(today.setHours(0, 0, 0, 0)).toISOString();
        const endOfDay = new Date(today.setHours(23, 59, 59, 999)).toISOString();

        const response = await api.get('/api/cita-medicas', {
            params: {
                'userId.equals': userId,
                'fechaHora.greaterThanOrEqual': startOfDay,
                'fechaHora.lessThanOrEqual': endOfDay,
                'estado.equals': 'TRIAGE_COMPLETADO',
                'sort': 'fechaHora,asc'
            }
        });
        return response.data;
    },

    getRecentAppointments: async (userId: string, limit: number = 5): Promise<Appointment[]> => {
        const response = await api.get('/api/cita-medicas', {
            params: {
                'userId.equals': userId,
                'size': limit,
                'sort': 'fechaHora,desc'
            }
        });
        return response.data;
    },

    updateAppointmentStatus: async (id: number, status: string): Promise<Appointment> => {
        const response = await api.patch(`/api/cita-medicas/${id}`, { id, estado: status }, {
            headers: { 'Content-Type': 'application/merge-patch+json' }
        });
        return response.data;
    },

    getAppointmentStats: async (userId: string) => {
        const today = new Date();
        const startOfDay = new Date(today.setHours(0, 0, 0, 0)).toISOString();

        const [totalToday, attendedToday, waitingToday] = await Promise.all([
            api.get('/api/cita-medicas/count', { params: { 'userId.equals': userId, 'fechaHora.greaterThanOrEqual': startOfDay } }),
            api.get('/api/cita-medicas/count', { params: { 'userId.equals': userId, 'fechaHora.greaterThanOrEqual': startOfDay, 'estado.equals': 'ATENDIDA' } }),
            api.get('/api/cita-medicas/count', { params: { 'userId.equals': userId, 'fechaHora.greaterThanOrEqual': startOfDay, 'estado.equals': 'TRIAGE_COMPLETADO' } })
        ]);

        return {
            totalToday: totalToday.data,
            attendedToday: attendedToday.data,
            waitingToday: waitingToday.data,
        };
    }
};
