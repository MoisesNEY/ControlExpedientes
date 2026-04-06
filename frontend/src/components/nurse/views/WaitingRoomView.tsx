import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { CitaService, type CitaMedicaDTO } from '../../../services/cita.service';
import { StatusBadge } from '../../ui/StatusBadge';
import { AppButton } from '../../ui/AppButton';
import Avatar from '../../ui/Avatar';

const WAITING_STATES: CitaMedicaDTO['estado'][] = ['EN_SALA_ESPERA', 'EN_TRIAGE', 'ESPERANDO_MEDICO'];
const REFRESH_INTERVAL = 30_000;

function formatTime(iso: string): string {
    const d = new Date(iso);
    return d.toLocaleTimeString('es-HN', { hour: '2-digit', minute: '2-digit', hour12: true });
}

function minutesSince(iso: string): number {
    return Math.max(0, Math.floor((Date.now() - new Date(iso).getTime()) / 60_000));
}

function waitLabel(minutes: number): string {
    if (minutes < 60) return `${minutes} min`;
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    return `${h}h ${m}m`;
}

function waitColor(minutes: number): string {
    if (minutes < 15) return 'text-emerald-500';
    if (minutes <= 30) return 'text-amber-500';
    return 'text-rose-500';
}

function waitBg(minutes: number): string {
    if (minutes < 15) return 'bg-emerald-50 dark:bg-emerald-500/10';
    if (minutes <= 30) return 'bg-amber-50 dark:bg-amber-500/10';
    return 'bg-rose-50 dark:bg-rose-500/10';
}

function patientName(cita: CitaMedicaDTO): string {
    const p = cita.paciente;
    if (!p) return 'Paciente';
    return [p.nombres, p.apellidos].filter(Boolean).join(' ') || 'Paciente';
}

const WaitingRoomView = () => {
    const navigate = useNavigate();
    const [citas, setCitas] = useState<CitaMedicaDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [, setTick] = useState(0);

    const fetchCitas = useCallback(async () => {
        try {
            const all = await Promise.all(
                WAITING_STATES.map(estado =>
                    CitaService.getAll({ 'estado.equals': estado, sort: 'fechaHora,asc' }),
                ),
            );
            setCitas(all.flat());
        } catch {
            /* keep previous data */
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchCitas();
        const dataTimer = setInterval(fetchCitas, REFRESH_INTERVAL);
        // tick every 60 s to refresh "time ago" labels
        const tickTimer = setInterval(() => setTick(t => t + 1), 60_000);
        return () => {
            clearInterval(dataTimer);
            clearInterval(tickTimer);
        };
    }, [fetchCitas]);

    const enEspera = citas.filter(c => c.estado === 'EN_SALA_ESPERA');
    const enTriage = citas.filter(c => c.estado === 'EN_TRIAGE');
    const listos = citas.filter(c => c.estado === 'ESPERANDO_MEDICO');

    /* ------------------------------------------------------------------ */
    /* Summary cards config                                                */
    /* ------------------------------------------------------------------ */
    const cards: { label: string; count: number; icon: string; border: string; bg: string; text: string; iconBg: string }[] = [
        {
            label: 'En Espera',
            count: enEspera.length,
            icon: 'airline_seat_recline_normal',
            border: 'border-amber-200 dark:border-amber-900/60',
            bg: 'bg-amber-50/80 dark:bg-amber-950/30',
            text: 'text-amber-600 dark:text-amber-400',
            iconBg: 'bg-amber-500 shadow-amber-500/30',
        },
        {
            label: 'En Triage',
            count: enTriage.length,
            icon: 'monitor_heart',
            border: 'border-violet-200 dark:border-violet-900/60',
            bg: 'bg-violet-50/80 dark:bg-violet-950/30',
            text: 'text-violet-600 dark:text-violet-400',
            iconBg: 'bg-violet-500 shadow-violet-500/30',
        },
        {
            label: 'Listos para Médico',
            count: listos.length,
            icon: 'check_circle',
            border: 'border-emerald-200 dark:border-emerald-900/60',
            bg: 'bg-emerald-50/80 dark:bg-emerald-950/30',
            text: 'text-emerald-600 dark:text-emerald-400',
            iconBg: 'bg-emerald-500 shadow-emerald-500/30',
        },
    ];

    /* ------------------------------------------------------------------ */
    /* Render                                                              */
    /* ------------------------------------------------------------------ */
    return (
        <div className="p-4 md:p-8 max-w-7xl mx-auto w-full flex flex-col gap-6 md:gap-8 transition-colors duration-300">
            {/* ---- Header ---- */}
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h1 className="text-2xl md:text-4xl font-black text-slate-900 dark:text-white tracking-tight">
                        Sala de Espera
                    </h1>
                    <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
                        Seguimiento de pacientes en proceso de atención
                    </p>
                </div>

                <AppButton
                    icon="refresh"
                    onClick={() => { setLoading(true); fetchCitas(); }}
                    className="bg-amber-500 text-white hover:bg-amber-600 shadow-lg shadow-amber-500/25"
                    size="sm"
                >
                    Actualizar
                </AppButton>
            </div>

            {/* ---- Summary cards ---- */}
            <div className="grid gap-4 sm:grid-cols-3">
                {cards.map(c => (
                    <div
                        key={c.label}
                        className={`rounded-2xl border ${c.border} ${c.bg} px-5 py-4 flex items-center gap-4 transition-all`}
                    >
                        <div className={`size-12 rounded-2xl flex items-center justify-center text-white shadow-lg ${c.iconBg}`}>
                            <span className="material-symbols-outlined text-[22px]">{c.icon}</span>
                        </div>
                        <div>
                            <p className="text-[11px] font-black uppercase tracking-[0.22em] text-slate-500 dark:text-slate-400">
                                {c.label}
                            </p>
                            <p className={`text-3xl font-black leading-none mt-0.5 ${c.text}`}>{c.count}</p>
                        </div>
                    </div>
                ))}
            </div>

            {/* ---- Table ---- */}
            <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden">
                {/* Table header bar */}
                <div className="p-4 md:p-6 border-b border-slate-100 dark:border-slate-800 flex items-center gap-3">
                    <span className="material-symbols-outlined text-amber-500 text-[22px]">groups</span>
                    <h2 className="font-bold text-slate-900 dark:text-white text-sm md:text-base">
                        Pacientes en sala
                    </h2>
                    <span className="ml-auto text-[11px] font-bold text-slate-400 dark:text-slate-500 uppercase tracking-wider">
                        {citas.length} paciente{citas.length !== 1 ? 's' : ''}
                    </span>
                </div>

                {loading ? (
                    <div className="px-6 py-20 text-center">
                        <span className="material-symbols-outlined animate-spin text-slate-400 text-3xl">progress_activity</span>
                        <p className="text-sm text-slate-400 mt-3 font-medium">Cargando pacientes…</p>
                    </div>
                ) : citas.length === 0 ? (
                    <div className="px-6 py-20 text-center">
                        <span className="material-symbols-outlined text-slate-300 dark:text-slate-600 text-5xl">
                            event_available
                        </span>
                        <p className="text-sm text-slate-400 italic font-medium mt-3">
                            No hay pacientes en la sala de espera
                        </p>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left">
                            <thead>
                                <tr className="bg-slate-50 dark:bg-slate-800/50">
                                    {['Hora', 'Paciente', 'Estado', 'Tiempo de Espera', 'Acciones'].map(h => (
                                        <th
                                            key={h}
                                            className="px-4 md:px-6 py-3 text-[10px] font-black uppercase tracking-widest text-slate-500 dark:text-slate-400"
                                        >
                                            {h}
                                        </th>
                                    ))}
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                                {citas.map(cita => {
                                    const mins = minutesSince(cita.fechaHora);
                                    const name = patientName(cita);

                                    return (
                                        <tr
                                            key={cita.id}
                                            className="group hover:bg-slate-50/50 dark:hover:bg-slate-800/50 transition-colors"
                                        >
                                            {/* Hora */}
                                            <td className="px-4 md:px-6 py-4 whitespace-nowrap">
                                                <span className="text-xs font-semibold text-slate-700 dark:text-slate-300">
                                                    {formatTime(cita.fechaHora)}
                                                </span>
                                            </td>

                                            {/* Paciente */}
                                            <td className="px-4 md:px-6 py-4 whitespace-nowrap">
                                                <div className="flex items-center gap-3">
                                                    <Avatar name={name} size="sm" />
                                                    <span className="text-sm font-semibold text-slate-900 dark:text-white">
                                                        {name}
                                                    </span>
                                                </div>
                                            </td>

                                            {/* Estado */}
                                            <td className="px-4 md:px-6 py-4 whitespace-nowrap">
                                                <StatusBadge status={cita.estado} />
                                            </td>

                                            {/* Tiempo de Espera */}
                                            <td className="px-4 md:px-6 py-4 whitespace-nowrap">
                                                <span
                                                    className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-xs font-bold ${waitColor(mins)} ${waitBg(mins)}`}
                                                >
                                                    <span className="material-symbols-outlined text-[14px]">schedule</span>
                                                    {waitLabel(mins)}
                                                </span>
                                            </td>

                                            {/* Acciones */}
                                            <td className="px-4 md:px-6 py-4 whitespace-nowrap">
                                                {cita.estado === 'EN_SALA_ESPERA' && (
                                                    <AppButton
                                                        size="sm"
                                                        icon="vital_signs"
                                                        onClick={() => navigate(`/enfermeria/triage/${cita.id}`)}
                                                        className="bg-amber-500 text-white hover:bg-amber-600 shadow-lg shadow-amber-500/25"
                                                    >
                                                        Iniciar Triage
                                                    </AppButton>
                                                )}
                                                {cita.estado === 'EN_TRIAGE' && (
                                                    <AppButton
                                                        size="sm"
                                                        icon="edit_note"
                                                        onClick={() => navigate(`/enfermeria/triage/${cita.id}`)}
                                                        className="bg-violet-500 text-white hover:bg-violet-600 shadow-lg shadow-violet-500/25"
                                                    >
                                                        Continuar Triage
                                                    </AppButton>
                                                )}
                                            </td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
};

export default WaitingRoomView;
