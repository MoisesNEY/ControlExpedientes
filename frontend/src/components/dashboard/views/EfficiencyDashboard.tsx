import { useState, useEffect } from 'react';
import api from '../../../services/api';
import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
    PieChart, Pie, Cell, Legend
} from 'recharts';

interface EstadisticaConsultas {
    totalConsultas: number;
    consultasHoy: number;
    pacientesAtendidosHoy: number;
    promedioConsultasPorDia: number;
}

interface ConsultasPorEstado {
    estado: string;
    cantidad: number;
}

const COLORS = ['#06b6d4', '#8b5cf6', '#f59e0b', '#10b981', '#ef4444', '#3b82f6'];

const ESTADO_LABELS: Record<string, string> = {
    EN_RECEPCION: 'Recepción',
    EN_TRIAGE: 'Triage',
    ESPERANDO_MEDICO: 'Espera',
    EN_CONSULTA: 'Consulta',
    ATENDIDA: 'Atendida',
    CANCELADA: 'Cancelada',
};

/**
 * Dashboard de Eficiencia con gráficos Recharts.
 * Muestra estadísticas de consultas médicas del día y distribución por estado.
 */
const EfficiencyDashboard = () => {
    const [stats, setStats] = useState<EstadisticaConsultas | null>(null);
    const [porEstado, setPorEstado] = useState<ConsultasPorEstado[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            try {
                // Obtener todas las citas del día para calcular estadísticas
                const today = new Date().toISOString().split('T')[0];
                const response = await api.get('/api/cita-medicas', {
                    params: {
                        'fechaHora.greaterThanOrEqual': `${today}T00:00:00Z`,
                        'fechaHora.lessThanOrEqual': `${today}T23:59:59Z`,
                        size: 1000,
                    },
                });

                const citas = response.data || [];
                const total = citas.length;
                const atendidas = citas.filter((c: any) => c.estado === 'ATENDIDA').length;

                // Agrupar por estado
                const estadoMap: Record<string, number> = {};
                citas.forEach((c: any) => {
                    estadoMap[c.estado] = (estadoMap[c.estado] || 0) + 1;
                });

                const distribucion = Object.entries(estadoMap).map(([estado, cantidad]) => ({
                    estado: ESTADO_LABELS[estado] || estado,
                    cantidad,
                }));

                setStats({
                    totalConsultas: total,
                    consultasHoy: total,
                    pacientesAtendidosHoy: atendidas,
                    promedioConsultasPorDia: total,
                });
                setPorEstado(distribucion);
            } catch (err) {
                console.error('Error cargando estadísticas:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    if (loading) {
        return (
            <div className="flex items-center justify-center h-64">
                <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-primary"></div>
            </div>
        );
    }

    return (
        <div className="flex flex-col gap-6">
            {/* KPI Cards */}
            <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
                <KPICard
                    icon="calendar_today"
                    label="Citas del Día"
                    value={stats?.consultasHoy ?? 0}
                    color="text-cyan-500"
                    bgColor="bg-cyan-50 dark:bg-cyan-500/10"
                />
                <KPICard
                    icon="check_circle"
                    label="Pacientes Atendidos"
                    value={stats?.pacientesAtendidosHoy ?? 0}
                    color="text-emerald-500"
                    bgColor="bg-emerald-50 dark:bg-emerald-500/10"
                />
                <KPICard
                    icon="pending"
                    label="En Espera"
                    value={porEstado.find(e => e.estado === 'Espera')?.cantidad ?? 0}
                    color="text-amber-500"
                    bgColor="bg-amber-50 dark:bg-amber-500/10"
                />
                <KPICard
                    icon="trending_up"
                    label="Tasa de Atención"
                    value={stats && stats.totalConsultas > 0
                        ? `${Math.round((stats.pacientesAtendidosHoy / stats.totalConsultas) * 100)}%`
                        : '0%'}
                    color="text-violet-500"
                    bgColor="bg-violet-50 dark:bg-violet-500/10"
                />
            </div>

            {/* Charts */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* Bar Chart: Distribución por Estado */}
                <div className="bg-white dark:bg-slate-900 rounded-2xl shadow-sm border border-slate-200 dark:border-slate-800 p-6">
                    <h3 className="font-bold text-slate-900 dark:text-white mb-4 flex items-center gap-2">
                        <span className="material-symbols-outlined text-cyan-500">bar_chart</span>
                        Distribución de Citas por Estado
                    </h3>
                    {porEstado.length > 0 ? (
                        <ResponsiveContainer width="100%" height={300}>
                            <BarChart data={porEstado} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                                <XAxis dataKey="estado" tick={{ fontSize: 12 }} />
                                <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                                <Tooltip
                                    contentStyle={{
                                        borderRadius: '12px',
                                        border: '1px solid #e2e8f0',
                                        fontSize: '13px',
                                    }}
                                />
                                <Bar dataKey="cantidad" fill="#06b6d4" radius={[8, 8, 0, 0]} />
                            </BarChart>
                        </ResponsiveContainer>
                    ) : (
                        <EmptyState message="Sin datos para mostrar" />
                    )}
                </div>

                {/* Pie Chart: Proporción por Estado */}
                <div className="bg-white dark:bg-slate-900 rounded-2xl shadow-sm border border-slate-200 dark:border-slate-800 p-6">
                    <h3 className="font-bold text-slate-900 dark:text-white mb-4 flex items-center gap-2">
                        <span className="material-symbols-outlined text-violet-500">donut_large</span>
                        Proporción de Estados
                    </h3>
                    {porEstado.length > 0 ? (
                        <ResponsiveContainer width="100%" height={300}>
                            <PieChart>
                                <Pie
                                    data={porEstado}
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={60}
                                    outerRadius={100}
                                    paddingAngle={5}
                                    dataKey="cantidad"
                                    nameKey="estado"
                                    label={({ name, percent }: { name?: string; percent?: number }) => `${name || ''} ${((percent || 0) * 100).toFixed(0)}%`}
                                >
                                    {porEstado.map((_entry, index) => (
                                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                    ))}
                                </Pie>
                                <Tooltip />
                                <Legend />
                            </PieChart>
                        </ResponsiveContainer>
                    ) : (
                        <EmptyState message="Sin datos para mostrar" />
                    )}
                </div>
            </div>
        </div>
    );
};

// --- Sub-components ---

const KPICard = ({ icon, label, value, color, bgColor }: {
    icon: string; label: string; value: number | string; color: string; bgColor: string;
}) => (
    <div className={`${bgColor} rounded-2xl p-5 border border-slate-100 dark:border-slate-800 flex items-center gap-4 transition-all hover:scale-[1.02]`}>
        <div className={`${color} p-3 rounded-xl bg-white/80 dark:bg-slate-900/50 shadow-sm`}>
            <span className="material-symbols-outlined text-2xl">{icon}</span>
        </div>
        <div>
            <p className="text-xs font-bold text-slate-500 uppercase tracking-widest">{label}</p>
            <p className={`text-2xl font-black ${color}`}>{value}</p>
        </div>
    </div>
);

const EmptyState = ({ message }: { message: string }) => (
    <div className="flex flex-col items-center justify-center h-[250px] text-slate-400">
        <span className="material-symbols-outlined text-5xl mb-2">analytics</span>
        <p className="text-sm font-medium">{message}</p>
    </div>
);

export default EfficiencyDashboard;
