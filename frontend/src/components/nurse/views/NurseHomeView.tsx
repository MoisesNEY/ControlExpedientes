import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Area,
    AreaChart,
    Bar,
    BarChart,
    CartesianGrid,
    Cell,
    Legend,
    PolarAngleAxis,
    RadialBar,
    RadialBarChart,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from 'recharts';
import { DashboardEmptyState, DashboardLoading, DashboardMetricCard, DashboardPanel } from '../../analytics/DashboardPrimitives';
import { DashboardService, type DashboardMetrics } from '../../../services/dashboard.service';
import { AppButton } from '../../ui/AppButton';

const CARD_ICONS: Record<string, string> = {
    salaEspera: 'hourglass_top',
    triage: 'monitor_heart',
    listosMedico: 'stethoscope',
    programadas: 'calendar_today',
};
const CARD_ACCENTS = ['bg-amber-500', 'bg-violet-500', 'bg-emerald-500', 'bg-sky-500'];
const STATUS_COLORS = ['#f59e0b', '#8b5cf6', '#22c55e', '#0ea5e9', '#64748b'];

const getCardValue = (metrics: DashboardMetrics, key: string) => Number(metrics.cards.find(card => card.key === key)?.value ?? 0);

const formatPercent = (value: number, total: number) => {
    if (total <= 0) return '0%';
    return `${Math.round((value / total) * 100)}%`;
};

const NurseHomeView = () => {
    const navigate = useNavigate();
    const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const loadDashboard = async () => {
            setLoading(true);
            try {
                setMetrics(await DashboardService.getNurseDashboard());
            } catch (error) {
                console.error('Error fetching nurse dashboard:', error);
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
        return <DashboardEmptyState message="No se pudieron cargar las métricas del panel de enfermería." />;
    }

    const salaEspera = getCardValue(metrics, 'salaEspera');
    const triage = getCardValue(metrics, 'triage');
    const listosMedico = getCardValue(metrics, 'listosMedico');
    const programadas = getCardValue(metrics, 'programadas');
    const maxFlowValue = Math.max(...metrics.secondarySeries.map((entry) => entry.value), 1);

    return (
        <div className="p-4 md:p-8 max-w-7xl mx-auto w-full flex flex-col gap-6 md:gap-8 transition-colors duration-300">
            <div className="relative overflow-hidden rounded-[32px] border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 md:p-8 shadow-sm">
                <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(245,158,11,0.12),transparent_32%),radial-gradient(circle_at_bottom_right,rgba(16,185,129,0.1),transparent_28%)] dark:bg-[radial-gradient(circle_at_top_left,rgba(245,158,11,0.18),transparent_32%),radial-gradient(circle_at_bottom_right,rgba(16,185,129,0.1),transparent_28%)]" />
                <div className="relative grid gap-8 xl:grid-cols-[1.5fr_0.95fr] xl:items-start">
                    <div className="space-y-5">
                        <div>
                            <p className="text-xs font-black uppercase tracking-[0.32em] text-amber-600 dark:text-amber-300">Centro de coordinación clínica</p>
                            <h2 className="mt-3 text-3xl md:text-4xl font-black tracking-tight text-slate-900 dark:text-white">Dashboard de Enfermería</h2>
                            <p className="mt-3 max-w-2xl text-sm md:text-base text-slate-600 dark:text-slate-300">
                                Control del ingreso, triage y derivación a consulta con una lectura rápida de presión operativa.
                            </p>
                        </div>

                        <div className="grid gap-3 sm:grid-cols-3">
                            <div className="rounded-2xl border border-amber-100 dark:border-amber-900/60 bg-amber-50/80 dark:bg-amber-950/30 px-4 py-3">
                                <p className="text-[11px] font-black uppercase tracking-[0.22em] text-amber-700 dark:text-amber-300">Presión de sala</p>
                                <p className="mt-2 text-2xl font-black text-slate-900 dark:text-white">{formatPercent(salaEspera, programadas || salaEspera)}</p>
                                <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">Pacientes esperando respecto al total programado.</p>
                            </div>
                            <div className="rounded-2xl border border-violet-100 dark:border-violet-900/60 bg-violet-50/80 dark:bg-violet-950/30 px-4 py-3">
                                <p className="text-[11px] font-black uppercase tracking-[0.22em] text-violet-700 dark:text-violet-300">Triage activo</p>
                                <p className="mt-2 text-2xl font-black text-slate-900 dark:text-white">{triage}</p>
                                <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">Pacientes en valoración por enfermería ahora.</p>
                            </div>
                            <div className="rounded-2xl border border-emerald-100 dark:border-emerald-900/60 bg-emerald-50/80 dark:bg-emerald-950/30 px-4 py-3">
                                <p className="text-[11px] font-black uppercase tracking-[0.22em] text-emerald-700 dark:text-emerald-300">Derivación lista</p>
                                <p className="mt-2 text-2xl font-black text-slate-900 dark:text-white">{formatPercent(listosMedico, Math.max(listosMedico + triage, 1))}</p>
                                <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">Proporción del flujo que ya puede pasar a consulta.</p>
                            </div>
                        </div>

                        <div className="flex flex-wrap gap-3">
                            <AppButton
                                icon="refresh"
                                onClick={() => window.location.reload()}
                                className="bg-amber-500 text-white hover:bg-amber-600 shadow-lg shadow-amber-500/25"
                            >
                                Actualizar vista
                            </AppButton>
                            <AppButton
                                variant="ghost"
                                icon="inventory_2"
                                onClick={() => navigate('/enfermeria/inventario')}
                                className="border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800"
                            >
                                Ver insumos
                            </AppButton>
                        </div>
                    </div>

                    <div className="flex flex-col gap-4 xl:border-l xl:border-slate-200 xl:dark:border-slate-800 xl:pl-8">
                        <div className="flex items-center justify-between gap-3">
                            <div>
                                <p className="text-[11px] font-black uppercase tracking-[0.22em] text-slate-400">Siguiente acción</p>
                                <h3 className="mt-2 text-xl font-black text-slate-900 dark:text-white">{metrics.spotlight?.title || 'Sin paciente priorizado'}</h3>
                            </div>
                            <span className="rounded-full border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-1 text-[11px] font-black uppercase tracking-[0.2em] text-slate-500 dark:text-slate-300">
                                Auto 60s
                            </span>
                        </div>

                        <p className="text-sm text-slate-500 dark:text-slate-400">
                            {metrics.spotlight?.meta || metrics.spotlight?.subtitle || 'Cuando aparezca un paciente en foco, podrás iniciar triage desde aquí.'}
                        </p>

                        {metrics.spotlight?.id ? (
                            <AppButton
                                icon="vital_signs"
                                onClick={() => navigate(`/enfermeria/triage/${metrics.spotlight?.id}`)}
                                className="w-full bg-amber-500 text-white hover:bg-amber-600 shadow-lg shadow-amber-500/25"
                            >
                                Iniciar triage del paciente en foco
                            </AppButton>
                        ) : null}

                        <div className="space-y-3 rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50/80 dark:bg-slate-950/40 p-4">
                            {metrics.secondarySeries.length > 0 ? (
                                metrics.secondarySeries.slice(0, 4).map((entry, index) => (
                                    <div key={entry.label} className="space-y-1.5">
                                        <div className="flex items-center justify-between gap-3 text-xs font-semibold text-slate-600 dark:text-slate-300">
                                            <span>{entry.label}</span>
                                            <span>{entry.value}</span>
                                        </div>
                                        <div className="h-2 rounded-full bg-slate-200 dark:bg-slate-800 overflow-hidden">
                                            <div
                                                className="h-full rounded-full transition-all"
                                                style={{
                                                    width: `${(entry.value / maxFlowValue) * 100}%`,
                                                    backgroundColor: STATUS_COLORS[index % STATUS_COLORS.length],
                                                }}
                                            />
                                        </div>
                                    </div>
                                ))
                            ) : (
                                <p className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-white/80 dark:bg-slate-900/70 px-4 py-3 text-sm text-slate-500 dark:text-slate-400">
                                    Todavía no hay estados activos para calcular la presión del flujo clínico.
                                </p>
                            )}
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
                    <p className="text-[11px] font-black uppercase tracking-[0.24em] text-slate-400">Pasando a consulta</p>
                    <p className="mt-3 text-3xl font-black text-slate-900 dark:text-white">{listosMedico}</p>
                    <p className="mt-2 text-sm text-slate-500">Pacientes ya listos para ser tomados por el médico.</p>
                </div>
                <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-5 shadow-sm">
                    <p className="text-[11px] font-black uppercase tracking-[0.24em] text-slate-400">Cola inmediata</p>
                    <p className="mt-3 text-3xl font-black text-slate-900 dark:text-white">{metrics.queue.length}</p>
                    <p className="mt-2 text-sm text-slate-500">Pacientes con posibilidad de ser evaluados desde este panel.</p>
                </div>
                <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-5 shadow-sm">
                    <p className="text-[11px] font-black uppercase tracking-[0.24em] text-slate-400">Carga del turno</p>
                    <p className="mt-3 text-3xl font-black text-slate-900 dark:text-white">{programadas}</p>
                    <p className="mt-2 text-sm text-slate-500">Total de pacientes programados en la jornada actual.</p>
                </div>
            </div>

            <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
                <div className="xl:col-span-2">
                    <DashboardPanel title="Ingresos por Hora" icon="timeline" actions={<span className="text-xs font-semibold text-slate-400">Tiempo real del día</span>}>
                        {metrics.primarySeries.length > 0 ? (
                            <ResponsiveContainer width="100%" height={320}>
                                <AreaChart data={metrics.primarySeries}>
                                    <defs>
                                        <linearGradient id="nurseArea" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="5%" stopColor="#f59e0b" stopOpacity={0.35} />
                                            <stop offset="95%" stopColor="#f59e0b" stopOpacity={0.02} />
                                        </linearGradient>
                                    </defs>
                                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                                    <XAxis dataKey="label" tick={{ fontSize: 12 }} />
                                    <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                                    <Tooltip />
                                    <Area type="monotone" dataKey="value" name="Pacientes" stroke="#f59e0b" fill="url(#nurseArea)" strokeWidth={3} />
                                </AreaChart>
                            </ResponsiveContainer>
                        ) : (
                            <DashboardEmptyState message="Aún no hay ingresos registrados hoy." />
                        )}
                    </DashboardPanel>
                </div>

                <DashboardPanel title="Distribución del Flujo" icon="donut_large" actions={<span className="text-xs font-semibold text-slate-400">Presión por estado</span>}>
                    {metrics.secondarySeries.length > 0 ? (
                        <ResponsiveContainer width="100%" height={320}>
                            <RadialBarChart innerRadius="20%" outerRadius="100%" data={metrics.secondarySeries} startAngle={180} endAngle={0}>
                                <PolarAngleAxis type="number" domain={[0, Math.max(...metrics.secondarySeries.map((entry) => entry.value), 1)]} tick={false} />
                                <RadialBar background dataKey="value">
                                    {metrics.secondarySeries.map((entry, index) => (
                                        <Cell key={entry.label} fill={STATUS_COLORS[index % STATUS_COLORS.length]} />
                                    ))}
                                </RadialBar>
                                <Legend />
                                <Tooltip />
                            </RadialBarChart>
                        </ResponsiveContainer>
                    ) : (
                        <DashboardEmptyState message="No hay estados activos para mostrar en enfermería." />
                    )}
                </DashboardPanel>
            </div>

            <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
                <div className="xl:col-span-2">
                    <DashboardPanel title="Evolución Hacia Consulta" icon="bar_chart" actions={<span className="text-xs font-semibold text-slate-400">Listos y derivados</span>}>
                        {metrics.tertiarySeries.length > 0 ? (
                            <ResponsiveContainer width="100%" height={320}>
                                <BarChart data={metrics.tertiarySeries}>
                                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                                    <XAxis dataKey="label" tick={{ fontSize: 12 }} />
                                    <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                                    <Tooltip />
                                    <Bar dataKey="value" name="Pacientes" radius={[12, 12, 0, 0]} fill="#14b8a6" />
                                </BarChart>
                            </ResponsiveContainer>
                        ) : (
                            <DashboardEmptyState message="Todavía no hay pacientes listos o derivados hoy." />
                        )}
                    </DashboardPanel>
                </div>

                <DashboardPanel title="Cola de Espera" icon="queue" actions={<span className="text-xs font-semibold text-slate-400">Acceso rápido a triage</span>}>
                    {metrics.queue.length > 0 ? (
                        <div className="space-y-3">
                            {metrics.queue.map((item, index) => (
                                <button
                                    key={item.id ?? `${item.title}-${index}`}
                                    onClick={() => item.id && navigate(`/enfermeria/triage/${item.id}`)}
                                    className="w-full text-left rounded-2xl border border-slate-100 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-800/30 p-4 hover:border-primary hover:bg-primary/5 transition-colors"
                                >
                                    <div className="flex items-start gap-4">
                                        <div className={`mt-0.5 h-11 w-1.5 rounded-full ${index === 0 ? 'bg-rose-500' : index === 1 ? 'bg-amber-500' : 'bg-emerald-500'}`} />
                                        <div>
                                            <div className="flex items-start justify-between gap-3">
                                                <div>
                                                    <p className="font-bold text-slate-900 dark:text-white text-sm">{item.title}</p>
                                                    <p className="text-xs text-slate-500 mt-1">{item.subtitle}</p>
                                                </div>
                                                <span className="px-2.5 py-1 rounded-lg text-[10px] font-black uppercase bg-amber-100 text-amber-600 dark:bg-amber-500/10 dark:text-amber-300">
                                                    {index + 1}
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
                        <DashboardEmptyState message="La sala de espera está vacía en este momento." />
                    )}
                </DashboardPanel>
            </div>
        </div>
    );
};

export default NurseHomeView;
