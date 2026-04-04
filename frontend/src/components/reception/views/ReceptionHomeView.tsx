import { useState, useEffect } from 'react';
import { CitaService } from '../../../services/cita.service';
import { useNavigate } from 'react-router-dom';

interface KpiCardProps {
    icon: string;
    label: string;
    value: number | string;
    color: string;
    loading?: boolean;
}

const KpiCard = ({ icon, label, value, color, loading }: KpiCardProps) => (
    <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 p-6 flex items-center gap-5 shadow-sm hover:shadow-md transition-shadow">
        <div className={`w-14 h-14 rounded-2xl flex items-center justify-center shrink-0 ${color}`}>
            <span className="material-symbols-outlined text-white text-2xl">{icon}</span>
        </div>
        <div>
            {loading ? (
                <div className="h-8 w-16 bg-slate-100 dark:bg-slate-800 rounded-lg animate-pulse mb-1" />
            ) : (
                <p className="text-3xl font-black text-slate-900 dark:text-white leading-none">{value}</p>
            )}
            <p className="text-xs font-bold text-slate-500 uppercase tracking-widest mt-1">{label}</p>
        </div>
    </div>
);

interface QuickAction {
    icon: string;
    label: string;
    description: string;
    onClick: () => void;
    color: string;
}

const QuickActionCard = ({ icon, label, description, onClick, color }: QuickAction) => (
    <button
        onClick={onClick}
        className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 p-5 text-left hover:shadow-md hover:scale-[1.02] active:scale-[0.98] transition-all w-full group"
    >
        <div className={`w-10 h-10 rounded-xl flex items-center justify-center mb-3 ${color} group-hover:shadow-lg transition-shadow`}>
            <span className="material-symbols-outlined text-white">{icon}</span>
        </div>
        <p className="font-black text-slate-900 dark:text-white text-sm">{label}</p>
        <p className="text-xs text-slate-500 mt-0.5">{description}</p>
    </button>
);

interface AppointmentRow {
    id?: number;
    hora: string;
    paciente: string;
    medico: string;
    estado: string;
}

const ReceptionHomeView = () => {
    const navigate = useNavigate();
    const [kpis, setKpis] = useState({ total: 0, enEspera: 0, atendidas: 0, canceladas: 0 });
    const [citasHoy, setCitasHoy] = useState<AppointmentRow[]>([]);
    const [loadingKpis, setLoadingKpis] = useState(true);
    const [loadingCitas, setLoadingCitas] = useState(true);

    useEffect(() => {
        const loadData = async () => {
            const today = new Date();
            const startOfDay = new Date(today.setHours(0, 0, 0, 0)).toISOString();
            const endOfDay = new Date(today.setHours(23, 59, 59, 999)).toISOString();

            const baseParams = {
                'fechaHora.greaterThanOrEqual': startOfDay,
                'fechaHora.lessThanOrEqual': endOfDay,
            };

            try {
                // Load KPIs in parallel
                const [total, atendidas, canceladas] = await Promise.all([
                    CitaService.count(baseParams),
                    CitaService.count({ ...baseParams, 'estado.equals': 'ATENDIDA' }),
                    CitaService.count({ ...baseParams, 'estado.equals': 'CANCELADA' }),
                ]);
                setKpis({
                    total,
                    enEspera: total - atendidas - canceladas,
                    atendidas,
                    canceladas,
                });
            } catch (e) {
                console.error('Error loading KPIs', e);
            } finally {
                setLoadingKpis(false);
            }

            // Load today's next appointments (limited)
            try {
                const citas = await CitaService.getAll({
                    ...baseParams,
                    'estado.equals': 'PROGRAMADA',
                    sort: 'fechaHora,asc',
                    size: 8,
                });
                setCitasHoy(citas.map(c => ({
                    id: c.id,
                    hora: new Date(c.fechaHora).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
                    paciente: c.paciente?.nombres
                        ? `${c.paciente.nombres} ${c.paciente.apellidos || ''}`.trim()
                        : 'Sin nombre',
                    medico: c.user?.login || 'Sin asignar',
                    estado: c.estado,
                })));
            } catch (e) {
                console.error('Error loading citas', e);
            } finally {
                setLoadingCitas(false);
            }
        };

        loadData();
    }, []);

    const estadoBadge = (estado: string) => {
        switch (estado) {
            case 'PROGRAMADA': return 'bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400';
            case 'EN_SALA_ESPERA': return 'bg-amber-100 dark:bg-amber-900/30 text-amber-600 dark:text-amber-400';
            case 'ATENDIDA': return 'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400';
            case 'CANCELADA': return 'bg-red-100 dark:bg-red-900/30 text-red-500 dark:text-red-400';
            default: return 'bg-slate-100 dark:bg-slate-800 text-slate-500';
        }
    };

    return (
        <div className="p-6 md:p-8 space-y-8">
            {/* Header */}
            <div>
                <h2 className="text-2xl font-black text-slate-900 dark:text-white">Panel de Recepción</h2>
                <p className="text-slate-500 text-sm font-medium mt-1">
                    {new Date().toLocaleDateString('es-ES', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
                </p>
            </div>

            {/* KPI Cards */}
            <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
                <KpiCard icon="calendar_today" label="Citas de Hoy" value={kpis.total} color="bg-primary" loading={loadingKpis} />
                <KpiCard icon="hourglass_top" label="En Espera / Programadas" value={kpis.enEspera} color="bg-amber-500" loading={loadingKpis} />
                <KpiCard icon="check_circle" label="Atendidas" value={kpis.atendidas} color="bg-emerald-500" loading={loadingKpis} />
                <KpiCard icon="cancel" label="Canceladas" value={kpis.canceladas} color="bg-red-500" loading={loadingKpis} />
            </div>

            {/* Quick Actions */}
            <div>
                <h3 className="text-xs font-black text-slate-500 uppercase tracking-widest mb-3">Acciones Rápidas</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    <QuickActionCard
                        icon="person_add"
                        label="Registrar Paciente"
                        description="Nuevo ingreso al sistema"
                        color="bg-primary"
                        onClick={() => navigate('/recepcion/pacientes')}
                    />
                    <QuickActionCard
                        icon="calendar_add_on"
                        label="Nueva Cita"
                        description="Agendar una cita médica"
                        color="bg-violet-500"
                        onClick={() => navigate('/recepcion/citas?action=new')}
                    />
                    <QuickActionCard
                        icon="how_to_reg"
                        label="Check-In Paciente"
                        description="Registrar llegada del paciente"
                        color="bg-amber-500"
                        onClick={() => navigate('/recepcion/citas')}
                    />
                    <QuickActionCard
                        icon="search"
                        label="Buscar Paciente"
                        description="Localizar expediente"
                        color="bg-slate-600"
                        onClick={() => navigate('/recepcion/pacientes')}
                    />
                </div>
            </div>

            {/* Today's schedule */}
            <div>
                <div className="flex items-center justify-between mb-3">
                    <h3 className="text-xs font-black text-slate-500 uppercase tracking-widest">Agenda de Hoy (Próximas Citas)</h3>
                    <button
                        onClick={() => navigate('/recepcion/citas')}
                        className="text-xs font-black text-primary hover:underline"
                    >
                        Ver todo →
                    </button>
                </div>
                <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 overflow-hidden shadow-sm">
                    {loadingCitas ? (
                        <div className="p-6 space-y-3">
                            {Array(4).fill(0).map((_, i) => (
                                <div key={i} className="h-10 bg-slate-100 dark:bg-slate-800 rounded-xl animate-pulse" />
                            ))}
                        </div>
                    ) : citasHoy.length === 0 ? (
                        <div className="py-16 text-center text-slate-400">
                            <span className="material-symbols-outlined text-4xl block mb-2 opacity-40">event_busy</span>
                            <p className="text-sm font-bold">No hay citas programadas para hoy</p>
                        </div>
                    ) : (
                        <table className="w-full text-left min-w-[600px]">
                            <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 text-[10px] font-black uppercase tracking-widest">
                                <tr>
                                    <th className="px-6 py-3">Hora</th>
                                    <th className="px-6 py-3">Paciente</th>
                                    <th className="px-6 py-3">Médico</th>
                                    <th className="px-6 py-3">Estado</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                                {citasHoy.map((c, idx) => (
                                    <tr key={idx} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/30 transition-colors">
                                        <td className="px-6 py-3 font-black text-slate-900 dark:text-white text-sm">{c.hora}</td>
                                        <td className="px-6 py-3 text-sm text-slate-700 dark:text-slate-300 font-semibold">{c.paciente}</td>
                                        <td className="px-6 py-3 text-xs text-slate-500">{c.medico}</td>
                                        <td className="px-6 py-3">
                                            <span className={`px-2.5 py-1 rounded-lg text-[10px] font-black uppercase ${estadoBadge(c.estado)}`}>
                                                {c.estado === 'EN_SALA_ESPERA' ? 'En Sala' : c.estado}
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ReceptionHomeView;
