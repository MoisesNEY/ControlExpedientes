import { useEffect, useState } from 'react';
import api from '../../../services/api';

interface DashboardStats {
    totalPacientes: number;
    totalMedicamentos: number;
    totalCitas: number;
    lowStockCount: number;
}

const StatCard = ({ label, value, icon, color, subtitle }: { label: string; value: string; icon: string; color: string; subtitle?: string }) => (
    <div className="bg-white dark:bg-slate-900 p-5 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800 flex items-center gap-4 transition-colors">
        <div className={`${color} size-12 rounded-lg flex items-center justify-center text-white shadow-lg`}>
            <span className="material-symbols-outlined">{icon}</span>
        </div>
        <div>
            <p className="text-slate-500 dark:text-slate-400 text-xs font-bold uppercase tracking-wider">{label}</p>
            <p className="text-2xl font-black text-slate-900 dark:text-white leading-none mt-1">{value}</p>
            {subtitle && <p className="text-[10px] text-slate-400 mt-1">{subtitle}</p>}
        </div>
    </div>
);

const AdminHomeView = () => {
    const [stats, setStats] = useState<DashboardStats>({
        totalPacientes: 0,
        totalMedicamentos: 0,
        totalCitas: 0,
        lowStockCount: 0,
    });
    const [recentAudit, setRecentAudit] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchStats = async () => {
            setLoading(true);
            try {
                const [pacientes, medicamentos, citas, lowStock, auditoria] = await Promise.all([
                    api.get('/api/pacientes/count'),
                    api.get('/api/medicamentos/count'),
                    api.get('/api/cita-medicas/count'),
                    api.get('/api/medicamentos/low-stock'),
                    api.get('/api/auditoria-acciones', { params: { size: 5, sort: 'fecha,desc' } }),
                ]);

                setStats({
                    totalPacientes: pacientes.data,
                    totalMedicamentos: medicamentos.data,
                    totalCitas: citas.data,
                    lowStockCount: Array.isArray(lowStock.data) ? lowStock.data.length : 0,
                });
                setRecentAudit(auditoria.data || []);
            } catch (error) {
                console.error('Error fetching admin stats:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchStats();
    }, []);

    if (loading) {
        return (
            <div className="p-8 flex items-center justify-center h-full">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-amber-600"></div>
            </div>
        );
    }

    return (
        <div className="p-4 md:p-8 max-w-6xl mx-auto w-full flex flex-col gap-6 md:gap-8 transition-colors duration-300">
            {/* Header */}
            <div className="flex flex-col gap-1">
                <h2 className="text-slate-900 dark:text-white text-2xl md:text-3xl font-black tracking-tight">Panel de Administración</h2>
                <p className="text-slate-500 text-sm md:text-base font-medium tracking-tight">
                    Resumen general del sistema — {new Date().toLocaleDateString('es-ES', { day: '2-digit', month: 'long', year: 'numeric' })}.
                </p>
            </div>

            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6">
                <StatCard label="Pacientes" value={stats.totalPacientes.toString()} icon="group" color="bg-blue-600" />
                <StatCard label="Medicamentos" value={stats.totalMedicamentos.toString()} icon="medication" color="bg-emerald-600" />
                <StatCard label="Citas Totales" value={stats.totalCitas.toString()} icon="calendar_month" color="bg-violet-600" />
                <StatCard
                    label="Stock Bajo"
                    value={stats.lowStockCount.toString()}
                    icon="warning"
                    color={stats.lowStockCount > 0 ? 'bg-red-500' : 'bg-slate-400'}
                    subtitle={stats.lowStockCount > 0 ? 'Requiere atención' : 'Todo en orden'}
                />
            </div>

            {/* Actividad Reciente */}
            <div className="bg-white dark:bg-slate-900 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800 overflow-hidden">
                <div className="p-6 border-b border-slate-100 dark:border-slate-800">
                    <h3 className="text-slate-900 dark:text-white font-bold flex items-center gap-2">
                        <span className="material-symbols-outlined text-amber-600">shield</span>
                        Actividad Reciente
                    </h3>
                </div>
                <div className="divide-y divide-slate-100 dark:divide-slate-800">
                    {recentAudit.length > 0 ? (
                        recentAudit.map((entry: any) => (
                            <div key={entry.id} className="px-6 py-4 flex items-center gap-4 hover:bg-slate-50 dark:hover:bg-slate-800/30 transition-colors">
                                <div className={`size-8 rounded-lg flex items-center justify-center text-white text-xs font-bold ${
                                    entry.accion === 'CREAR' ? 'bg-emerald-500' :
                                    entry.accion === 'EDITAR' || entry.accion === 'ACTUALIZAR' ? 'bg-blue-500' :
                                    entry.accion === 'ELIMINAR' ? 'bg-red-500' : 'bg-slate-400'
                                }`}>
                                    <span className="material-symbols-outlined text-sm">
                                        {entry.accion === 'CREAR' ? 'add' :
                                         entry.accion === 'ELIMINAR' ? 'delete' : 'edit'}
                                    </span>
                                </div>
                                <div className="flex-1 min-w-0">
                                    <p className="text-sm font-semibold text-slate-800 dark:text-white truncate">
                                        {entry.accion} — {entry.entidad}
                                    </p>
                                    <p className="text-[10px] text-slate-400 truncate">{entry.descripcion || 'Sin descripción'}</p>
                                </div>
                                <div className="text-right flex-shrink-0">
                                    <p className="text-[10px] text-slate-400 font-bold">
                                        {new Date(entry.fecha).toLocaleDateString('es-ES', { day: '2-digit', month: 'short' })}
                                    </p>
                                    <p className="text-[10px] text-slate-300">
                                        {new Date(entry.fecha).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                    </p>
                                </div>
                            </div>
                        ))
                    ) : (
                        <div className="px-6 py-10 text-center text-slate-400 italic font-medium">
                            No hay actividad reciente registrada.
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default AdminHomeView;
