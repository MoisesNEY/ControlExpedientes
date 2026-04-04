import { useState, useEffect } from 'react';
import { AppointmentService, type Appointment } from '../../../services/appointment.service';
import { useAuth } from '../../../context/AuthContext';
import { usePatient } from '../../../context/PatientContext';
import Avatar from '../../ui/Avatar';
import { PacienteService } from '../../../services/paciente.service';
import { useNavigate } from 'react-router-dom';

const AppointmentView = () => {
    const { user } = useAuth();
    const { selectPatient, updateSelectedPatient } = usePatient();
    const navigate = useNavigate();
    const [appointments, setAppointments] = useState<Appointment[]>([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState<'HOY' | 'TODAS' | 'ATENDIDAS' | 'PENDIENTES'>('HOY');
    const [searchTerm, setSearchTerm] = useState('');

    const calculateAge = (birthday?: string) => {
        if (!birthday) return 'N/A';
        const b = new Date(birthday);
        if (Number.isNaN(b.getTime())) return 'N/A';
        const today = new Date();
        let years = today.getFullYear() - b.getFullYear();
        const m = today.getMonth() - b.getMonth();
        if (m < 0 || (m === 0 && today.getDate() < b.getDate())) years--;
        return `${years} años`;
    };

    const mapSexo = (sexo?: string) => {
        if (!sexo) return 'N/A';
        if (sexo === 'MASCULINO') return 'Masculino';
        if (sexo === 'FEMENINO') return 'Femenino';
        return 'Otro';
    };

    useEffect(() => {
        const fetchAppointments = async () => {
            if (!user?.id) return;
            setLoading(true);

            try {
                // HOY muestra agenda del día, el resto muestra histórico/recientes
                const data = filter === 'HOY'
                    ? await AppointmentService.getTodayAppointments(user.id)
                    : await AppointmentService.getRecentAppointments(user.id);

                let filtered = data;

                if (filter === 'ATENDIDAS') {
                    filtered = data.filter(a => a.estado === 'ATENDIDA');
                } else if (filter === 'PENDIENTES') {
                    filtered = data.filter(a => a.estado === 'ESPERANDO_MEDICO');
                }

                if (searchTerm) {
                    filtered = filtered.filter(a =>
                        `${a.paciente?.nombres || ''} ${a.paciente?.apellidos || ''}`.toLowerCase().includes(searchTerm.toLowerCase())
                    );
                }

                setAppointments(filtered);
            } catch (error) {
                console.error('Error fetching appointments:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchAppointments();
    }, [user?.id, filter, searchTerm]);

    const handleAttend = (appointment: Appointment) => {
        const pacienteId = appointment.paciente?.id;
        const name = `${appointment.paciente?.nombres || ''} ${appointment.paciente?.apellidos || ''}`.trim() || 'Paciente';

        if (!pacienteId) return;

        selectPatient({
            id: `PX-${pacienteId}`,
            patientId: pacienteId,
            name,
            age: 'N/A',
            gender: 'N/A',
            status: 'Activo',
            appointmentId: appointment.id
        });

        // Persistir consulta activa para poder reanudar si salimos
        if (appointment.id) {
            try { localStorage.setItem('activeConsultation', String(appointment.id)); } catch (e) { /* silent */ }
            navigate(`/medico/consulta/${appointment.id}`);
        }

        // Cargar información completa del paciente en background
        void (async () => {
            try {
                const full = await PacienteService.getById(pacienteId);
                updateSelectedPatient({
                    name: `${full.nombres} ${full.apellidos}`.trim() || name,
                    age: calculateAge(full.fechaNacimiento),
                    gender: mapSexo(full.sexo),
                    status: full.activo ? 'Activo' : 'Inactivo',
                });
            } catch {
                // ignore
            }
        })();
    };

    const handleViewRecord = (appointment: Appointment) => {
        const pacienteId = appointment.paciente?.id;
        const name = `${appointment.paciente?.nombres || ''} ${appointment.paciente?.apellidos || ''}`.trim() || 'Paciente';

        if (!pacienteId) return;

        selectPatient({
            id: `PX-${pacienteId}`,
            patientId: pacienteId,
            name,
            age: 'N/A',
            gender: 'N/A',
            status: 'Activo',
            appointmentId: appointment.id
        });

        navigate('/medico/registros');
    };

    const formatDate = (dateString: string) => {
        const date = new Date(dateString);
        return {
            time: date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
            date: date.toLocaleDateString('es-ES', { day: '2-digit', month: 'short' })
        };
    };

    return (
        <div className="p-4 md:p-8 space-y-4 md:space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h2 className="text-xl md:text-2xl font-black text-slate-900 dark:text-white">Agenda de Citas</h2>
                    <p className="text-slate-500 text-xs md:text-sm font-medium">Gestiona tus consultas en espera y pacientes programados.</p>
                </div>
            </div>

            <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden">
                <div className="p-4 md:p-6 border-b border-slate-100 dark:border-slate-800 flex flex-col md:flex-row gap-4 items-center justify-between">
                    <div className="flex bg-slate-100 dark:bg-slate-800 p-1 rounded-xl w-full md:w-auto overflow-x-auto scrollbar-hide">
                        {(['HOY', 'TODAS', 'PENDIENTES', 'ATENDIDAS'] as const).map((f) => (
                            <button
                                key={f}
                                onClick={() => setFilter(f)}
                                className={`flex-1 md:flex-none px-4 py-2 rounded-lg text-xs font-bold transition-all whitespace-nowrap ${filter === f
                                    ? 'bg-white dark:bg-slate-700 text-primary shadow-sm'
                                    : 'text-slate-500 hover:text-slate-700 dark:hover:text-slate-300'
                                    }`}
                            >
                                {f}
                            </button>
                        ))}
                    </div>

                    <div className="relative w-full md:w-72">
                        <span className="absolute left-3 top-1/2 -translate-y-1/2 material-symbols-outlined text-slate-400 text-sm">search</span>
                        <input
                            type="text"
                            placeholder="Buscar paciente..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full pl-10 pr-4 py-2 bg-slate-50 dark:bg-slate-800 border-none rounded-xl text-xs outline-none focus:ring-2 focus:ring-primary transition-all"
                        />
                    </div>
                </div>

                <div className="overflow-x-auto scrollbar-hide">
                    <table className="w-full text-left min-w-[800px]">
                        <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 text-[10px] font-black uppercase tracking-widest">
                            <tr>
                                <th className="px-6 py-4">Fecha y Hora</th>
                                <th className="px-6 py-4">Paciente</th>
                                <th className="px-6 py-4">Motivo / Notas</th>
                                <th className="px-6 py-4">Estado</th>
                                <th className="px-6 py-4 text-right">Acciones</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                            {loading ? (
                                Array(3).fill(0).map((_, i) => (
                                    <tr key={i} className="animate-pulse">
                                        <td colSpan={5} className="px-6 py-8"><div className="h-4 bg-slate-100 dark:bg-slate-800 rounded w-full"></div></td>
                                    </tr>
                                ))
                            ) : (
                                appointments.map(a => {
                                    const { time, date } = formatDate(a.fechaHora);
                                    return (
                                        <tr key={a.id} className="group hover:bg-slate-50/50 dark:hover:bg-slate-800/50 transition-colors">
                                            <td className="px-6 py-4">
                                                <div className="flex flex-col">
                                                    <span className="text-sm font-bold text-slate-900 dark:text-white">{time}</span>
                                                    <span className="text-[10px] text-slate-400 font-bold uppercase">{date}</span>
                                                </div>
                                            </td>
                                            <td className="px-6 py-4">
                                                <div className="flex items-center gap-3">
                                                    <Avatar
                                                        name={`${a.paciente?.nombres || ''} ${a.paciente?.apellidos || ''}`.trim() || 'Paciente'}
                                                        size="sm"
                                                    />
                                                    <span className="text-sm font-semibold text-slate-700 dark:text-slate-300">
                                                        {a.paciente?.nombres} {a.paciente?.apellidos}
                                                    </span>
                                                </div>
                                            </td>
                                            <td className="px-6 py-4 text-xs text-slate-500 dark:text-slate-400 max-w-xs truncate">
                                                {a.observaciones || 'Sin motivo especificado'}
                                            </td>
                                            <td className="px-6 py-4">
                                                <span className={`px-2 py-1 rounded-lg text-[10px] font-black uppercase ${a.estado === 'ATENDIDA' ? 'bg-success/10 text-success' :
                                                    a.estado === 'ESPERANDO_MEDICO' ? 'bg-primary/10 text-primary animate-pulse' :
                                                        a.estado === 'PROGRAMADA' ? 'bg-blue-100 text-blue-600' :
                                                            'bg-slate-100 dark:bg-slate-800 text-slate-500'
                                                    }`}>
                                                    {a.estado.replace('_', ' ')}
                                                </span>
                                            </td>
                                            <td className="px-6 py-4 text-right">
                                                {a.estado === 'ESPERANDO_MEDICO' ? (
                                                    <button
                                                        onClick={() => handleAttend(a)}
                                                        className="px-4 py-2 bg-primary text-white rounded-xl text-xs font-black uppercase shadow-lg shadow-primary/20 hover:scale-105 transition-all"
                                                    >
                                                        Atender
                                                    </button>
                                                ) : (
                                                    <button onClick={() => handleViewRecord(a)} className="text-slate-400 hover:text-primary transition-colors">
                                                        <span className="material-symbols-outlined text-sm">visibility</span>
                                                    </button>
                                                )}
                                            </td>
                                        </tr>
                                    );
                                })
                            )}
                            {!loading && appointments.length === 0 && (
                                <tr>
                                    <td colSpan={5} className="px-6 py-20 text-center text-slate-400 italic font-medium transition-colors">
                                        No se encontraron citas con los filtros seleccionados.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default AppointmentView;
