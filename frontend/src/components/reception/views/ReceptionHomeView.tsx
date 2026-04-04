import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Bar,
    BarChart,
    CartesianGrid,
    Cell,
    Legend,
    Line,
    LineChart,
    Pie,
    PieChart,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from 'recharts';
import { DashboardEmptyState, DashboardLoading, DashboardMetricCard, DashboardPanel } from '../../analytics/DashboardPrimitives';
import { DashboardService, type DashboardMetrics } from '../../../services/dashboard.service';
import { AppButton } from '../../ui/AppButton';

const CARD_ICONS: Record<string, string> = {
    citasHoy: 'calendar_today',
    checkIn: 'how_to_reg',
    enAtencion: 'local_hospital',
    pacientesActivos: 'groups',
};
const CARD_ACCENTS = ['bg-sky-500', 'bg-amber-500', 'bg-violet-500', 'bg-emerald-500'];
const STATUS_COLORS = ['#3b82f6', '#f59e0b', '#8b5cf6', '#14b8a6', '#22c55e', '#ef4444', '#64748b'];
const DOCTOR_COLORS = ['#0ea5e9', '#f97316', '#8b5cf6', '#10b981', '#f43f5e', '#14b8a6'];

const getCardValue = (metrics: DashboardMetrics, key: string) => Number(metrics.cards.find(card => card.key === key)?.value ?? 0);

const formatRatio = (value: number, total: number) => {
    if (total <= 0) return '0%';
    return `${Math.round((value / total) * 100)}%`;
};

const ReceptionHomeView = () => {
    const navigate = useNavigate();
    const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const loadDashboard = async () => {
            setLoading(true);
            try {
                setMetrics(await DashboardService.getReceptionDashboard());
            } catch (error) {
                console.error('Error fetching reception dashboard:', error);
            } finally {
                setLoading(false);
            }
        };

        loadDashboard();
        const interval = setInterval(loadDashboard, 60000);
        return () => clearInterval(interval);
    }, []);

    if (loading) {
        return <DashboardLoading />;
    }

    if (!metrics) {
        return <DashboardEmptyState message="No se pudieron cargar las métricas del panel de recepción." />;
    }

    const citasHoy = getCardValue(metrics, 'citasHoy');
    const checkIn = getCardValue(metrics, 'checkIn');
    const enAtencion = getCardValue(metrics, 'enAtencion');
    const pacientesActivos = getCardValue(metrics, 'pacientesActivos');
    const nextAppointment = metrics.queue[0] ?? metrics.spotlight ?? null;
    const busiestDoctor = metrics.tertiarySeries.reduce<{ label: string; value: number } | null>((current, entry) => {
        if (!current || entry.value > current.value) {
            return { label: entry.label, value: entry.value };
        }
        return current;
    }, null);

    return (
        <div className="p-4 md:p-8 max-w-7xl mx-auto w-full flex flex-col gap-6 md:gap-8 transition-colors duration-300">
            <div className="relative overflow-hidden rounded-[32px] border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 md:p-8 shadow-sm">
                <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(14,165,233,0.12),transparent_32%),radial-gradient(circle_at_bottom_right,rgba(20,184,166,0.1),transparent_28%)] dark:bg-[radial-gradient(circle_at_top_left,rgba(14,165,233,0.18),transparent_32%),radial-gradient(circle_at_bottom_right,rgba(20,184,166,0.12),transparent_28%)]" />
                <div className="relative grid gap-8 xl:grid-cols-[1.45fr_0.95fr] xl:items-start">
                    <div className="space-y-5">
                        <div>
                            <p className="text-xs font-black uppercase tracking-[0.32em] text-sky-600 dark:text-sky-300">Front desk overview</p>
                            <h2 className="mt-3 text-3xl md:text-4xl font-black tracking-tight text-slate-900 dark:text-white">Dashboard de Recepción</h2>
                            <p className="mt-3 max-w-2xl text-sm md:text-base text-slate-600 dark:text-slate-300">
                                Control en vivo de agenda, check-in y distribución de carga para sostener un ingreso fluido.
                            </p>
                        </div>

                        <div className="grid gap-3 sm:grid-cols-3">
                            <div className="rounded-2xl border border-sky-100 dark:border-sky-900/60 bg-sky-50/80 dark:bg-sky-950/30 px-4 py-3">
                                <p className="text-[11px] font-black uppercase tracking-[0.22em] text-sky-700 dark:text-sky-300">Check-in completado</p>
                                <p className="mt-2 text-2xl font-black text-slate-900 dark:text-white">{formatRatio(checkIn, citasHoy || checkIn)}</p>
                                <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">Citas del día que ya pasaron por recepción.</p>
                            </div>
                            <div className="rounded-2xl border border-teal-100 dark:border-teal-900/60 bg-teal-50/80 dark:bg-teal-950/30 px-4 py-3">
                                <p className="text-[11px] font-black uppercase tracking-[0.22em] text-teal-700 dark:text-teal-300">Pacientes activos</p>
                                <p className="mt-2 text-2xl font-black text-slate-900 dark:text-white">{pacientesActivos}</p>
                                <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">Pacientes con actividad en el flujo durante la jornada.</p>
                            </div>
                            <div className="rounded-2xl border border-cyan-100 dark:border-cyan-900/60 bg-cyan-50/80 dark:bg-cyan-950/30 px-4 py-3">
                                <p className="text-[11px] font-black uppercase tracking-[0.22em] text-cyan-700 dark:text-cyan-300">Médico con más carga</p>
                                <p className="mt-2 text-lg font-black leading-tight text-slate-900 dark:text-white">{busiestDoctor?.label || 'Sin datos'}</p>
                                <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">{busiestDoctor ? `${busiestDoctor.value} citas concentradas hoy.` : 'Aún no hay agenda asignada.'}</p>
                            </div>
                        </div>

                        <div className="flex flex-wrap gap-3">
                            <AppButton
                                icon="calendar_month"
                                onClick={() => navigate('/recepcion/citas')}
                                className="bg-sky-500 text-white hover:bg-sky-600 shadow-lg shadow-sky-500/25"
                            >
                                Ir a agenda
                            </AppButton>
                            <AppButton
                                variant="ghost"
                                icon="how_to_reg"
                                onClick={() => navigate('/recepcion/pacientes')}
                                className="border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800"
                            >
                                Registro base
                            </AppButton>
                            <AppButton
                                variant="ghost"
                                icon="refresh"
                                onClick={() => window.location.reload()}
                                className="border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800"
                            >
                                Actualizar
                            </AppButton>
                        </div>
                    </div>

                    <div className="flex flex-col gap-4 xl:border-l xl:border-slate-200 xl:dark:border-slate-800 xl:pl-8">
                        <div className="flex items-center justify-between gap-3">
                            <div>
                                <p className="text-[11px] font-black uppercase tracking-[0.22em] text-slate-400">Próxima cita clave</p>
                                <h3 className="mt-2 text-xl font-black text-slate-900 dark:text-white">{nextAppointment?.title || 'Agenda estable'}</h3>
                            </div>
                            <span className="rounded-full border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-1 text-[11px] font-black uppercase tracking-[0.2em] text-slate-500 dark:text-slate-300">
                                Auto 60s
                            </span>
                        </div>

                        <p className="text-sm text-slate-500 dark:text-slate-400">
                            {nextAppointment?.subtitle || nextAppointment?.meta || 'No hay citas pendientes inmediatas para intervenir desde recepción.'}
                        </p>

                        <div className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50/80 dark:bg-slate-950/40 p-4">
                            <div className="flex items-center justify-between gap-3 text-sm">
                                <span className="text-slate-500 dark:text-slate-400">Hora estimada</span>
                                <span className="font-black text-slate-900 dark:text-white">
                                    {nextAppointment?.timestamp ? new Date(nextAppointment.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '--'}
                                </span>
                            </div>
                            <div className="mt-3 flex items-center justify-between gap-3 text-sm">
                                <span className="text-slate-500 dark:text-slate-400">Estado</span>
                                <span className="rounded-full bg-sky-100 dark:bg-sky-500/10 px-3 py-1 text-[11px] font-black uppercase tracking-[0.18em] text-sky-700 dark:text-sky-300">
                                    {nextAppointment?.status?.replaceAll('_', ' ') || 'SIN ALERTAS'}
                                </span>
                            </div>
                            <p className="mt-4 text-sm text-slate-600 dark:text-slate-300">{nextAppointment?.meta || 'Sin observaciones adicionales para esta franja.'}</p>
                        </div>

                        <div className="grid grid-cols-2 gap-3 text-left">
                            <div className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-white/70 dark:bg-slate-900/70 px-4 py-3">
                                <p className="text-[11px] font-black uppercase tracking-[0.22em] text-slate-400">En atención</p>
                                <p className="mt-2 text-2xl font-black text-slate-900 dark:text-white">{enAtencion}</p>
                            </div>
                            <div className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-white/70 dark:bg-slate-900/70 px-4 py-3">
                                <p className="text-[11px] font-black uppercase tracking-[0.22em] text-slate-400">Próximas citas</p>
                                <p className="mt-2 text-2xl font-black text-slate-900 dark:text-white">{metrics.queue.length}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4 md:gap-6">
                {metrics.cards.map((card, index) => (
                    <DashboardMetricCard
                        key={card.key}
                        label={card.label}
                        value={card.value}
                        helperText={card.helperText}
                        icon={CARD_ICONS[card.key] || 'monitoring'}
                        accent={CARD_ACCENTS[index % CARD_ACCENTS.length]}
                    />
                ))}
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-5 shadow-sm">
                    <p className="text-[11px] font-black uppercase tracking-[0.24em] text-slate-400">Ritmo de ingreso</p>
                    <p className="mt-3 text-3xl font-black text-slate-900 dark:text-white">{checkIn}</p>
                    <p className="mt-2 text-sm text-slate-500">Pacientes ya registrados hoy frente al total agendado.</p>
                </div>
                <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-5 shadow-sm">
                    <p className="text-[11px] font-black uppercase tracking-[0.24em] text-slate-400">Carga médica activa</p>
                    <p className="mt-3 text-3xl font-black text-slate-900 dark:text-white">{metrics.tertiarySeries.length}</p>
                    <p className="mt-2 text-sm text-slate-500">Médicos con agenda asignada visibles en el reparto actual.</p>
                </div>
                <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-5 shadow-sm">
                    <p className="text-[11px] font-black uppercase tracking-[0.24em] text-slate-400">Conversión a atención</p>
                    <p className="mt-3 text-3xl font-black text-slate-900 dark:text-white">{formatRatio(enAtencion, citasHoy || enAtencion)}</p>
                    <p className="mt-2 text-sm text-slate-500">Fracción de citas del día que ya avanzaron a atención clínica.</p>
                </div>
            </div>

            <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
                <div className="xl:col-span-2">
                    <DashboardPanel title="Carga Horaria de la Agenda" icon="multiline_chart" actions={<span className="text-xs font-semibold text-slate-400">Capacidad por franja</span>}>
                        {metrics.primarySeries.length > 0 ? (
                            <ResponsiveContainer width="100%" height={320}>
                                <LineChart data={metrics.primarySeries}>
                                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                                    <XAxis dataKey="label" tick={{ fontSize: 12 }} />
                                    <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                                    <Tooltip />
                                    <Line type="monotone" dataKey="value" name="Citas" stroke="#0ea5e9" strokeWidth={3} dot={{ r: 4 }} />
                                </LineChart>
                            </ResponsiveContainer>
                        ) : (
                            <DashboardEmptyState message="No hay citas programadas hoy para mostrar la carga horaria." />
                        )}
                    </DashboardPanel>
                </div>

                <DashboardPanel title="Estados de Atención" icon="pie_chart" actions={<span className="text-xs font-semibold text-slate-400">Distribución operativa</span>}>
                    {metrics.secondarySeries.length > 0 ? (
                        <ResponsiveContainer width="100%" height={320}>
                            <PieChart>
                                <Pie data={metrics.secondarySeries} dataKey="value" nameKey="label" innerRadius={60} outerRadius={110} paddingAngle={3}>
                                    {metrics.secondarySeries.map((entry, index) => (
                                        <Cell key={entry.label} fill={STATUS_COLORS[index % STATUS_COLORS.length]} />
                                    ))}
                                </Pie>
                                <Tooltip />
                                <Legend />
                            </PieChart>
                        </ResponsiveContainer>
                    ) : (
                        <DashboardEmptyState message="No hay estados de cita para mostrar en recepción." />
                    )}
                </DashboardPanel>
            </div>

            <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
                <div className="xl:col-span-2">
                    <DashboardPanel title="Carga por Médico" icon="bar_chart" actions={<span className="text-xs font-semibold text-slate-400">Reparto de agenda</span>}>
                        {metrics.tertiarySeries.length > 0 ? (
                            <ResponsiveContainer width="100%" height={320}>
                                <BarChart data={metrics.tertiarySeries}>
                                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                                    <XAxis dataKey="label" tick={{ fontSize: 12 }} />
                                    <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                                    <Tooltip />
                                    <Bar dataKey="value" radius={[12, 12, 0, 0]}>
                                        {metrics.tertiarySeries.map((entry, index) => (
                                            <Cell key={entry.label} fill={DOCTOR_COLORS[index % DOCTOR_COLORS.length]} />
                                        ))}
                                    </Bar>
                                </BarChart>
                            </ResponsiveContainer>
                        ) : (
                            <DashboardEmptyState message="Aún no hay citas con médico asignado para construir este gráfico." />
                        )}
                    </DashboardPanel>
                </div>

                <DashboardPanel title="Próximas Citas del Día" icon="event_upcoming" actions={<span className="text-xs font-semibold text-slate-400">Seguimiento inmediato</span>}>
                    {metrics.queue.length > 0 ? (
                        <div className="space-y-3">
                            {metrics.queue.map((item, index) => (
                                <button
                                    key={item.id ?? item.title}
                                    onClick={() => navigate('/recepcion/citas')}
                                    className="w-full text-left rounded-2xl border border-slate-100 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-800/30 p-4 hover:border-primary hover:bg-primary/5 transition-colors"
                                >
                                    <div className="flex items-start gap-4">
                                        <div className={`mt-0.5 h-11 w-1.5 rounded-full ${index === 0 ? 'bg-sky-500' : index === 1 ? 'bg-cyan-500' : 'bg-emerald-500'}`} />
                                        <div className="min-w-0 flex-1">
                                            <div className="flex items-start justify-between gap-3">
                                                <div>
                                                    <p className="font-bold text-slate-900 dark:text-white text-sm">{item.title}</p>
                                                    <p className="text-xs text-slate-500 mt-1">{item.subtitle || 'Paciente agendado'}</p>
                                                </div>
                                                <span className="px-2.5 py-1 rounded-lg text-[10px] font-black uppercase bg-sky-100 text-sky-600 dark:bg-sky-500/10 dark:text-sky-300">
                                                    {item.status?.replaceAll('_', ' ') || 'PENDIENTE'}
                                                </span>
                                            </div>
                                            <div className="flex items-center justify-between mt-3 text-xs text-slate-400 gap-3">
                                                <span>{item.timestamp ? new Date(item.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '--'}</span>
                                                <span className="truncate max-w-[220px]">{item.meta}</span>
                                            </div>
                                        </div>
                                    </div>
                                </button>
                            ))}
                        </div>
                    ) : (
                        <DashboardEmptyState message="No hay próximas citas pendientes para hoy." />
                    )}
                </DashboardPanel>
            </div>
        </div>
    );
};

export default ReceptionHomeView;
