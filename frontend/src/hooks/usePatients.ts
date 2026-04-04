import { useState, useCallback } from 'react';
import { AppointmentService, type Appointment } from '../services/appointment.service';

export function usePatients(userId: string | undefined) {
    const [waitingPatients, setWaitingPatients] = useState<Appointment[]>([]);
    const [stats, setStats] = useState({ totalToday: 0, attendedToday: 0, waitingToday: 0 });
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const loadDashboardData = useCallback(async () => {
        if (!userId) return;
        setIsLoading(true);
        setError(null);
        try {
            const [statData, waitingData] = await Promise.all([
                AppointmentService.getAppointmentStats(userId),
                AppointmentService.getTriageCompletedAppointments(userId)
            ]);
            setStats(statData);
            setWaitingPatients(waitingData);
        } catch (err: any) {
            console.error("Error fetching patient dashboard data:", err);
            setError(err.message || 'Error al cargar la sala de espera');
        } finally {
            setIsLoading(false);
        }
    }, [userId]);

    return {
        waitingPatients,
        stats,
        isLoading,
        error,
        loadDashboardData
    };
}
