import React, { useEffect, useState } from 'react';
import { AppointmentService, type Appointment } from '../../services/appointment.service';
import { useAuth } from '../../context/AuthContext';

interface AppointmentListProps {
    onNavigate?: (tab: string) => void;
}

const AppointmentList: React.FC<AppointmentListProps> = ({ onNavigate }) => {
    const { user } = useAuth();
    const [appointments, setAppointments] = useState<Appointment[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchTodayApts = async () => {
            const userId = user?.id;
            if (!userId) {
                setLoading(false);
                return;
            }

            setLoading(true);
            try {
                const data = await AppointmentService.getTodayAppointments(userId);
                // Tomar las primeras 5 para no saturar el panel lateral
                setAppointments(data.slice(0, 5));
            } catch (error) {
                console.error('Error fetching today appointments:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchTodayApts();
    }, [user?.id]);

    const getStatusColor = (status: string) => {
        switch (status) {
            case 'ATENDIDA': return 'bg-success/10 text-success';
            case 'PROGRAMADA': return 'bg-primary/20 text-primary animate-pulse';
            case 'CANCELADA': return 'bg-red-100 text-red-700';
            default: return 'bg-slate-100 text-slate-500';
        }
    };

    const formatTime = (dateStr: string) => {
        return new Date(dateStr).toLocaleTimeString('es-ES', {
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    return (
        <div className="p-6 border-t border-slate-100 dark:border-slate-800 transition-colors duration-300">
            <h3 className="text-slate-900 dark:text-white text-sm font-bold mb-4 flex justify-between items-center">
                <span className="flex items-center gap-2">
                    <span className="material-symbols-outlined text-primary">event_note</span>
                    Citas Médicas
                </span>
                <span className="text-[10px] text-slate-400 font-black tracking-widest uppercase">Hoy</span>
            </h3>

            <div className="flex flex-col gap-3">
                {loading ? (
                    <div className="animate-pulse space-y-3">
                        <div className="h-16 bg-slate-100 dark:bg-slate-800 rounded-lg"></div>
                        <div className="h-16 bg-slate-100 dark:bg-slate-800 rounded-lg"></div>
                    </div>
                ) : appointments.length > 0 ? (
                    appointments.map((apt) => (
                        <div
                            key={apt.id}
                            className={`p-3 rounded-lg border-l-4 transition-all hover:scale-[1.02] cursor-pointer border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800/50 hover:border-primary`}
                            onClick={() => onNavigate?.('Citas')}
                        >
                            <div className="flex justify-between items-start mb-1">
                                <p className="text-xs font-bold text-slate-500">
                                    {formatTime(apt.fechaHora)}
                                </p>
                                <span className={`px-1.5 py-0.5 text-[9px] font-black rounded uppercase tracking-tight ${getStatusColor(apt.estado)}`}>
                                    {apt.estado.replace('_', ' ')}
                                </span>
                            </div>
                            <p className="text-sm font-semibold text-slate-900 dark:text-white uppercase truncate">
                                {apt.paciente?.nombres} {apt.paciente?.apellidos}
                            </p>
                        </div>
                    ))
                ) : (
                    <div className="flex flex-col items-center justify-center py-8 px-4 bg-slate-50 dark:bg-slate-800/30 rounded-2xl border border-dashed border-slate-200 dark:border-slate-800">
                        <span className="material-symbols-outlined text-3xl text-slate-300 dark:text-slate-700 mb-2">
                            calendar_today
                        </span>
                        <p className="text-[10px] text-slate-500 font-black uppercase tracking-widest text-center">
                            Sin citas pendientes
                        </p>
                        <p className="text-[9px] text-slate-400 text-center mt-1 font-medium">
                            Tu agenda para hoy está despejada.
                        </p>
                    </div>
                )}
            </div>

            <button
                onClick={() => onNavigate?.('Citas')}
                className="w-full mt-6 py-2 border border-dashed border-slate-300 dark:border-slate-700 text-slate-500 text-[10px] font-black uppercase tracking-widest rounded-lg hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors"
            >
                Ver Agenda Completa
            </button>
        </div>
    );
};

export default AppointmentList;
