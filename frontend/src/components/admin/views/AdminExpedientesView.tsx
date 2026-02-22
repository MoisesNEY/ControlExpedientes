import { useState, useEffect } from 'react';
import { ExpedienteService, type ExpedienteClinicoDTO } from '../../../services/expediente.service';
import { PacienteService, type PacienteDTO } from '../../../services/paciente.service';

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
                                            <span className="text-sm font-bold text-amber-600">{e.numeroExpediente}</span>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className="text-sm font-semibold text-slate-700 dark:text-slate-300">
                                                {e.paciente?.nombres ? `${e.paciente.nombres} ${e.paciente.apellidos || ''}` : 'N/A'}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 text-sm text-slate-500">
                                            {new Date(e.fechaApertura + 'T00:00:00').toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' })}
                                        </td>
                                        <td className="px-6 py-4 text-xs text-slate-500 dark:text-slate-400 truncate max-w-xs">{e.observaciones || '—'}</td>
                                        <td className="px-6 py-4 text-right">
                                            <div className="flex items-center justify-end gap-1">
                                                <button onClick={() => openEdit(e)} className="p-2 text-slate-400 hover:text-amber-600 transition-colors"><span className="material-symbols-outlined text-sm">edit</span></button>
                                                <button onClick={() => e.id && handleDelete(e.id)} className="p-2 text-slate-400 hover:text-red-500 transition-colors"><span className="material-symbols-outlined text-sm">delete</span></button>
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
        </div>
    );
};

export default AdminExpedientesView;
