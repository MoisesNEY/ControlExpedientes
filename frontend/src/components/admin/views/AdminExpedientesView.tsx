import { useState, useEffect } from 'react';
import { ExpedienteService, type ExpedienteClinicoDTO } from '../../../services/expediente.service';
import { PacienteService, type PacienteDTO } from '../../../services/paciente.service';

interface TimelineEntry {
    fecha: string;
    profesional: string;
    motivo: string;
    diagnosticos: string[];
    recetas: string[];
    signosVitales?: {
        peso?: number;
        altura?: number;
        presionArterial?: string;
        temperatura?: number;
        frecuenciaCardiaca?: number;
    };
}

const emptyExpediente: ExpedienteClinicoDTO = {
    numeroExpediente: '',
    fechaApertura: new Date().toISOString().slice(0, 10),
    observaciones: '',
    paciente: undefined,
};

const AdminExpedientesView = () => {
    const [expedientes, setExpedientes] = useState<ExpedienteClinicoDTO[]>([]);
    const [pacientes, setPacientes] = useState<PacienteDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const [showForm, setShowForm] = useState(false);
    const [editing, setEditing] = useState<ExpedienteClinicoDTO | null>(null);
    const [form, setForm] = useState<ExpedienteClinicoDTO>(emptyExpediente);
    const [saving, setSaving] = useState(false);

    // Detail modal state
    const [detailExpediente, setDetailExpediente] = useState<ExpedienteClinicoDTO | null>(null);
    const [timeline, setTimeline] = useState<TimelineEntry[]>([]);
    const [timelineLoading, setTimelineLoading] = useState(false);

    const fetchExpedientes = async () => {
        setLoading(true);
        try {
            const data = await ExpedienteService.getAll({ sort: 'fechaApertura,desc', size: 100 });
            setExpedientes(data);
        } catch (error) {
            console.error('Error fetching expedientes:', error);
        } finally {
            setLoading(false);
        }
    };

    const fetchPacientes = async () => {
        try {
            const data = await PacienteService.getAll({ 'activo.equals': true, size: 200, sort: 'nombres,asc' });
            setPacientes(data);
        } catch (error) {
            console.error('Error fetching pacientes:', error);
        }
    };

    useEffect(() => { fetchExpedientes(); fetchPacientes(); }, []);

    const filtered = expedientes.filter(e => {
        const q = search.toLowerCase();
        return !q
            || e.numeroExpediente.toLowerCase().includes(q)
            || (e.paciente?.nombres || '').toLowerCase().includes(q)
            || (e.paciente?.apellidos || '').toLowerCase().includes(q);
    });

    const openCreate = () => {
        setEditing(null);
        setForm({ ...emptyExpediente, fechaApertura: new Date().toISOString().slice(0, 10) });
        setShowForm(true);
    };

    const openEdit = (e: ExpedienteClinicoDTO) => {
        setEditing(e);
        setForm({ ...e });
        setShowForm(true);
    };

    const handleClose = () => { setShowForm(false); setEditing(null); setForm(emptyExpediente); };

    const handleSave = async () => {
        if (!form.numeroExpediente || !form.fechaApertura || !form.paciente?.id) {
            alert('Complete los campos obligatorios: paciente, número de expediente y fecha de apertura.');
            return;
        }
        setSaving(true);
        try {
            if (editing?.id) {
                await ExpedienteService.update(editing.id, form);
            } else {
                await ExpedienteService.create(form);
            }
            handleClose();
            fetchExpedientes();
        } catch (error: any) {
            alert(error?.response?.data?.message || 'Error al guardar el expediente.');
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (id: number) => {
        if (!confirm('¿Eliminar este expediente clínico?')) return;
        try {
            await ExpedienteService.delete(id);
            fetchExpedientes();
        } catch (error: any) {
            alert(error?.response?.data?.message || 'Error al eliminar.');
        }
    };

    const openDetail = async (e: ExpedienteClinicoDTO) => {
        setDetailExpediente(e);
        setTimeline([]);
        if (!e.id) return;
        setTimelineLoading(true);
        try {
            const data = await ExpedienteService.getTimeline(e.id);
            setTimeline(data as TimelineEntry[]);
        } catch (error) {
            console.error('Error fetching timeline:', error);
        } finally {
            setTimelineLoading(false);
        }
    };

    const closeDetail = () => {
        setDetailExpediente(null);
        setTimeline([]);
    };

    const formatDate = (dateStr: string) => {
        try {
            const d = dateStr.includes('T') ? new Date(dateStr) : new Date(dateStr + 'T00:00:00');
            return d.toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' });
        } catch {
            return dateStr;
        }
    };

    const formatDateTime = (dateStr: string) => {
        try {
            const d = new Date(dateStr);
            return d.toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
        } catch {
            return dateStr;
        }
    };

    return (
        <div className="p-4 md:p-8 space-y-4 md:space-y-6">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                    <h2 className="text-xl md:text-2xl font-black text-slate-900 dark:text-white">Expedientes Clínicos</h2>
                    <p className="text-slate-500 text-xs md:text-sm font-medium">Administrar expedientes clínicos del sistema.</p>
                </div>
                <button onClick={openCreate} className="w-full md:w-auto flex items-center justify-center gap-2 px-6 py-3 bg-amber-600 text-white rounded-xl font-bold shadow-lg shadow-amber-600/30 hover:scale-105 transition-transform">
                    <span className="material-symbols-outlined">note_add</span>
                    Nuevo Expediente
                </button>
            </div>

            <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden">
                <div className="p-4 md:p-6 border-b border-slate-100 dark:border-slate-800">
                    <div className="relative">
                        <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-lg">search</span>
                        <input type="text" placeholder="Buscar por N° expediente o paciente..." value={search} onChange={e => setSearch(e.target.value)}
                            className="w-full pl-10 pr-4 py-2.5 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl text-sm outline-none focus:ring-2 focus:ring-amber-600 text-slate-900 dark:text-white" />
                    </div>
                </div>
                <div className="overflow-x-auto scrollbar-hide">
                    <table className="w-full text-left min-w-[700px]">
                        <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 text-[10px] font-black uppercase tracking-widest">
                            <tr>
                                <th className="px-6 py-4">N° Expediente</th>
                                <th className="px-6 py-4">Paciente</th>
                                <th className="px-6 py-4">Fecha Apertura</th>
                                <th className="px-6 py-4">Observaciones</th>
                                <th className="px-6 py-4 text-right">Acciones</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                            {loading ? (
                                Array(5).fill(0).map((_, i) => (
                                    <tr key={i} className="animate-pulse"><td colSpan={5} className="px-6 py-6"><div className="h-4 bg-slate-100 dark:bg-slate-800 rounded w-full"></div></td></tr>
                                ))
                            ) : (
                                filtered.map(e => (
                                    <tr key={e.id} className="group hover:bg-slate-50/50 dark:hover:bg-slate-800/50 transition-colors">
                                        <td className="px-6 py-4">
                                            <button
                                                onClick={() => openDetail(e)}
                                                className="text-sm font-bold text-amber-600 hover:text-amber-500 hover:underline transition-colors cursor-pointer"
                                            >
                                                {e.numeroExpediente}
                                            </button>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className="text-sm font-semibold text-slate-700 dark:text-slate-300">
                                                {e.paciente?.nombres ? `${e.paciente.nombres} ${e.paciente.apellidos || ''}` : 'N/A'}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 text-sm text-slate-500">
                                            {formatDate(e.fechaApertura)}
                                        </td>
                                        <td className="px-6 py-4 text-xs text-slate-500 dark:text-slate-400 truncate max-w-xs">{e.observaciones || '—'}</td>
                                        <td className="px-6 py-4 text-right">
                                            <div className="flex items-center justify-end gap-1">
                                                <button onClick={() => openDetail(e)} className="p-2 text-slate-400 hover:text-amber-600 transition-colors" title="Ver expediente"><span className="material-symbols-outlined text-sm">visibility</span></button>
                                                <button onClick={() => openEdit(e)} className="p-2 text-slate-400 hover:text-amber-600 transition-colors" title="Editar"><span className="material-symbols-outlined text-sm">edit</span></button>
                                                <button onClick={() => e.id && handleDelete(e.id)} className="p-2 text-slate-400 hover:text-red-500 transition-colors" title="Eliminar"><span className="material-symbols-outlined text-sm">delete</span></button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                            {!loading && filtered.length === 0 && (
                                <tr><td colSpan={5} className="px-6 py-20 text-center text-slate-400 italic font-medium">No se encontraron expedientes.</td></tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Modal Form */}
            {showForm && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                    <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={handleClose} />
                    <div className="relative bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 w-full max-w-md">
                        <div className="p-6 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center">
                            <h3 className="text-lg font-black text-slate-900 dark:text-white">{editing ? 'Editar Expediente' : 'Nuevo Expediente'}</h3>
                            <button onClick={handleClose} className="text-slate-400 hover:text-slate-600"><span className="material-symbols-outlined">close</span></button>
                        </div>
                        <div className="p-6 space-y-4">
                            <div className="flex flex-col gap-1.5">
                                <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Paciente *</label>
                                <select
                                    value={form.paciente?.id || ''}
                                    onChange={e => setForm({ ...form, paciente: { id: parseInt(e.target.value) } })}
                                    className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600"
                                >
                                    <option value="">— Seleccionar paciente —</option>
                                    {pacientes.map(p => (
                                        <option key={p.id} value={p.id}>{p.nombres} {p.apellidos} ({p.codigo})</option>
                                    ))}
                                </select>
                            </div>
                            <div className="flex flex-col gap-1.5">
                                <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">N° Expediente *</label>
                                <input type="text" value={form.numeroExpediente} onChange={e => setForm({ ...form, numeroExpediente: e.target.value })} placeholder="EXP-001"
                                    className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600 text-slate-900 dark:text-white" />
                            </div>
                            <div className="flex flex-col gap-1.5">
                                <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Fecha Apertura *</label>
                                <input type="date" value={form.fechaApertura} onChange={e => setForm({ ...form, fechaApertura: e.target.value })}
                                    className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600 text-slate-900 dark:text-white" />
                            </div>
                            <div className="flex flex-col gap-1.5">
                                <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Observaciones</label>
                                <textarea value={form.observaciones || ''} onChange={e => setForm({ ...form, observaciones: e.target.value })} placeholder="Notas adicionales..."
                                    className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600 min-h-[80px] text-slate-900 dark:text-white" />
                            </div>
                        </div>
                        <div className="p-6 border-t border-slate-100 dark:border-slate-800 flex justify-end gap-3">
                            <button onClick={handleClose} className="px-6 py-2.5 text-sm font-bold text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition-colors">Cancelar</button>
                            <button onClick={handleSave} disabled={saving}
                                className={`px-6 py-2.5 text-sm font-bold text-white bg-amber-600 rounded-xl shadow-lg shadow-amber-600/30 hover:scale-105 transition-all ${saving ? 'opacity-70 cursor-not-allowed' : ''}`}>
                                {saving ? 'Guardando...' : editing ? 'Actualizar' : 'Crear Expediente'}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Detail Modal */}
            {detailExpediente && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                    <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={closeDetail} />
                    <div className="relative bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 w-full max-w-3xl max-h-[90vh] flex flex-col">
                        {/* Header */}
                        <div className="p-6 border-b border-slate-100 dark:border-slate-800 flex-shrink-0">
                            <div className="flex justify-between items-start gap-4">
                                <div className="flex items-start gap-4 min-w-0">
                                    <div className="w-12 h-12 rounded-xl bg-amber-600/10 flex items-center justify-center flex-shrink-0">
                                        <span className="material-symbols-outlined text-amber-600 text-2xl">folder_open</span>
                                    </div>
                                    <div className="min-w-0">
                                        <h3 className="text-lg font-black text-slate-900 dark:text-white">
                                            Expediente {detailExpediente.numeroExpediente}
                                        </h3>
                                        <p className="text-sm text-slate-500 font-medium">
                                            {detailExpediente.paciente?.nombres
                                                ? `${detailExpediente.paciente.nombres} ${detailExpediente.paciente.apellidos || ''}`
                                                : 'Paciente no asignado'}
                                        </p>
                                        <div className="flex flex-wrap items-center gap-3 mt-2">
                                            <span className="inline-flex items-center gap-1 text-xs font-semibold text-slate-400">
                                                <span className="material-symbols-outlined text-xs">calendar_today</span>
                                                Apertura: {formatDate(detailExpediente.fechaApertura)}
                                            </span>
                                            {detailExpediente.observaciones && (
                                                <span className="inline-flex items-center gap-1 text-xs text-slate-400 truncate max-w-xs" title={detailExpediente.observaciones}>
                                                    <span className="material-symbols-outlined text-xs">notes</span>
                                                    {detailExpediente.observaciones}
                                                </span>
                                            )}
                                        </div>
                                    </div>
                                </div>
                                <button onClick={closeDetail} className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-300 flex-shrink-0">
                                    <span className="material-symbols-outlined">close</span>
                                </button>
                            </div>
                        </div>

                        {/* Timeline Content */}
                        <div className="flex-1 overflow-y-auto p-6">
                            <h4 className="text-[10px] font-black text-slate-500 uppercase tracking-widest mb-5">Historial de Consultas</h4>

                            {timelineLoading ? (
                                <div className="space-y-6">
                                    {Array(3).fill(0).map((_, i) => (
                                        <div key={i} className="animate-pulse flex gap-4">
                                            <div className="w-3 h-3 mt-1.5 rounded-full bg-slate-200 dark:bg-slate-700 flex-shrink-0" />
                                            <div className="flex-1 space-y-3">
                                                <div className="h-4 bg-slate-100 dark:bg-slate-800 rounded w-1/3" />
                                                <div className="h-3 bg-slate-100 dark:bg-slate-800 rounded w-2/3" />
                                                <div className="h-3 bg-slate-100 dark:bg-slate-800 rounded w-1/2" />
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            ) : timeline.length === 0 ? (
                                <div className="flex flex-col items-center justify-center py-16 text-center">
                                    <div className="w-16 h-16 rounded-2xl bg-slate-100 dark:bg-slate-800 flex items-center justify-center mb-4">
                                        <span className="material-symbols-outlined text-3xl text-slate-300 dark:text-slate-600">history</span>
                                    </div>
                                    <p className="text-sm font-semibold text-slate-400">Sin consultas registradas</p>
                                    <p className="text-xs text-slate-400 mt-1">Este expediente aún no tiene historial de consultas.</p>
                                </div>
                            ) : (
                                <div className="relative border-l-2 border-amber-600/20 ml-1.5 space-y-6">
                                    {timeline.map((entry, i) => (
                                        <div key={i} className="relative pl-7 group">
                                            {/* Timeline dot */}
                                            <div className="absolute -left-[9px] top-1 w-4 h-4 rounded-full border-[3px] border-amber-600/30 bg-white dark:bg-slate-900 group-hover:border-amber-600 group-hover:scale-125 transition-all" />

                                            <div className="bg-slate-50 dark:bg-slate-800/50 rounded-xl border border-slate-200 dark:border-slate-700 p-4 hover:shadow-md transition-shadow">
                                                {/* Entry header */}
                                                <div className="flex flex-wrap items-center gap-2 mb-3">
                                                    <span className="inline-flex items-center gap-1 text-xs font-bold text-amber-600 bg-amber-600/10 px-2.5 py-1 rounded-lg">
                                                        <span className="material-symbols-outlined text-xs">event</span>
                                                        {formatDateTime(entry.fecha)}
                                                    </span>
                                                    {entry.profesional && (
                                                        <span className="inline-flex items-center gap-1 text-xs font-semibold text-slate-500 dark:text-slate-400">
                                                            <span className="material-symbols-outlined text-xs">person</span>
                                                            {entry.profesional}
                                                        </span>
                                                    )}
                                                </div>

                                                {/* Motivo */}
                                                {entry.motivo && (
                                                    <p className="text-sm text-slate-700 dark:text-slate-300 font-medium mb-3">{entry.motivo}</p>
                                                )}

                                                {/* Diagnósticos & Recetas grid */}
                                                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                                                    {entry.diagnosticos && entry.diagnosticos.length > 0 && (
                                                        <div className="space-y-1.5">
                                                            <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest">Diagnósticos</span>
                                                            <div className="flex flex-wrap gap-1.5">
                                                                {entry.diagnosticos.map((d, j) => (
                                                                    <span key={j} className="inline-flex items-center gap-1 text-xs font-medium bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-400 px-2 py-0.5 rounded-md">
                                                                        <span className="material-symbols-outlined text-xs">medical_information</span>
                                                                        {d}
                                                                    </span>
                                                                ))}
                                                            </div>
                                                        </div>
                                                    )}

                                                    {entry.recetas && entry.recetas.length > 0 && (
                                                        <div className="space-y-1.5">
                                                            <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest">Recetas</span>
                                                            <div className="flex flex-wrap gap-1.5">
                                                                {entry.recetas.map((r, j) => (
                                                                    <span key={j} className="inline-flex items-center gap-1 text-xs font-medium bg-emerald-50 dark:bg-emerald-900/20 text-emerald-700 dark:text-emerald-400 px-2 py-0.5 rounded-md">
                                                                        <span className="material-symbols-outlined text-xs">medication</span>
                                                                        {r}
                                                                    </span>
                                                                ))}
                                                            </div>
                                                        </div>
                                                    )}
                                                </div>

                                                {/* Signos Vitales */}
                                                {entry.signosVitales && (
                                                    <div className="mt-3 pt-3 border-t border-slate-200 dark:border-slate-700">
                                                        <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest">Signos Vitales</span>
                                                        <div className="flex flex-wrap gap-3 mt-1.5">
                                                            {entry.signosVitales.presionArterial && (
                                                                <span className="inline-flex items-center gap-1 text-xs text-slate-600 dark:text-slate-400">
                                                                    <span className="material-symbols-outlined text-sm text-red-400">bloodtype</span>
                                                                    PA: {entry.signosVitales.presionArterial}
                                                                </span>
                                                            )}
                                                            {entry.signosVitales.temperatura != null && (
                                                                <span className="inline-flex items-center gap-1 text-xs text-slate-600 dark:text-slate-400">
                                                                    <span className="material-symbols-outlined text-sm text-orange-400">thermostat</span>
                                                                    {entry.signosVitales.temperatura}°C
                                                                </span>
                                                            )}
                                                            {entry.signosVitales.frecuenciaCardiaca != null && (
                                                                <span className="inline-flex items-center gap-1 text-xs text-slate-600 dark:text-slate-400">
                                                                    <span className="material-symbols-outlined text-sm text-pink-400">favorite</span>
                                                                    {entry.signosVitales.frecuenciaCardiaca} bpm
                                                                </span>
                                                            )}
                                                            {entry.signosVitales.peso != null && (
                                                                <span className="inline-flex items-center gap-1 text-xs text-slate-600 dark:text-slate-400">
                                                                    <span className="material-symbols-outlined text-sm text-slate-400">scale</span>
                                                                    {entry.signosVitales.peso} kg
                                                                </span>
                                                            )}
                                                            {entry.signosVitales.altura != null && (
                                                                <span className="inline-flex items-center gap-1 text-xs text-slate-600 dark:text-slate-400">
                                                                    <span className="material-symbols-outlined text-sm text-slate-400">height</span>
                                                                    {entry.signosVitales.altura} cm
                                                                </span>
                                                            )}
                                                        </div>
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>

                        {/* Footer */}
                        <div className="p-4 border-t border-slate-100 dark:border-slate-800 flex justify-end flex-shrink-0">
                            <button onClick={closeDetail} className="px-6 py-2.5 text-sm font-bold text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition-colors">
                                Cerrar
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminExpedientesView;
