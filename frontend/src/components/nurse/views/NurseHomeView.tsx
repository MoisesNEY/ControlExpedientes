import { useState, useEffect } from 'react';
import { CitaService, type CitaMedicaDTO } from '../../../services/cita.service';
import { useNavigate } from 'react-router-dom';

const NurseHomeView = () => {
    const navigate = useNavigate();
    const [citasEnEspera, setCitasEnEspera] = useState<CitaMedicaDTO[]>([]);
    const [citasProgr, setCitasProgr] = useState<number>(0);
    const [loading, setLoading] = useState(true);
    const [lastRefresh, setLastRefresh] = useState<Date>(new Date());

    const loadData = async () => {
        setLoading(true);
        try {
            const today = new Date();
            const startOfDay = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 0, 0, 0).toISOString();
            const endOfDay = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 23, 59, 59).toISOString();
            const baseParams = {
                'fechaHora.greaterThanOrEqual': startOfDay,
                'fechaHora.lessThanOrEqual': endOfDay,
            };

            const [enEspera, programadas] = await Promise.all([
                CitaService.getAll({ ...baseParams, 'estado.equals': 'EN_SALA_ESPERA', sort: 'fechaHora,asc', size: 50 }),
                CitaService.count({ ...baseParams, 'estado.equals': 'PROGRAMADA' }),
            ]);
            setCitasEnEspera(enEspera);
            setCitasProgr(programadas);
            setLastRefresh(new Date());
        } catch (e) {
            console.error('Error cargando datos de enfermería', e);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadData();
        // Auto-refresh every 60 seconds so nurses see real-time check-ins
        const interval = setInterval(loadData, 60_000);
        return () => clearInterval(interval);
    }, []);

    const getWaitTime = (fechaHora: string) => {
        const diff = Math.floor((Date.now() - new Date(fechaHora).getTime()) / 60000);
        if (diff < 1) return 'Recién llegó';
        if (diff < 60) return `${diff} min en espera`;
        return `${Math.floor(diff / 60)}h ${diff % 60}m en espera`;
    };

    const getWaitColor = (fechaHora: string) => {
        const diff = Math.floor((Date.now() - new Date(fechaHora).getTime()) / 60000);
        if (diff < 15) return 'text-emerald-500';
        if (diff < 30) return 'text-amber-500';
        return 'text-red-500';
    };

    const getPriorityBadge = (idx: number) => {
        if (idx === 0) return <span className="px-2 py-0.5 bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400 rounded-full text-[10px] font-black uppercase">Siguiente</span>;
        return null;
    };

    return (
        <div className="p-6 md:p-8 space-y-8">
            {/* Header */}
            <div className="flex items-start justify-between">
                <div>
                    <h2 className="text-2xl font-black text-slate-900 dark:text-white">Panel de Enfermería</h2>
                    <p className="text-slate-500 text-sm font-medium mt-1">
                        {new Date().toLocaleDateString('es-ES', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
                    </p>
                </div>
                <button
                    onClick={loadData}
                    className="flex items-center gap-2 px-4 py-2 bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 rounded-xl text-sm font-bold text-slate-600 dark:text-slate-300 transition-colors"
                    title="Actualizar lista"
                >
                    <span className="material-symbols-outlined text-sm">refresh</span>
                    Actualizar
                </button>
            </div>

            {/* KPI cards */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 p-5 flex items-center gap-4 shadow-sm">
                    <div className="w-12 h-12 rounded-xl bg-amber-500 flex items-center justify-center shrink-0">
                        <span className="material-symbols-outlined text-white text-xl">hourglass_top</span>
                    </div>
                    <div>
                        <p className="text-3xl font-black text-slate-900 dark:text-white leading-none">
                            {loading ? <span className="inline-block h-8 w-10 bg-slate-100 dark:bg-slate-800 animate-pulse rounded" /> : citasEnEspera.length}
                        </p>
                        <p className="text-xs font-bold text-slate-500 uppercase tracking-widest mt-1">En Sala de Espera</p>
                    </div>
                </div>
                <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 p-5 flex items-center gap-4 shadow-sm">
                    <div className="w-12 h-12 rounded-xl bg-blue-500 flex items-center justify-center shrink-0">
                        <span className="material-symbols-outlined text-white text-xl">calendar_today</span>
                    </div>
                    <div>
                        <p className="text-3xl font-black text-slate-900 dark:text-white leading-none">
                            {loading ? <span className="inline-block h-8 w-10 bg-slate-100 dark:bg-slate-800 animate-pulse rounded" /> : citasProgr}
                        </p>
                        <p className="text-xs font-bold text-slate-500 uppercase tracking-widest mt-1">Aún Programadas</p>
                    </div>
                </div>
                <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 p-5 flex items-center gap-4 shadow-sm">
                    <div className="w-12 h-12 rounded-xl bg-slate-400 flex items-center justify-center shrink-0">
                        <span className="material-symbols-outlined text-white text-xl">schedule</span>
                    </div>
                    <div>
                        <p className="text-sm font-black text-slate-900 dark:text-white">
                            {lastRefresh.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </p>
                        <p className="text-xs font-bold text-slate-500 uppercase tracking-widest mt-1">Última actualización</p>
                    </div>
                </div>
            </div>

            {/* Patient queue */}
            <div>
                <h3 className="text-xs font-black text-slate-500 uppercase tracking-widest mb-3">
                    Cola de Pacientes — Listos para Triage Pre-clínico
                </h3>
                <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 overflow-hidden shadow-sm">
                    {loading ? (
                        <div className="p-6 space-y-3">
                            {Array(3).fill(0).map((_, i) => (
                                <div key={i} className="h-16 bg-slate-100 dark:bg-slate-800 rounded-xl animate-pulse" />
                            ))}
                        </div>
                    ) : citasEnEspera.length === 0 ? (
                        <div className="py-20 text-center">
                            <span className="material-symbols-outlined text-5xl text-slate-200 dark:text-slate-700 block mb-3">weekend</span>
                            <p className="font-black text-slate-400 text-sm">Sala de espera vacía</p>
                            <p className="text-slate-400 text-xs mt-1">Los pacientes aparecerán aquí cuando el recepcionista realice el Check-In.</p>
                        </div>
                    ) : (
                        <ul className="divide-y divide-slate-100 dark:divide-slate-800">
                            {citasEnEspera.map((c, idx) => (
                                <li key={c.id} className="flex items-center gap-4 px-6 py-4 hover:bg-slate-50/50 dark:hover:bg-slate-800/30 transition-colors group">
                                    {/* Order number */}
                                    <div className={`w-8 h-8 rounded-full flex items-center justify-center font-black text-sm shrink-0 ${idx === 0 ? 'bg-primary text-white' : 'bg-slate-100 dark:bg-slate-800 text-slate-500'}`}>
                                        {idx + 1}
                                    </div>
                                    {/* Patient info */}
                                    <div className="flex-1 min-w-0">
                                        <div className="flex items-center gap-2 flex-wrap">
                                            <p className="font-black text-slate-900 dark:text-white text-sm truncate">
                                                {c.paciente?.nombres ? `${c.paciente.nombres} ${c.paciente.apellidos || ''}`.trim() : 'Paciente sin nombre'}
                                            </p>
                                            {getPriorityBadge(idx)}
                                        </div>
                                        <p className={`text-xs font-bold mt-0.5 ${getWaitColor(c.fechaHora)}`}>
                                            {getWaitTime(c.fechaHora)}
                                        </p>
                                    </div>
                                    {/* Scheduled time */}
                                    <div className="hidden sm:block text-right shrink-0">
                                        <p className="text-sm font-black text-slate-900 dark:text-white">
                                            {new Date(c.fechaHora).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                        </p>
                                        <p className="text-[10px] text-slate-400 font-bold uppercase">Hora cita</p>
                                    </div>
                                    {/* Triage button */}
                                    <button
                                        onClick={() => navigate(`/enfermeria/triage/${c.id}`)}
                                        className="flex items-center gap-2 px-4 py-2 bg-primary text-white rounded-xl font-bold text-xs hover:scale-105 active:scale-95 transition-all shadow-md shadow-primary/20 shrink-0"
                                    >
                                        <span className="material-symbols-outlined text-sm">vital_signs</span>
                                        <span className="hidden sm:inline">Iniciar Triage</span>
                                        <span className="sm:hidden">Triage</span>
                                    </button>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </div>
        </div>
    );
};

export default NurseHomeView;
