import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { type Appointment } from '../../../services/appointment.service';
import { useAuth } from '../../../context/AuthContext';
import { usePatients } from '../../../hooks/usePatients';
import { DataGrid, type Column } from '../../ui/DataGrid';
import { PatientCard } from '../../ui/PatientCard';
import { StatusBadge } from '../../ui/StatusBadge';
import { AppButton } from '../../ui/AppButton';

const StatCard = ({ label, value, icon, color }: any) => (
    <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl shadow-sm border border-slate-200 dark:border-slate-800 flex items-center gap-4 transition-all hover:shadow-md group">
        <div className={`${color} size-14 rounded-xl flex items-center justify-center text-white shadow-lg shadow-current/20 group-hover:scale-110 transition-transform`}>
            <span className="material-symbols-outlined">{icon}</span>
        </div>
        <div>
            <p className="text-slate-500 dark:text-slate-400 text-xs font-bold uppercase tracking-wider">{label}</p>
            <p className="text-3xl font-black text-slate-900 dark:text-white leading-none mt-1">{value}</p>
        </div>
    </div>
);

const DoctorHomeView = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    
    // Abstracted Fetching Logic
    const { waitingPatients, stats, isLoading, loadDashboardData } = usePatients(user?.id);

    useEffect(() => {
        loadDashboardData();
        const interval = setInterval(loadDashboardData, 30000);
        return () => clearInterval(interval);
    }, [loadDashboardData]);

    const handleAttend = (appointment: Appointment) => {
        try { localStorage.setItem('activeConsultation', String(appointment.id)); } catch (e) { }
        navigate(`/medico/consulta/${appointment.id}`);
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    };

    // Constructing DataGrid columns leveraging SRP
    const columns: Column<Appointment>[] = [
        {
            header: 'Hora Cita',
            id: 'time',
            cell: (item) => (
                <div className="flex flex-col">
                    <span className="text-sm font-black text-slate-900 dark:text-white">{formatDate(item.fechaHora)}</span>
                    <span className="text-xs text-amber-600 font-bold mt-1 px-2 py-0.5 w-fit bg-amber-50 rounded-md">Esperando atención</span>
                </div>
            )
        },
        {
            header: 'Paciente',
            id: 'patient',
            cell: (item) => (
                <PatientCard 
                    id={item.paciente?.id || 'N/A'}
                    fullName={`${item.paciente?.nombres || ''} ${item.paciente?.apellidos || ''}`.trim() || 'Paciente No Identificado'}
                    compact={true}
                />
            )
        },
        {
            header: 'Motivo de Consulta',
            id: 'reason',
            cell: (item) => (
                <div className="text-sm text-slate-600 dark:text-slate-300 max-w-[250px] line-clamp-2">
                    {item.observaciones || 'No se registró motivo especial'}
                </div>
            )
        },
        {
            header: 'Estado',
            id: 'status',
            cell: (item) => <StatusBadge status={item.estado || 'ESPERANDO_MEDICO'} />
        },
        {
            header: 'Acción',
            id: 'action',
            className: 'text-right',
            cell: (item) => (
                <div className="flex justify-end">
                    <AppButton 
                        icon="stethoscope" 
                        variant="primary" 
                        size="sm" 
                        onClick={(e) => { e.stopPropagation(); handleAttend(item); }}
                    >
                        Iniciar 
                    </AppButton>
                </div>
            )
        }
    ];

    if (isLoading && waitingPatients.length === 0) {
         // DataGrid internal loading handles the table structure
    }

    return (
        <div className="p-4 md:p-8 max-w-6xl mx-auto w-full flex flex-col gap-6 md:gap-8 transition-colors duration-300">
            {/* Header Area */}
            <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
                <div className="flex flex-col gap-1">
                    <h2 className="text-slate-900 dark:text-white text-3xl font-black tracking-tight">Panel Médico</h2>
                    <p className="text-slate-500 text-base font-medium">
                        Bienvenido, Dr. {user?.lastName || user?.firstName || 'Médico'}. Hoy es {new Date().toLocaleDateString('es-ES', { weekday: 'long', day: 'numeric', month: 'long' })}.
                    </p>
                </div>
            </div>

            {/* Stats Row */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 md:gap-6">
                <StatCard label="Pacientes en Espera" value={stats.waitingToday.toString()} icon="person_raised_hand" color="bg-amber-500" />
                <StatCard label="Atendidos Hoy" value={stats.attendedToday.toString()} icon="check_circle" color="bg-emerald-500" />
                <StatCard label="Total Agendados" value={stats.totalToday.toString()} icon="calendar_today" color="bg-sky-500" />
            </div>

            {/* Waiting List Section leveraging the new generic DataGrid */}
            <div className="bg-white dark:bg-slate-900 rounded-2xl shadow-sm border border-slate-200 dark:border-slate-800 overflow-hidden flex flex-col">
                <div className="p-6 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center bg-slate-50/50 dark:bg-slate-800/20">
                    <div className="flex items-center gap-3">
                        <div className="p-2 bg-amber-500/10 rounded-lg">
                            <span className="material-symbols-outlined text-amber-500">ward</span>
                        </div>
                        <h3 className="text-slate-900 dark:text-white font-bold text-lg">Sala de Espera - Post Triage</h3>
                    </div>
                    <div className="flex items-center gap-2 text-sm text-slate-500 font-medium">
                        <span className="relative flex h-3 w-3">
                            {waitingPatients.length > 0 && <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75"></span>}
                            <span className={`relative inline-flex rounded-full h-3 w-3 ${waitingPatients.length > 0 ? 'bg-amber-500' : 'bg-slate-300'}`}></span>
                        </span>
                        {waitingPatients.length} pacientes listos
                    </div>
                </div>

                <DataGrid 
                    data={waitingPatients}
                    columns={columns}
                    keyExtractor={(item) => item.id}
                    isLoading={isLoading}
                    emptyMessage={{
                        title: 'Excelente, estás al día',
                        description: 'No hay pacientes esperando en la sala de consulta en este momento.',
                        icon: 'coffee'
                    }}
                    onRowClick={handleAttend}
                />
            </div>
        </div>
    );
};

export default DoctorHomeView;
