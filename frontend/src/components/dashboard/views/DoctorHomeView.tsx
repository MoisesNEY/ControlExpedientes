import { useEffect, useState } from 'react';
import { usePatient } from '../../../context/PatientContext';
import { AppointmentService, type Appointment } from '../../../services/appointment.service';
import { useAuth } from '../../../context/AuthContext';

const StatCard = ({ label, value, icon, color }: any) => (
    <div className="bg-white dark:bg-slate-900 p-5 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800 flex items-center gap-4 transition-colors">
        <div className={`${color} size-12 rounded-lg flex items-center justify-center text-white shadow-lg shadow-current/20`}>
            <span className="material-symbols-outlined">{icon}</span>
        </div>
        <div>
            <p className="text-slate-500 dark:text-slate-400 text-xs font-bold uppercase tracking-wider">{label}</p>
            <p className="text-2xl font-black text-slate-900 dark:text-white leading-none mt-1">{value}</p>
        </div>
    </div>
);

const DoctorHomeView = ({ onNavigate }: { onNavigate?: (tab: string) => void }) => {
    const { selectPatient } = usePatient();
    const { user } = useAuth();
    const [stats, setStats] = useState({ totalToday: 0, attendedToday: 0 });
    const [appointments, setAppointments] = useState<Appointment[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            if (!user?.id) return;
            setLoading(true);
            try {
                const [statData, appointmentData] = await Promise.all([
                    AppointmentService.getAppointmentStats(user.id),
                    AppointmentService.getTodayAppointments(user.id)
                ]);
                setStats(statData);
                setAppointments(appointmentData);
            } catch (error) {
                console.error("Error fetching dashboard data:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [user?.id]);

    const handleAttend = (appointment: Appointment) => {
        const paciente = appointment.paciente;
        if (!paciente) return;

        // Map backend patient to frontend PatientContext structure
        selectPatient({
            id: `PX-${paciente.id}`,
            name: `${paciente.nombres || ''} ${paciente.apellidos || ''}`.trim() || 'N/A',
            age: 'N/A', // Age is unfortunately not in the minimal appointment payload right now
            gender: 'N/A',
            status: 'Activo',
            image: `https://i.pravatar.cc/150?u=${paciente.nombres}`,
            appointmentId: appointment.id
        });
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    };

    if (loading) {
        return (
            <div className="p-8 flex items-center justify-center h-full">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
            </div>
        );
    }

    return (
        <div className="p-4 md:p-8 max-w-5xl mx-auto w-full flex flex-col gap-6 md:gap-8 transition-colors duration-300">
            {/* Bienvenida */}
            <div className="flex flex-col gap-1">
                <h2 className="text-slate-900 dark:text-white text-2xl md:text-3xl font-black tracking-tight">¡Buen día, {user?.firstName || 'Doctor'}!</h2>
                <p className="text-slate-500 text-sm md:text-base font-medium tracking-tight">Aquí tienes el resumen de tu jornada para hoy, {new Date().toLocaleDateString('es-ES', { day: '2-digit', month: 'long' })}.</p>
            </div>

            {/* Estadísticas Rápidas */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 md:gap-6">
                <StatCard label="Citas Hoy" value={stats.totalToday.toString()} icon="calendar_month" color="bg-primary" />
                <StatCard label="Pacientes Atendidos" value={stats.attendedToday.toString()} icon="check_circle" color="bg-success" />
                <StatCard label="Nuevos Expedientes" value="-" icon="person_add" color="bg-orange-500" />
            </div>

            {/* Agenda Detallada */}
            <div className="bg-white dark:bg-slate-900 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800 overflow-hidden">
                <div className="p-6 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center transition-colors">
                    <h3 className="text-slate-900 dark:text-white font-bold flex items-center gap-2">
                        <span className="material-symbols-outlined text-primary">list_alt</span>
                        Agenda del Día
                    </h3>
                    <button
                        onClick={() => onNavigate?.('Citas')}
                        className="text-xs font-bold text-primary hover:underline transition-all"
                    >
                        Ver calendario completo
                    </button>
                </div>
                <div className="overflow-x-auto scrollbar-hide">
                    <table className="w-full text-left min-w-[600px]">
                        <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 uppercase text-[10px] font-black tracking-widest">
                            <tr>
                                <th className="px-6 py-4">Hora</th>
                                <th className="px-6 py-4">Paciente</th>
                                <th className="px-6 py-4">Motivo</th>
                                <th className="px-6 py-4">Estado</th>
                                <th className="px-6 py-4 text-right">Acción</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                            {appointments.map((item) => (
                                <tr key={item.id} className={`group hover:bg-slate-50 dark:hover:bg-slate-800/30 transition-colors ${item.estado === 'PROGRAMADA' ? 'bg-primary/5' : ''}`}>
                                    <td className="px-6 py-4 text-sm font-bold text-slate-900 dark:text-white">{formatDate(item.fechaHora)}</td>
                                    <td className="px-6 py-4">
                                        <div className="flex items-center gap-2">
                                            <div className="size-8 rounded-full bg-slate-200 dark:bg-slate-700 overflow-hidden shadow-inner">
                                                <img src={`https://i.pravatar.cc/150?u=${item.paciente?.nombres || item.id}`} alt="" />
                                            </div>
                                            <span className="text-sm font-semibold text-slate-700 dark:text-slate-300 transition-colors">
                                                {item.paciente?.nombres} {item.paciente?.apellidos}
                                            </span>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 text-sm text-slate-500 dark:text-slate-400 transition-colors truncate max-w-[200px]">{item.observaciones || 'Sin observaciones'}</td>
                                    <td className="px-6 py-4">
                                        <span className={`px-2 py-1 rounded-md text-[10px] font-black uppercase tracking-tight ${item.estado === 'ATENDIDA' ? 'bg-success/10 text-success' :
                                            item.estado === 'PROGRAMADA' ? 'bg-primary/20 text-primary animate-pulse' :
                                                'bg-slate-100 dark:bg-slate-800 text-slate-500'
                                            }`}>
                                            {item.estado.replace('_', ' ')}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 text-right">
                                        <button
                                            onClick={() => handleAttend(item)}
                                            className={`px-4 py-1.5 rounded-lg text-xs font-bold transition-all ${item.estado === 'PROGRAMADA' ? 'bg-primary text-white shadow-lg shadow-primary/30 hover:scale-105' : 'text-primary hover:bg-primary/10'
                                                }`}
                                        >
                                            {item.estado === 'PROGRAMADA' ? 'Atender' : 'Ver Detalles'}
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            {appointments.length === 0 && (
                                <tr>
                                    <td colSpan={5} className="px-6 py-10 text-center text-slate-400 italic">No hay citas programadas para hoy.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default DoctorHomeView;
