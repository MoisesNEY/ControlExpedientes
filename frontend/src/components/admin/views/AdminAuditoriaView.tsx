import { useState, useEffect } from 'react';
import { AuditoriaService, type AuditoriaAccionesDTO } from '../../../services/auditoria.service';

const AdminAuditoriaView = () => {
    const [registros, setRegistros] = useState<AuditoriaAccionesDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const [filterEntidad, setFilterEntidad] = useState('');
    const [filterAccion, setFilterAccion] = useState('');

    const fetchRegistros = async () => {
        setLoading(true);
        try {
            const params: Record<string, any> = { sort: 'fecha,desc', size: 100 };
            if (filterEntidad) params['entidad.equals'] = filterEntidad;
            if (filterAccion) params['accion.equals'] = filterAccion;
            const data = await AuditoriaService.getAll(params);
            setRegistros(data);
        } catch (error) {
            console.error('Error fetching auditoria:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchRegistros(); }, [filterEntidad, filterAccion]);

    const entidades = [...new Set(registros.map(r => r.entidad).filter(Boolean))];
    const acciones = [...new Set(registros.map(r => r.accion).filter(Boolean))];

    const filtered = registros.filter(r => {
        if (!search) return true;
        const q = search.toLowerCase();
        return r.entidad?.toLowerCase().includes(q)
            || r.accion?.toLowerCase().includes(q)
            || r.descripcion?.toLowerCase().includes(q)
            || r.user?.login?.toLowerCase().includes(q);
    });

    const accionColor = (accion: string) => {
        const a = accion?.toUpperCase();
        if (a?.includes('CREAR') || a?.includes('CREATE') || a?.includes('POST')) return 'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600';
        if (a?.includes('EDITAR') || a?.includes('UPDATE') || a?.includes('PUT')) return 'bg-blue-100 dark:bg-blue-900/30 text-blue-600';
        if (a?.includes('ELIMINAR') || a?.includes('DELETE')) return 'bg-red-100 dark:bg-red-900/30 text-red-600';
        return 'bg-slate-100 dark:bg-slate-800 text-slate-500';
    };

    return (
        <div className="p-4 md:p-8 space-y-4 md:space-y-6">
            <div>
                <h2 className="text-xl md:text-2xl font-black text-slate-900 dark:text-white">Auditoría de Acciones</h2>
                <p className="text-slate-500 text-xs md:text-sm font-medium">Registro de todas las acciones realizadas en el sistema.</p>
            </div>

            <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden">
                <div className="p-4 md:p-6 border-b border-slate-100 dark:border-slate-800 flex flex-col md:flex-row gap-3">
                    <div className="relative flex-1">
                        <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-lg">search</span>
                        <input type="text" placeholder="Buscar por entidad, acción, descripción o usuario..." value={search} onChange={e => setSearch(e.target.value)}
                            className="w-full pl-10 pr-4 py-2.5 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl text-sm outline-none focus:ring-2 focus:ring-amber-600 text-slate-900 dark:text-white" />
                    </div>
                    <select value={filterEntidad} onChange={e => setFilterEntidad(e.target.value)}
                        className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600">
                        <option value="">Todas las entidades</option>
                        {entidades.map(ent => <option key={ent} value={ent}>{ent}</option>)}
                    </select>
                    <select value={filterAccion} onChange={e => setFilterAccion(e.target.value)}
                        className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600">
                        <option value="">Todas las acciones</option>
                        {acciones.map(acc => <option key={acc} value={acc}>{acc}</option>)}
                    </select>
                </div>
                <div className="overflow-x-auto scrollbar-hide">
                    <table className="w-full text-left min-w-[800px]">
                        <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 text-[10px] font-black uppercase tracking-widest">
                            <tr>
                                <th className="px-6 py-4">Fecha / Hora</th>
                                <th className="px-6 py-4">Usuario</th>
                                <th className="px-6 py-4">Entidad</th>
                                <th className="px-6 py-4">Acción</th>
                                <th className="px-6 py-4">Descripción</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                            {loading ? (
                                Array(6).fill(0).map((_, i) => (
                                    <tr key={i} className="animate-pulse"><td colSpan={5} className="px-6 py-6"><div className="h-4 bg-slate-100 dark:bg-slate-800 rounded w-full"></div></td></tr>
                                ))
                            ) : (
                                filtered.map(r => {
                                    const d = new Date(r.fecha);
                                    return (
                                        <tr key={r.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/50 transition-colors">
                                            <td className="px-6 py-4">
                                                <p className="text-sm font-bold text-slate-900 dark:text-white">{d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</p>
                                                <p className="text-[10px] text-slate-400 font-bold uppercase">{d.toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' })}</p>
                                            </td>
                                            <td className="px-6 py-4">
                                                <span className="text-sm font-semibold text-slate-700 dark:text-slate-300">{r.user?.login || 'Sistema'}</span>
                                            </td>
                                            <td className="px-6 py-4">
                                                <span className="text-xs font-bold text-amber-600 bg-amber-50 dark:bg-amber-900/20 px-2 py-1 rounded-lg">{r.entidad}</span>
                                            </td>
                                            <td className="px-6 py-4">
                                                <span className={`text-[10px] font-black uppercase px-2 py-1 rounded-lg ${accionColor(r.accion)}`}>{r.accion}</span>
                                            </td>
                                            <td className="px-6 py-4 text-xs text-slate-500 dark:text-slate-400 truncate max-w-xs">{r.descripcion || '—'}</td>
                                        </tr>
                                    );
                                })
                            )}
                            {!loading && filtered.length === 0 && (
                                <tr><td colSpan={5} className="px-6 py-20 text-center text-slate-400 italic font-medium">No se encontraron registros de auditoría.</td></tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default AdminAuditoriaView;
