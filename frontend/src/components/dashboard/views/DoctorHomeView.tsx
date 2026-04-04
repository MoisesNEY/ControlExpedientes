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
import { useAuth } from '../../../context/AuthContext';
import { DashboardEmptyState, DashboardLoading, DashboardMetricCard, DashboardPanel } from '../../analytics/DashboardPrimitives';
import { DashboardService, type DashboardListItem, type DashboardMetrics } from '../../../services/dashboard.service';
import { AppButton } from '../../ui/AppButton';

const CARD_ACCENTS = ['bg-amber-500', 'bg-cyan-500', 'bg-emerald-500', 'bg-violet-500'];
const CARD_ICONS: Record<string, string> = {
    totalAsignadas: 'calendar_month',
    enEspera: 'ward',
    atendidas: 'check_circle',
    activas: 'stethoscope',
};
const STATUS_COLORS = ['#f59e0b', '#0ea5e9', '#22c55e', '#ef4444', '#64748b'];
const DIAGNOSIS_COLORS = ['#0f766e', '#0284c7', '#7c3aed', '#ea580c', '#e11d48'];

const DoctorHomeView = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const loadDashboard = async () => {
            setLoading(true);
            try {
                setMetrics(await DashboardService.getDoctorDashboard());
            } catch (error) {
                console.error('Error fetching doctor dashboard:', error);
            } finally {
                setLoading(false);
            }
        };

        loadDashboard();
        const interval = setInterval(loadDashboard, 30000);
        return () => clearInterval(interval);
    }, []);

    const openConsultation = (item?: DashboardListItem | null) => {
        if (!item?.id) return;
        try {
            localStorage.setItem('activeConsultation', String(item.id));
        } catch {
            // ignore
        }
        navigate(`/medico/consulta/${item.id}`);
    };

    if (loading) {
        return <DashboardLoading />;
    }

    if (!metrics) {
        return <DashboardEmptyState message="No se pudieron cargar las métricas del panel médico." />;
    }

    return (
        <div className="p-4 md:p-8 max-w-7xl mx-auto w-full flex flex-col gap-6 md:gap-8 transition-colors duration-300">
            <div className="flex flex-col lg:flex-row lg:items-end justify-between gap-4">
                <div className="flex flex-col gap-1">
                    <h2 className="text-slate-900 dark:text-white text-3xl font-black tracking-tight">Dashboard Médico</h2>
                    <p className="text-slate-500 text-base font-medium">
                        Bienvenido, Dr. {user?.lastName || user?.firstName || 'Médico'}. Seguimiento de agenda, consultas y diagnósticos en tiempo real.
                    </p>
                </div>
                {metrics.spotlight?.id && (
                    <AppButton icon="stethoscope" onClick={() => openConsultation(metrics.spotlight)}>
                        Continuar consulta activa
                    </AppButton>
                )}
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

            {metrics.spotlight && (
                <div className="rounded-3xl bg-gradient-to-r from-emerald-500 via-emerald-600 to-teal-600 p-6 text-white shadow-xl shadow-emerald-600/20">
                    <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
                        <div>
                            <p className="text-xs font-black uppercase tracking-[0.2em] text-emerald-100">Consulta en curso</p>
                            <h3 className="text-2xl font-black mt-2">{metrics.spotlight.title}</h3>
                            <p className="text-sm text-emerald-50 mt-2">
                                {metrics.spotlight.subtitle || 'Paciente actualmente en atención'}
                                {metrics.spotlight.meta ? ` · ${metrics.spotlight.meta}` : ''}
                            </p>
                        </div>
                        <div className="flex items-center gap-3">
                            <span className="px-3 py-1.5 rounded-full bg-white/15 text-xs font-black uppercase tracking-wider">
                                {metrics.spotlight.status?.replaceAll('_', ' ')}
                            </span>
                            <AppButton
                                variant="outline"
                                className="border-white/30 text-white hover:bg-white/10"
                                icon="arrow_forward"
                                onClick={() => openConsultation(metrics.spotlight)}
                            >
                                Abrir consulta
                            </AppButton>
                        </div>
                    </div>
                </div>
            )}

            <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
                <div className="xl:col-span-2">
                    <DashboardPanel title="Consultas de los Últimos 7 Días" icon="show_chart">
                        {metrics.primarySeries.length > 0 ? (
                            <ResponsiveContainer width="100%" height={320}>
                                <LineChart data={metrics.primarySeries}>
                                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                                    <XAxis dataKey="label" tick={{ fontSize: 12 }} />
                                    <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                                    <Tooltip />
                                    <Legend />
                                    <Line type="monotone" dataKey="value" name="Consultas" stroke="#0ea5e9" strokeWidth={3} dot={{ r: 4 }} activeDot={{ r: 7 }} />
                                </LineChart>
                            </ResponsiveContainer>
                        ) : (
                            <DashboardEmptyState message="Todavía no hay consultas registradas en el período reciente." />
                        )}
                    </DashboardPanel>
                </div>

                <DashboardPanel title="Estado de Agenda Hoy" icon="donut_large">
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
                        <DashboardEmptyState message="No hay citas del médico hoy para distribuir por estado." />
                    )}
                </DashboardPanel>
            </div>

            <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
                <div className="xl:col-span-2">
                    <DashboardPanel title="Diagnósticos Más Frecuentes" icon="bar_chart">
                        {metrics.tertiarySeries.length > 0 ? (
                            <ResponsiveContainer width="100%" height={320}>
                                <BarChart data={metrics.tertiarySeries} layout="vertical" margin={{ left: 20 }}>
                                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                                    <XAxis type="number" allowDecimals={false} tick={{ fontSize: 12 }} />
                                    <YAxis type="category" dataKey="label" width={140} tick={{ fontSize: 12 }} />
                                    <Tooltip />
                                    <Bar dataKey="value" radius={[0, 12, 12, 0]}>
                                        {metrics.tertiarySeries.map((entry, index) => (
                                            <Cell key={entry.label} fill={DIAGNOSIS_COLORS[index % DIAGNOSIS_COLORS.length]} />
                                        ))}
                                    </Bar>
                                </BarChart>
                            </ResponsiveContainer>
                        ) : (
                            <DashboardEmptyState message="Aún no hay diagnósticos suficientes para construir el ranking." />
                        )}
                    </DashboardPanel>
                </div>

                <DashboardPanel title="Pacientes Listos para Consulta" icon="ward">
                    {metrics.queue.length > 0 ? (
                        <div className="space-y-3">
                            {metrics.queue.map((item) => (
                                <button
                                    key={item.id ?? item.title}
                                    onClick={() => openConsultation(item)}
                                    className="w-full text-left rounded-2xl border border-slate-100 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-800/30 p-4 hover:border-sky-200 hover:bg-sky-50/60 dark:hover:bg-slate-800 transition-colors"
                                >
                                    <div className="flex items-start justify-between gap-3">
                                        <div>
                                            <p className="font-bold text-slate-900 dark:text-white text-sm">{item.title}</p>
                                            <p className="text-xs text-slate-500 mt-1">{item.subtitle || 'Asignado a tu agenda'}</p>
                                        </div>
                                        <span className="px-2.5 py-1 rounded-lg text-[10px] font-black uppercase bg-amber-100 text-amber-600 dark:bg-amber-500/10 dark:text-amber-300">
                                            {item.status?.replaceAll('_', ' ')}
                                        </span>
                                    </div>
                                    <div className="flex items-center justify-between mt-3 text-xs text-slate-400">
                                        <span>{item.timestamp ? new Date(item.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '--'}</span>
                                        <span className="truncate max-w-[220px]">{item.meta}</span>
                                    </div>
                                </button>
                            ))}
                        </div>
                    ) : (
                        <DashboardEmptyState message="No hay pacientes esperando al médico en este momento." />
                    )}
                </DashboardPanel>
            </div>
        </div>
    );
};

export default DoctorHomeView;
