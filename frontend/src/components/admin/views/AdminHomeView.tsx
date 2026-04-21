import { useEffect, useState } from 'react';
import {
    Area,
    AreaChart,
    Bar,
    BarChart,
    CartesianGrid,
    Cell,
    Legend,
    Pie,
    PieChart,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from 'recharts';
import { DashboardEmptyState, DashboardLoading, DashboardMetricCard, DashboardPanel } from '../../analytics/DashboardPrimitives';
import { DashboardService, type DashboardMetrics } from '../../../services/dashboard.service';
import { DatabaseAdminService } from '../../../services/database-admin.service';
import { AppButton } from '../../ui/AppButton';

const STATUS_COLORS = ['#0ea5e9', '#14b8a6', '#f59e0b', '#8b5cf6', '#ef4444', '#22c55e', '#64748b'];
const SEX_COLORS = ['#38bdf8', '#fb7185', '#a78bfa'];
const CARD_ACCENTS = ['bg-sky-500', 'bg-cyan-500', 'bg-violet-500', 'bg-rose-500'];
const CARD_ICONS: Record<string, string> = {
    pacientesActivos: 'groups',
    citasHoy: 'calendar_month',
    consultasSemana: 'clinical_notes',
    stockBajo: 'warning',
};

const AdminHomeView = () => {
    const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
    const [loading, setLoading] = useState(true);
    const [isExportingDatabase, setIsExportingDatabase] = useState(false);
    const [isRestoringDatabase, setIsRestoringDatabase] = useState(false);
    const [backupFile, setBackupFile] = useState<File | null>(null);
    const [backupMessage, setBackupMessage] = useState<string | null>(null);

    useEffect(() => {
        const loadDashboard = async () => {
            setLoading(true);
            try {
                setMetrics(await DashboardService.getAdminDashboard());
            } catch (error) {
                console.error('Error fetching admin dashboard:', error);
            } finally {
                setLoading(false);
            }
        };

        loadDashboard();
    }, []);

    const handleExportDatabase = async () => {
        setBackupMessage(null);
        setIsExportingDatabase(true);
        try {
            await DatabaseAdminService.exportDatabase();
            setBackupMessage('Respaldo generado correctamente.');
        } catch (error) {
            console.error('Error exporting database:', error);
            setBackupMessage('No se pudo generar el respaldo de la base de datos.');
        } finally {
            setIsExportingDatabase(false);
        }
    };

    const handleRestoreDatabase = async () => {
        if (!backupFile) {
            setBackupMessage('Seleccione un archivo .backup o .sql antes de restaurar.');
            return;
        }

        const confirmed = window.confirm('Esta acción reemplazará la información actual de la base de datos. ¿Desea continuar?');
        if (!confirmed) {
            return;
        }

        setBackupMessage(null);
        setIsRestoringDatabase(true);
        try {
            await DatabaseAdminService.restoreDatabase(backupFile);
            setBackupMessage('Restauración ejecutada correctamente.');
            setBackupFile(null);
        } catch (error) {
            console.error('Error restoring database:', error);
            setBackupMessage('No se pudo restaurar el respaldo. Verifique el archivo y la disponibilidad de las utilidades PostgreSQL.');
        } finally {
            setIsRestoringDatabase(false);
        }
    };

    if (loading) {
        return <DashboardLoading />;
    }

    if (!metrics) {
        return <DashboardEmptyState message="No se pudieron cargar las métricas de administración." />;
    }

    return (
        <div className="p-4 md:p-8 max-w-7xl mx-auto w-full flex flex-col gap-6 md:gap-8 transition-colors duration-300">
            <div className="flex flex-col gap-1">
                <h2 className="text-slate-900 dark:text-white text-3xl font-black tracking-tight">Dashboard Administrativo</h2>
                <p className="text-slate-500 text-base font-medium">
                    Vista consolidada del sistema con métricas operativas, clínicas y de inventario.
                </p>
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

            <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
                <div className="xl:col-span-2">
                    <DashboardPanel title="Tendencia Semanal de Citas" icon="area_chart">
                        {metrics.primarySeries.length > 0 ? (
                            <ResponsiveContainer width="100%" height={320}>
                                <AreaChart data={metrics.primarySeries}>
                                    <defs>
                                        <linearGradient id="appointmentsGradient" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="5%" stopColor="#0ea5e9" stopOpacity={0.35} />
                                            <stop offset="95%" stopColor="#0ea5e9" stopOpacity={0.02} />
                                        </linearGradient>
                                    </defs>
                                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                                    <XAxis dataKey="label" tick={{ fontSize: 12 }} />
                                    <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                                    <Tooltip />
                                    <Legend />
                                    <Area type="monotone" dataKey="value" name="Citas" stroke="#0ea5e9" fill="url(#appointmentsGradient)" strokeWidth={3} />
                                    <Area type="monotone" dataKey="secondaryValue" name="Atendidas" stroke="#22c55e" fillOpacity={0} strokeWidth={2} />
                                    <Area type="monotone" dataKey="tertiaryValue" name="Canceladas" stroke="#ef4444" fillOpacity={0} strokeWidth={2} />
                                </AreaChart>
                            </ResponsiveContainer>
                        ) : (
                            <DashboardEmptyState message="Aún no hay citas suficientes para construir la tendencia semanal." />
                        )}
                    </DashboardPanel>
                </div>

                <DashboardPanel title="Estados de Citas Hoy" icon="pie_chart">
                    {metrics.secondarySeries.length > 0 ? (
                        <ResponsiveContainer width="100%" height={320}>
                            <PieChart>
                                <Pie data={metrics.secondarySeries} dataKey="value" nameKey="label" innerRadius={65} outerRadius={110} paddingAngle={3}>
                                    {metrics.secondarySeries.map((entry, index) => (
                                        <Cell key={entry.label} fill={STATUS_COLORS[index % STATUS_COLORS.length]} />
                                    ))}
                                </Pie>
                                <Tooltip />
                                <Legend />
                            </PieChart>
                        </ResponsiveContainer>
                    ) : (
                        <DashboardEmptyState message="No hay movimientos de citas hoy para mostrar en este gráfico." />
                    )}
                </DashboardPanel>
            </div>

            <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
                <div className="xl:col-span-2">
                    <DashboardPanel title="Distribución de Pacientes por Sexo" icon="bar_chart">
                        {metrics.tertiarySeries.length > 0 ? (
                            <ResponsiveContainer width="100%" height={300}>
                                <BarChart data={metrics.tertiarySeries}>
                                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                                    <XAxis dataKey="label" tick={{ fontSize: 12 }} />
                                    <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                                    <Tooltip />
                                    <Bar dataKey="value" radius={[12, 12, 0, 0]}>
                                        {metrics.tertiarySeries.map((entry, index) => (
                                            <Cell key={entry.label} fill={SEX_COLORS[index % SEX_COLORS.length]} />
                                        ))}
                                    </Bar>
                                </BarChart>
                            </ResponsiveContainer>
                        ) : (
                            <DashboardEmptyState message="Todavía no hay pacientes registrados para mostrar distribución." />
                        )}
                    </DashboardPanel>
                </div>

                <DashboardPanel title="Medicamentos con Stock Bajo" icon="inventory_2">
                    {metrics.queue.length > 0 ? (
                        <div className="space-y-3">
                            {metrics.queue.map((item) => (
                                <div key={item.id ?? item.title} className="rounded-2xl border border-slate-100 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-800/30 p-4">
                                    <div className="flex items-start justify-between gap-3">
                                        <div>
                                            <p className="font-bold text-slate-900 dark:text-white text-sm">{item.title}</p>
                                            <p className="text-xs text-slate-500 mt-1">{item.subtitle}</p>
                                        </div>
                                        <span className={`px-2.5 py-1 rounded-lg text-[10px] font-black uppercase ${item.status === 'CRITICO' ? 'bg-rose-100 text-rose-600 dark:bg-rose-500/10 dark:text-rose-300' : 'bg-amber-100 text-amber-600 dark:bg-amber-500/10 dark:text-amber-300'}`}>
                                            {item.status}
                                        </span>
                                    </div>
                                    <p className="text-xs text-slate-400 mt-3">{item.meta}</p>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <DashboardEmptyState message="No hay medicamentos en zona crítica de inventario." />
                    )}
                </DashboardPanel>
            </div>

            <DashboardPanel title="Actividad Reciente" icon="shield">
                {metrics.activity.length > 0 ? (
                    <div className="divide-y divide-slate-100 dark:divide-slate-800">
                        {metrics.activity.map((item) => (
                            <div key={item.id ?? item.title} className="py-4 flex items-center gap-4 first:pt-0 last:pb-0">
                                <div className={`size-10 rounded-2xl flex items-center justify-center text-white ${item.action === 'ELIMINAR' ? 'bg-rose-500' : item.action === 'CREAR' ? 'bg-emerald-500' : 'bg-sky-500'}`}>
                                    <span className="material-symbols-outlined text-[18px]">
                                        {item.action === 'ELIMINAR' ? 'delete' : item.action === 'CREAR' ? 'add' : 'edit'}
                                    </span>
                                </div>
                                <div className="flex-1 min-w-0">
                                    <p className="text-sm font-bold text-slate-900 dark:text-white truncate">{item.title}</p>
                                    <p className="text-xs text-slate-500 truncate mt-1">{item.subtitle}</p>
                                </div>
                                <div className="text-right shrink-0">
                                    <p className="text-[11px] font-semibold text-slate-500">
                                        {item.timestamp ? new Date(item.timestamp).toLocaleDateString('es-ES', { day: '2-digit', month: 'short' }) : '--'}
                                    </p>
                                    <p className="text-[11px] text-slate-400">
                                        {item.timestamp ? new Date(item.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '--'}
                                    </p>
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
                    <DashboardEmptyState message="No hay actividad de auditoría reciente disponible." />
                )}
            </DashboardPanel>

            <DashboardPanel title="Respaldo y restauración" icon="database">
                <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">
                    <div className="rounded-2xl border border-slate-100 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-800/30 p-5 space-y-3">
                        <div>
                            <p className="text-sm font-bold text-slate-900 dark:text-white">Exportar base de datos</p>
                            <p className="text-xs text-slate-500 mt-1">Genera un respaldo completo para recuperación ante fallos o migraciones.</p>
                        </div>
                        <AppButton
                            variant="primary"
                            size="md"
                            icon="download"
                            isLoading={isExportingDatabase}
                            onClick={handleExportDatabase}
                        >
                            Descargar respaldo
                        </AppButton>
                    </div>

                    <div className="rounded-2xl border border-slate-100 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-800/30 p-5 space-y-3">
                        <div>
                            <p className="text-sm font-bold text-slate-900 dark:text-white">Restaurar respaldo</p>
                            <p className="text-xs text-slate-500 mt-1">Acepta archivos .backup y .sql generados para PostgreSQL.</p>
                        </div>
                        <label className="flex flex-col gap-2 rounded-2xl border border-dashed border-slate-300 dark:border-slate-700 px-4 py-4 text-sm text-slate-600 dark:text-slate-300 cursor-pointer hover:border-sky-400">
                            <span className="font-semibold">{backupFile ? backupFile.name : 'Seleccionar archivo de respaldo'}</span>
                            <span className="text-xs text-slate-400">Formatos soportados: .backup, .sql</span>
                            <input
                                type="file"
                                accept=".backup,.sql"
                                className="hidden"
                                onChange={(event) => setBackupFile(event.target.files?.[0] ?? null)}
                            />
                        </label>
                        <AppButton
                            variant="outline"
                            size="md"
                            icon="upload"
                            isLoading={isRestoringDatabase}
                            disabled={!backupFile}
                            onClick={handleRestoreDatabase}
                        >
                            Restaurar base de datos
                        </AppButton>
                    </div>
                </div>

                {backupMessage && (
                    <div className="mt-4 rounded-2xl border border-sky-100 bg-sky-50 px-4 py-3 text-sm font-medium text-sky-700 dark:border-sky-900/40 dark:bg-sky-950/30 dark:text-sky-200">
                        {backupMessage}
                    </div>
                )}
            </DashboardPanel>
        </div>
    );
};

export default AdminHomeView;
